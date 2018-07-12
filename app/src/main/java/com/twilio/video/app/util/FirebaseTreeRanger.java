/*
 * Copyright (C) 2017 Twilio, Inc.
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

package com.twilio.video.app.util;

import com.google.firebase.crash.FirebaseCrash;
import javax.inject.Inject;

public class FirebaseTreeRanger implements TreeRanger {

    @Inject
    public FirebaseTreeRanger() {}

    @Override
    public void inform(String message) {
        // No inform implementation for now. We could potentially use Firebase Analytics
    }

    @Override
    public void caution(String message) {
        FirebaseCrash.log(message);
    }

    @Override
    public void alert(Throwable throwable) {
        FirebaseCrash.report(throwable);
    }
}
