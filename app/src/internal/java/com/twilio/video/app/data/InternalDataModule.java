package com.twilio.video.app.data;

import android.content.SharedPreferences;

import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.data.api.TokenService;
import com.twilio.video.app.data.api.VideoAppService;
import com.twilio.video.app.data.api.model.Topology;
import com.twilio.video.app.data.api.model.VideoConfiguration;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

@Module(includes = DataModule.class)
public class InternalDataModule {
    @Provides
    @ApplicationScope
    TokenService providesTokenService(final SharedPreferences sharedPreferences,
                                      final VideoAppService videoAppService) {
        return new TokenService() {
            @Override
            public Single<String> getToken(final String identity, final Topology topology) {
                final String env = sharedPreferences.getString(Preferences.ENVIRONMENT,
                        Preferences.ENVIRONMENT_DEFAULT);

                return videoAppService.getConfiguration(env)
                        .flatMap(new Function<VideoConfiguration, SingleSource<? extends String>>() {
                            @Override
                            public SingleSource<? extends String>
                            apply(@NonNull VideoConfiguration videoConfiguration)
                                    throws Exception {
                                return videoAppService.getToken(env,
                                        identity,
                                        videoConfiguration.getSid(topology));
                            }
                        });
            }
        };
    }
}
