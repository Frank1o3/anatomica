package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
import com.frank1o3.franklylib.Mesh;
import com.frank1o3.franklylib.MeshVertex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelMeshCache {

    private static final float TEXTURE_SIZE = 64.0f;

    private record Key(IDeformableModel model, UVLayout layout) {
    }

    private static final Map<Key, Mesh> CACHE = new ConcurrentHashMap<>();

    private ModelMeshCache() {
    }

    public static Mesh get(IDeformableModel model, UVLayout layout) {
        return CACHE.computeIfAbsent(new Key(model, layout), key -> build(key.model(), key.layout()));
    }

    /** Invalidates meshes after client resources have reloaded. */
    public static void clear() {
        CACHE.clear();
    }

    private static Mesh build(IDeformableModel model, UVLayout layout) {
        ModelVertex[] source = model.baseVertices();
        MeshVertex[] meshVertices = new MeshVertex[source.length];
        for (int i = 0; i < source.length; i++) {
            ModelVertex v = source[i];
            UVDirection dir = v.direction();
            UVQuad quad = (dir != null && layout != null) ? layout.get(dir) : null;
            float u, vv;
            if (quad != null) {
                float qw = quad.x2() - quad.x1();
                float qh = quad.y2() - quad.y1();
                u = (quad.x1() + v.u() * qw) / TEXTURE_SIZE;
                vv = (quad.y2() - v.v() * qh) / TEXTURE_SIZE;
            } else {
                u = v.u();
                vv = v.v();
            }
            meshVertices[i] = new MeshVertex(v.restPosition(), u, vv);
        }
        return Mesh.of(meshVertices, model.indices());
    }
}
