package com.frank1o3.anatomica.model;

import com.frank1o3.franklylib.Vec3;

/**
 * A single vertex of an {@link IDeformableModel}'s rest-pose mesh, pre-tagged
 * with
 * which physics nodes influence it and by how much. Weights are computed once
 * at
 * model-construction time (e.g. inverse-distance weighting to the nearest 2-4
 * physics
 * nodes in rest space) so per-frame skinning
 * ({@link com.frank1o3.anatomica.client.render.NodeSkinning})
 * is a cheap weighted blend rather than a search.
 *
 * @param restPosition   rest-pose position of this vertex, in the model's local
 *                       space
 * @param u              texture U coordinate
 * @param v              texture V coordinate
 * @param nodeInfluences indices into the driving
 *                       {@link com.frank1o3.anatomica.physics.IPhysicsEngine}'s
 *                       node list
 * @param nodeWeights    blend weight per entry in {@code nodeInfluences}, same
 *                       length/order
 */
public record ModelVertex(Vec3 restPosition, float u, float v, int[] nodeInfluences, float[] nodeWeights) {
    public ModelVertex {
        if (nodeInfluences.length != nodeWeights.length) {
            throw new IllegalArgumentException("nodeInfluences and nodeWeights must be the same length");
        }
    }
}
