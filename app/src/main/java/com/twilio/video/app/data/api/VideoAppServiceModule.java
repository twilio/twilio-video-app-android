package com.twilio.video.app.data.api;


import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.twilio.video.app.ApplicationScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class VideoAppServiceModule {
    @Provides
    @ApplicationScope
    @Named("VideoAppService")
    OkHttpClient providesOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new FirebaseAuthInterceptor())
                .build();

        return okHttpClient;
    }

    @Provides
    @ApplicationScope
    @Named("VideoAppService")
    Retrofit.Builder providesRetrofitBuilder(@Named("VideoAppService") OkHttpClient okHttpClient) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        return retrofitBuilder;
    }
}
