package com.twilio.video;

/**
 *  Rational aspect ratio represented as numerator:denominator.
 *  For x:y aspect ratio you can set numerator to x and denominator to y.
 */
public class AspectRatio {
    public final int numerator;
    public final int denominator;

    public AspectRatio(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AspectRatio that = (AspectRatio) o;

        if (numerator != that.numerator) return false;
        return denominator == that.denominator;

    }

    @Override
    public int hashCode() {
        int result = numerator;
        result = 31 * result + denominator;
        return result;
    }
}
