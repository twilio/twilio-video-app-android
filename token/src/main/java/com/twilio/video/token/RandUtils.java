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
