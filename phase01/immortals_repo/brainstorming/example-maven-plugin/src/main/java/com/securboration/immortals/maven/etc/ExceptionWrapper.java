package com.securboration.immortals.maven.etc;


/**
 * Lambda-friendly wrapper for dealing with Exception subclasses
 * @author jstaples
 *
 */
public class ExceptionWrapper {
    public static void wrap(Wrappable w) {
        try {
            w.f();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static interface Wrappable {
        public void f() throws Exception;
    }
}
