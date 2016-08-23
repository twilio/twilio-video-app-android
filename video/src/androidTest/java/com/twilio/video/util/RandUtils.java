package com.twilio.video.util;

import java.util.Random;

public class RandUtils {
    private static final char[] symbols;
    static {
        StringBuilder symbolsBuilder = new StringBuilder();
        for (char ch = '0' ; ch <= '9' ; ++ch) {
            symbolsBuilder.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            symbolsBuilder.append(ch);
        }
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            symbolsBuilder.append(ch);
        }
        symbols = symbolsBuilder.toString().toCharArray();
    }

    public static String generateRandomString(int length) {
        char[] randomSymbolBuffer = new char[length];
        Random random = new Random();

        for (int i = 0 ; i < randomSymbolBuffer.length ; i++) {
            randomSymbolBuffer[i] = symbols[random.nextInt(symbols.length)];
        }

        return new String(randomSymbolBuffer);
    }
}
