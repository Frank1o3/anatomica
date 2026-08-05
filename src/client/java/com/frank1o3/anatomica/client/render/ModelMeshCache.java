package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.franklylib.Mesh;
import com.frank1o3.franklylib.MeshVertex;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Builds and caches the FranklyLib {@link Mesh} (rest-pose vertices + triangle
 * indices) for a given {@link IDeformableModel}, so the vertex/UV arrays aren't
 * rebuilt every frame. The physics-node influence weights that live on
 * {@link ModelVertex} stay on the Anatomica side — FranklyLib's {@code Mesh} only
 * needs position/UV, since deformation itself is handled by
 * {@link com.frank1o3.anatomica.model.NodeSkinning} via {@link BoundMeshDeformer}, not
 * by anything FranklyLib knows about.
 */
public final class ModelMeshCache {

    private static final Map<IDeformableModel, Mesh> CACHE = new WeakHashMap<>();

    private ModelMeshCache() {
    }

    public static synchronized Mesh get(IDeformableModel model) {
        return CACHE.computeIfAbsent(model, ModelMeshCache::build);
    }

    private static Mesh build(IDeformableModel model) {
        ModelVertex[] source = model.baseVertices();
        MeshVertex[] meshVertices = new MeshVertex[source.length];
        for (int i = 0; i < source.length; i++) {
            ModelVertex v = source[i];
            meshVertices[i] = new MeshVertex(v.restPosition(), v.u(), v.v());
        }
        return Mesh.of(meshVertices, model.indices());
    }
}
