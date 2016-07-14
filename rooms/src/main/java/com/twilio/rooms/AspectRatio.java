package com.twilio.rooms;

/**
 *  Rational aspect ratio represented as numerator:denominator
 *  For x:y aspect ratio you can set numerater to x and denominator to y
 */
public class AspectRatio {
    public final int numerator;
    public final int denominator;

    public AspectRatio(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
}
