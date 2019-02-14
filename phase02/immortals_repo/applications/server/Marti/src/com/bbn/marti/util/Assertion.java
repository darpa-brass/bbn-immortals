package com.bbn.marti.util;

/**
 * A simple class for asserting things
 *
 * @author simon chase
 */
public class Assertion {
    public static void condition(boolean toAssert) {
        condition(toAssert, "");
    }

    public static void condition(boolean toAssert, String msg) {
        if (!toAssert)
            throw new AssertionException("Assertion.condition failure: " + msg);
    }

    public static void fail() {
        fail("");
    }

    public static void fail(String msg) {
        throw new AssertionException("Assertion.fail failure: " + msg);
    }

    public static void notNull(Object value, String msg) {
        if (value == null)
            throw new AssertionException("Assertion.notNull failure: " + msg);
    }

    public static void notNull(Object value) {
        notNull(value, "");
    }

    public static void isNull(Object value) {
        if (value != null)
            throw new AssertionException("Assertion.null failure");
    }

    public static void zero(int zero) {
        if (zero != 0)
            throw new AssertionException("Assertion.zero failure");
    }

    public static void same(int i1, int i2) {
        if (i1 != i2) {
            throw new AssertionException(
                    String.format("Assertion.same failure: int 1 (%d) != int 2 (%d)", i1, i2)
            );
        }
    }

    public static class AssertionException extends RuntimeException {
        public AssertionException(String msg) {
            super(msg);
        }
    }
}