package com.frank1o3.anatomica.config;

import com.frank1o3.anatomica.data.IEntityBodyData;

public final class Services {

    private static IBodyConfigSerializer bodyConfigSerializer;
    private static IEntityBodyData entityBodyData;

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

    public static void registerEntityBodyData(IEntityBodyData storage) {
        if (entityBodyData != null) {
            throw new IllegalStateException("EntityBodyData already registered.");
        }

        entityBodyData = storage;
    }

    public static IEntityBodyData entityBodyData() {
        if (entityBodyData == null) {
            throw new IllegalStateException("No EntityBodyData registered.");
        }

        return entityBodyData;
    }
}