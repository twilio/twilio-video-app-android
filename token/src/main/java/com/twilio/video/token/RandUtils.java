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

package com.twilio.video.token;

import java.util.Random;

class RandUtils {
    private static final char[] symbols;
    static {
        StringBuilder symbolsBuilder = new StringBuilder();
        for (char symbol = '0' ; symbol <= '9' ; symbol++) {
            symbolsBuilder.append(symbol);
        }
        for (char symbol = 'a' ; symbol <= 'z' ; symbol++) {
            symbolsBuilder.append(symbol);
        }
        for (char symbol = 'A' ; symbol <= 'Z' ; symbol++) {
            symbolsBuilder.append(symbol);
        }
        symbols = symbolsBuilder.toString().toCharArray();
    }

    static String generateRandomString(int length) {
        char[] randomSymbolBuffer = new char[length];
        Random random = new Random();

        for (int i = 0 ; i < randomSymbolBuffer.length ; i++) {
            randomSymbolBuffer[i] = symbols[random.nextInt(symbols.length)];
        }

        return new String(randomSymbolBuffer);
    }
}
