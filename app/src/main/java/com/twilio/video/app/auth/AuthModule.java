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

import com.twilio.video.app.ApplicationScope;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module
public class AuthModule {

    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator(FirebaseWrapper firebaseWrapper) {
        return new FirebaseAuthenticator(firebaseWrapper);
    }

    @Provides
    @ApplicationScope
    FirebaseAuthenticator providesFirebaseAuthenticator(FirebaseWrapper firebaseWrapper) {
        return new FirebaseAuthenticator(firebaseWrapper);
    }

    @Provides
    @ApplicationScope
    GoogleAuthenticator providesGoogleAuthenticator(FirebaseWrapper firebaseWrapper) {
        return new GoogleAuthenticator(firebaseWrapper);
    }

    @Provides
    @ApplicationScope
    FirebaseWrapper providesEmailAuthenticator() {
        return new FirebaseWrapper();
    }

    @Provides
    @ApplicationScope
    Authenticators providesAuthenticators(FirebaseAuthenticator communityAuthenticator, GoogleAuthenticator googleAuthenticator) {
        List<Authenticator> authenticators = new ArrayList<>();
        authenticators.add(communityAuthenticator);
        authenticators.add(googleAuthenticator);
        return new Authenticators(authenticators);
    }
}
