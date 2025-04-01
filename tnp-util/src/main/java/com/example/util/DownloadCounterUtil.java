package com.example.util;

import java.util.concurrent.atomic.AtomicInteger;

public class DownloadCounterUtil {

    private static AtomicInteger counter = new AtomicInteger(0);

    public static int get() {
        return counter.get();
    }

    public static int increment() {
        return counter.incrementAndGet();
    }

    public static void reset() {
        System.out.print("Resetting counter");
        counter.set(0);
    }
}
