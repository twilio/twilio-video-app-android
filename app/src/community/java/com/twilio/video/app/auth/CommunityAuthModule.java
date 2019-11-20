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
import com.twilio.video.app.ApplicationScope;

import dagger.Module;
import dagger.Provides;

// TODO Remove as part of https://issues.corp.twilio.com/browse/AHOYAPPS-93
@Module
public class CommunityAuthModule {

    @Provides
    @ApplicationScope
    FirebaseFacade providesFirebaseFacade(FirebaseWrapper firebaseWrapper, Application application) {
        Context context = application.getApplicationContext();
        return new FirebaseFacade(firebaseWrapper,
                new GoogleAuthenticator(
                        new FirebaseWrapper(),
                        context,
                        new GoogleAuthWrapper(),
                        new GoogleSignInWrapper(),
                        new GoogleSignInOptionsBuilderWrapper(GoogleSignInOptions.DEFAULT_SIGN_IN),
                        new GoogleAuthProviderWrapper()),
                new EmailAuthenticator(firebaseWrapper));
    }

    @Provides
    @ApplicationScope
    FirebaseWrapper providesFirebaseWrapper() {
        return new FirebaseWrapper();
    }

    @Provides
    @ApplicationScope
    CommunityAuthenticator providesCommunityAuthenticator(SharedPreferences preferences) {
        return new CommunityAuthenticator(preferences);
    }
}
