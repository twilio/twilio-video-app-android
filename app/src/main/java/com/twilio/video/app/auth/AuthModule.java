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
import android.content.SharedPreferences;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.twilio.video.app.ApplicationModule;
import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.idlingresource.ICountingIdlingResource;
import com.twilio.video.app.idlingresource.IdlingResourceModule;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module(includes = {
        ApplicationModule.class,
        IdlingResourceModule.class
})
public class AuthModule {

    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator(
            FirebaseWrapper firebaseWrapper,
            Application application,
            SharedPreferences sharedPreferences,
            ICountingIdlingResource resource) {
        Context context = application.getApplicationContext();
        List<AuthenticationProvider> authenticators = new ArrayList<>();
        authenticators.add(
                new GoogleAuthenticator(
                        new FirebaseWrapper(),
                        context,
                        new GoogleAuthWrapper(),
                        new GoogleSignInWrapper(),
                        new GoogleSignInOptionsBuilderWrapper(GoogleSignInOptions.DEFAULT_SIGN_IN),
                        new GoogleAuthProviderWrapper(),
                        sharedPreferences)
        );
        authenticators.add(new EmailAuthenticator(firebaseWrapper, sharedPreferences, resource));
        return new FirebaseAuthenticator(firebaseWrapper, authenticators);
    }

    @Provides
    @ApplicationScope
    FirebaseWrapper providesFirebaseWrapper() {
        return new FirebaseWrapper();
    }
}
