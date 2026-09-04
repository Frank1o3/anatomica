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
        if (engine == null) {
            return vertex.restPosition();
        }
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
        if (engine == null) {
            Vec3[] out = new Vec3[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                out[i] = vertices[i].restPosition();
            }
            return out;
        }

        int count = engine.nodeCount();
        Vec3[] nodeDeltas = new Vec3[count];
        for (int i = 0; i < count; i++) {
            nodeDeltas[i] = engine.interpolatedNodePosition(i).subtract(engine.nodeRestPosition(i));
        }

        Vec3[] out = new Vec3[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            ModelVertex vertex = vertices[i];
            int[] influences = vertex.nodeInfluences();
            float[] weights = vertex.nodeWeights();

            float totalDeltaX = 0f;
            float totalDeltaY = 0f;
            float totalDeltaZ = 0f;
            float totalWeight = 0f;

            for (int k = 0; k < influences.length; k++) {
                int node = influences[k];
                float weight = weights[k];
                if (weight <= 0f || node < 0 || node >= count) {
                    continue;
                }
                Vec3 d = nodeDeltas[node];
                totalDeltaX += d.x() * weight;
                totalDeltaY += d.y() * weight;
                totalDeltaZ += d.z() * weight;
                totalWeight += weight;
            }

            if (totalWeight <= 0f) {
                out[i] = vertex.restPosition();
            } else {
                float invWeight = 1f / totalWeight;
                Vec3 rest = vertex.restPosition();
                out[i] = new Vec3(rest.x() + totalDeltaX * invWeight,
                        rest.y() + totalDeltaY * invWeight,
                        rest.z() + totalDeltaZ * invWeight);
            }
        }
        return out;
    }
}
