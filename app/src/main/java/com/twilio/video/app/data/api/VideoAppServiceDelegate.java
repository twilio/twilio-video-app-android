package com.twilio.video.app.data.api;

import android.content.SharedPreferences;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.data.api.model.RoomProperties;
import com.twilio.video.twilioapi.TwilioApiUtils;
import io.reactivex.Single;

public class VideoAppServiceDelegate implements TokenService {
    private static final String APP_FLAVOR_TWILIO = "twilio";
    private static final String APP_FLAVOR_INTERNAL = "internal";

    private final SharedPreferences sharedPreferences;
    private final VideoAppService videoAppServiceDev;
    private final VideoAppService videoAppServiceStage;
    private final VideoAppService videoAppServiceProd;

    public VideoAppServiceDelegate(
            final SharedPreferences sharedPreferences,
            final VideoAppService videoAppServiceDev,
            final VideoAppService videoAppServiceStage,
            final VideoAppService videoAppServiceProd) {
        this.sharedPreferences = sharedPreferences;
        this.videoAppServiceDev = videoAppServiceDev;
        this.videoAppServiceStage = videoAppServiceStage;
        this.videoAppServiceProd = videoAppServiceProd;
    }

    @Override
    public Single<String> getToken(String identity, RoomProperties roomProperties) {
        final String env =
                sharedPreferences.getString(
                        Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT);

        String appEnv = resolveAppEnvironment(BuildConfig.FLAVOR);
        VideoAppService videoAppService = resolveVideoAppService(env);
        return videoAppService.getToken(
                identity,
                roomProperties.getName(),
                appEnv,
                roomProperties.getTopology().getString(),
                roomProperties.isRecordParticipantsOnConnect());
    }

    private String resolveAppEnvironment(String appFlavor) {
        // Video App Service only accepts internal and production for app environment
        switch (appFlavor) {
            case APP_FLAVOR_TWILIO:
                return "production";
            case APP_FLAVOR_INTERNAL:
                return "internal";
            default:
                return "production";
        }
    }

    private VideoAppService resolveVideoAppService(String env) {
        switch (env) {
            case TwilioApiUtils.DEV:
                return videoAppServiceDev;
            case TwilioApiUtils.STAGE:
                return videoAppServiceStage;
            case TwilioApiUtils.PROD:
                return videoAppServiceProd;
            default:
                return videoAppServiceProd;
        }
    }
}
