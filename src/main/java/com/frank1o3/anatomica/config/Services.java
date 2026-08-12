package com.frank1o3.anatomica.config;

public final class Services {

    private static IBodyConfigSerializer bodyConfigSerializer;

    private Services() {
    }

    public static void registerBodyConfigSerializer(IBodyConfigSerializer serializer) {
        if (bodyConfigSerializer != null) {
            throw new IllegalStateException("BodyConfigSerializer already registered.");
        }

        bodyConfigSerializer = serializer;
    }

    public static IBodyConfigSerializer bodyConfigSerializer() {
        if (bodyConfigSerializer == null) {
            throw new IllegalStateException("No BodyConfigSerializer registered.");
        }

        return bodyConfigSerializer;
    }

}
