package com.frank1o3.anatomica.model;

import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.franklylib.Vec3;

/**
 * Blends a single {@link ModelVertex}'s rest position against the current state
 * of a
 * driving {@link IPhysicsEngine}, using the vertex's precomputed node influence
 * weights.
 *
 * <p>
 * This is the one and only vertex-skinning code path in Anatomica: every
 * {@link IDeformableModel}, including the simplest box model, is skinned
 * through this
 * exact method. There is no separate "legacy" or "static" fallback path — a
 * model with
 * no meaningful physics coupling simply has all its vertices weighted toward
 * whichever
 * node(s) are nearest, degenerating naturally rather than needing special-case
 * code.
 *
 * <p>
 * Deliberately common-side: this is plain vector math over
 * {@link IPhysicsEngine},
 * with no rendering or client dependency, so it can be unit tested without a
 * running
 * client.
 */
public final class NodeSkinning {

    private NodeSkinning() {
    }

    public static Vec3 skin(ModelVertex vertex, IPhysicsEngine engine) {
        int[] influences = vertex.nodeInfluences();
        float[] weights = vertex.nodeWeights();

        Vec3 accumulatedDelta = Vec3.ZERO;
        float totalWeight = 0f;

        for (int k = 0; k < influences.length; k++) {
            int node = influences[k];
            float weight = weights[k];
            if (weight <= 0f) {
                continue;
            }
            Vec3 delta = engine.interpolatedNodePosition(node).subtract(engine.nodeRestPosition(node));
            accumulatedDelta = accumulatedDelta.add(delta.scale(weight));
            totalWeight += weight;
        }

        if (totalWeight <= 0f) {
            return vertex.restPosition();
        }
        return vertex.restPosition().add(accumulatedDelta.scale(1f / totalWeight));
    }

    /** Skins every vertex of {@code vertices} against {@code engine}, in order. */
    public static Vec3[] skinAll(ModelVertex[] vertices, IPhysicsEngine engine) {
        Vec3[] out = new Vec3[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            out[i] = skin(vertices[i], engine);
        }
        return out;
    }
}
