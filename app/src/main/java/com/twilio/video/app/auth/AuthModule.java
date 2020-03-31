/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.app.auth;

import android.app.Application;
import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.twilio.video.app.ApplicationModule;
import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.R;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.List;

@Module(includes = {ApplicationModule.class})
public class AuthModule {

    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator(FirebaseWrapper firebaseWrapper, Application application) {
        Context context = application.getApplicationContext();
        List<AuthenticationProvider> authProviders = new ArrayList<>();
        String acceptedDomain = "twilio.com";
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .setHostedDomain(acceptedDomain)
                        .build();

        authProviders.add(
                GoogleAuthProvider.Companion.newInstance(
                        context, googleSignInOptions, acceptedDomain));
        authProviders.add(new EmailAuthProvider(firebaseWrapper));
        return new FirebaseAuthenticator(firebaseWrapper, authProviders);
    }

    @Provides
    @ApplicationScope
    FirebaseWrapper providesFirebaseWrapper() {
        return new FirebaseWrapper();
    }
}
