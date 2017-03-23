package com.twilio.video.app.util;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonObject;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Video App token server client.
 */
public class VideoAppService {

    // urls
    private static final String BASE_URL = "https://app.video.bytwilio.com/";

    // headers
    private static final String HEADER_AUTHORIZATION = "Authorization";

    // payload parameter names
    private static final String PAYLOAD_CONFIGURATION_PROFILE_SIDS = "configurationProfileSids";

    // request parameter names
    private static final String REQUEST_ENVIRONMENT = "environment";
    private static final String REQUEST_IDENTITY = "identity";
    private static final String REQUEST_TTL = "ttl";
    private static final String REQUEST_CONFIGURATION_PROFILE_SID = "configurationProfileSid";

    // defaults
    private static final int DEFAULT_TTL = 1800;

    /**
     * Video App Token server urls
     */
    private interface VideoAppServiceApi {

        @GET("api/v1/token")
        Single<String> getAccessToken(@QueryMap Map<String, String> options);

        @GET("api/v1/configuration")
        Single<JsonObject> getConfigurationProfile(@Query(REQUEST_ENVIRONMENT) String environment);
    }

    /**
     * Authorization interceptor. Adds firebase token as a "Authorization" header.
     */
    private static class FirebaseAuthInterceptor implements Interceptor {

        private String authToken;

        FirebaseAuthInterceptor() {
            this.authToken = "";
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request authorizedRequest = chain.request()
                    .newBuilder()
                    .addHeader(HEADER_AUTHORIZATION, authToken)
                    .build();

            return chain.proceed(authorizedRequest);
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }
    }

    /**
     * Video App token server client.
     */
    private static final VideoAppService instance = new VideoAppService();

    /**
     * Video App client API.
     */
    private VideoAppServiceApi videoAppServiceApi;

    /**
     * Videp App token server authorization interceptor.
     */
    private FirebaseAuthInterceptor authInterceptor;

    /**
     * Video App token server authorized client with Firebase token.
     * All network requests by default are handled on {@link Schedulers#io()}.
     */
    private VideoAppService() {

        authInterceptor = new FirebaseAuthInterceptor();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build();

        videoAppServiceApi = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VideoAppServiceApi.class);
    }

    /**
     * Obtain configuration profile for specified environment.
     *
     * @param environment configuration profile environment ("prod"|"stage"|"dev")
     * @return JSON represetation with all configuration profiles available.
     */
    public static Single<String> getConfigurationProfile(final String environment, final String topology) {
        return injectAuthToken()
                .andThen(Single.defer(new Callable<SingleSource<? extends String>>() {
                    @Override
                    public SingleSource<? extends String> call() throws Exception {
                        return instance.videoAppServiceApi.getConfigurationProfile(environment)
                                .map(new Function<JsonObject, String>() {
                                    @Override
                                    public String apply(@NonNull JsonObject jsonObject)
                                            throws Exception {
                                        return jsonObject
                                                .getAsJsonObject(PAYLOAD_CONFIGURATION_PROFILE_SIDS)
                                                .get(topology).getAsString();
                                    }
                                });
                    }
                }));
    }

    /**
     * Obtain access token for {@link com.twilio.video.VideoClient}.
     *
     * @param environment             environment to setup for {@link com.twilio.video.VideoClient}.
     * @param identity                client identity or display name.
     * @param configurationProfileSid configuration profile.
     * @return String access token.
     */
    public static Single<String> getAccessToken(String environment,
                                                String identity,
                                                String configurationProfileSid) {

        return getAccessToken(environment, identity, DEFAULT_TTL, configurationProfileSid);
    }

    /**
     * Obtain access token for {@link com.twilio.video.VideoClient}.
     *
     * @param environment             environment to setup for {@link com.twilio.video.VideoClient}.
     * @param identity                client identity or display name.
     * @param ttl                     token expiration time.
     * @param configurationProfileSid configuration profile.
     * @return String access token.
     */
    public static Single<String> getAccessToken(final String environment,
                                                final String identity,
                                                final int ttl,
                                                final String configurationProfileSid) {

        return injectAuthToken()
                .andThen(Single.defer(new Callable<SingleSource<? extends String>>() {
                    @Override
                    public SingleSource<? extends String> call() throws Exception {
                        final Map<String, String> queryParams = ImmutableMap
                                .<String, String>builder()
                                .put(REQUEST_ENVIRONMENT, environment)
                                .put(REQUEST_IDENTITY, identity)
                                .put(REQUEST_CONFIGURATION_PROFILE_SID, configurationProfileSid)
                                .put(REQUEST_TTL, String.valueOf(ttl))
                                .build();

                        return instance.videoAppServiceApi.getAccessToken(queryParams);
                    }
                }));
    }

    /**
     * Obtain Firebase token, if user is not found throws {@link IllegalStateException}
     *
     * @return String Firebase token.
     */
    private static Single<String> getFirebaseToken() {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(final SingleEmitter<String> emitter) throws Exception {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (firebaseUser == null) {
                    throw new IllegalStateException("Firebase user is not found");
                }

                firebaseUser.getToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@android.support.annotation.NonNull Task<GetTokenResult> task) {
                                if (!task.isSuccessful()) {
                                    emitter.onError(task.getException());
                                } else {
                                    emitter.onSuccess(task.getResult().getToken());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@android.support.annotation.NonNull Exception e) {
                                emitter.onError(e);
                            }
                        });
            }
        });
    }

    /**
     * Firebase token injector.
     * <p>
     * NOTE: Requests Firebase current user auth token and updates interceptor with actual version.
     *
     * @return Completable.
     */
    private static Completable injectAuthToken() {
        return getFirebaseToken()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(new Function<String, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@NonNull final String authToken)
                            throws Exception {
                        return Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                instance.authInterceptor.setAuthToken(authToken);
                            }
                        });
                    }
                });
    }

}
