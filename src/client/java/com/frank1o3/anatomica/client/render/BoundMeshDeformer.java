package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.franklylib.Mesh;
import com.frank1o3.franklylib.MeshDeformer;
import com.frank1o3.franklylib.Vec3;

/**
 * The one place Anatomica's node-skinning system meets FranklyLib's opinion-free
 * {@link MeshDeformer} contract. FranklyLib has no idea what a "physics node" is; this
 * class is what makes that true while still letting the two systems work together —
 * it just calls {@code model.deform(engine)} (which internally does the actual
 * node-skinning) each frame.
 */
public final class BoundMeshDeformer implements MeshDeformer {

    private final IDeformableModel model;
    private final IPhysicsEngine engine;
    public BoundMeshDeformer(IDeformableModel model, IPhysicsEngine engine) {
        this.model = model;
        this.engine = engine;
    }

    @Override
    public Vec3[] deform(Mesh baseMesh, float partialTick) {
        if (engine != null) {
            engine.interpolate(partialTick);
        }
        return model.deform(engine);
    }
}
