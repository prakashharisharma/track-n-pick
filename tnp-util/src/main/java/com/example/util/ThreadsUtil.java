package com.example.util;

public class ThreadsUtil {

    public static int poolSize() {
        return Runtime.getRuntime().availableProcessors();
    }
}
