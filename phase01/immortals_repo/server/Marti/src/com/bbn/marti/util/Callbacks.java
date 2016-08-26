package com.bbn.marti.util;

public interface Callbacks<T> {
    void success(T info);

    void error(String reason, Throwable trace);
}
