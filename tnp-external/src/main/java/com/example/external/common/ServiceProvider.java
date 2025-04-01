package com.example.external.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum ServiceProvider {
    NDTV,
    SCREENER,
    REDIFF,
    NSE,
    MYSTOCKS,
    STORAGE;

    private static final List<ServiceProvider> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    private static final int SIZE = VALUES.size();

    private static final Random RANDOM = new Random();

    public static ServiceProvider randomServiceProvider() {

        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
