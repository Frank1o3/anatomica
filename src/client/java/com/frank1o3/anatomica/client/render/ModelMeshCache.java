package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.franklylib.Mesh;
import com.frank1o3.franklylib.MeshVertex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelMeshCache {

    private static final int TEXTURE_SIZE = 64;

    public record TextureRegion(int x1, int y1, int x2, int y2) {
        public static final TextureRegion FULL = new TextureRegion(0, 0, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private record Key(IDeformableModel model, TextureRegion region) {
    }

    private static final Map<Key, Mesh> CACHE = new ConcurrentHashMap<>();

    private ModelMeshCache() {
    }

    public static Mesh get(IDeformableModel model, TextureRegion region) {
        return CACHE.computeIfAbsent(new Key(model, region), key -> build(key.model(), key.region()));
    }

    private static Mesh build(IDeformableModel model, TextureRegion region) {
        ModelVertex[] source = model.baseVertices();
        MeshVertex[] meshVertices = new MeshVertex[source.length];
        float rw = region.x2() - region.x1();
        float rh = region.y2() - region.y1();
        for (int i = 0; i < source.length; i++) {
            ModelVertex v = source[i];
            float u = (region.x1() + v.u() * rw) / TEXTURE_SIZE;
            float vv = (region.y1() + v.v() * rh) / TEXTURE_SIZE;
            meshVertices[i] = new MeshVertex(v.restPosition(), u, vv);
        }
        return Mesh.of(meshVertices, model.indices());
    }
}