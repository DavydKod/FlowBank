package com.davyd.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ConcurrencyTestUtils {

    private ConcurrencyTestUtils() {
    }

    public static void awaitOrThrow(
            CountDownLatch latch,
            String message
    ) throws InterruptedException {
        if (!latch.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException(message);
        }
    }
}