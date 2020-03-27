package com.twilio.video.util;

import static org.junit.Assert.fail;

import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

class OkHttpClientProvider {
    private static final String TAG = "OkHttpClient";
    private static final int HTTP_UPGRADE_REQUIRED = 426;

    static OkHttpClient setupOkHttpClient() {
        OkHttpClient.Builder client =
                new OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .addInterceptor(httpUpgradeRequiredInterceptor())
                        .cache(null)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS);

        return enableTls12OnPreLollipop(client).build();
    }

    private static Interceptor httpUpgradeRequiredInterceptor() {
        return chain -> {
            Request request = chain.request();

            Response response = chain.proceed(request);

            /*
             * Receiving an Upgrade Required error code indicates that enableTls12OnPreLollipop
             * did not succeed enabling TLS 1.2 on the device. This failure means that
             * the test will not be able to execute Twilio REST API calls and the test
             * will not pass. As a result, short circuit to a test failure that explains
             * what happened.
             */
            if (response.code() == HTTP_UPGRADE_REQUIRED) {
                fail(
                        "Received Http Upgrade Required error which means that the test failed"
                                + "to enable TLS 1.2 on this device.");
            }

            return response;
        };
    }

    /*
     * Configure an OkHttp Client with TLS 1.2 enabled.
     *
     * This code was inspired from the following articles.
     *
     * https://medium.com/square-corner-blog/okhttp-3-13-requires-android-5-818bb78d07ce
     * https://medium.com/tech-quizlet/working-with-tls-1-2-on-android-4-4-and-lower-f4f5205629a
     */
    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {

            /*
             * Update device security protocols in accordance with Google documentation.
             *
             * https://developer.android.com/training/articles/security-gms-provider
             */
            try {
                ProviderInstaller.installIfNeeded(InstrumentationRegistry.getContext());
                ProviderInstaller.installIfNeeded(InstrumentationRegistry.getTargetContext());
            } catch (GooglePlayServicesRepairableException e) {
                Log.e(TAG, e.getMessage());
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, e.getMessage());
            }

            try {
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                X509TrustManager x509TrustManager = null;

                for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        x509TrustManager = (X509TrustManager) trustManager;
                    }
                }

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, new TrustManager[] {x509TrustManager}, null);
                client.sslSocketFactory(
                        new Tls12SocketFactory(sc.getSocketFactory()), x509TrustManager);

                ConnectionSpec cs =
                        new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                .tlsVersions(TlsVersion.TLS_1_2)
                                .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }
}
