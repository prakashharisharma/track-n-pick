package com.example.util;

import java.util.Random;

public class ThreadsUtil {

    private static int min = 837;
    private static int max = 1897;

    public static int poolSize() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void delay(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    public static void delay() throws InterruptedException {
        long interval = getInterval();
        Thread.sleep(interval);
    }

    public static long getInterval() {

        return new Random().nextInt(max - min + 1) + min;
    }
}
