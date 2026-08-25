package com.frank1o3.anatomica.client;

public final class SimdSupport {
    private static final boolean AVAILABLE = ModuleLayer.boot()
            .findModule("jdk.incubator.vector")
            .isPresent();

    private SimdSupport() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }
}
