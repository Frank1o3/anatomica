package com.frank1o3.anatomica.model;

import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.franklylib.Vec3;

/**
 * A single vertex of an {@link IDeformableModel}'s rest-pose mesh, pre-tagged
 * with which physics nodes influence it and by how much, plus optional face direction for UV remapping.
 */
public record ModelVertex(Vec3 restPosition, float u, float v, UVDirection direction, int[] nodeInfluences, float[] nodeWeights) {
    public ModelVertex {
        if (nodeInfluences.length != nodeWeights.length) {
            throw new IllegalArgumentException("nodeInfluences and nodeWeights must be the same length");
        }
    }

    public ModelVertex(Vec3 restPosition, float u, float v, int[] nodeInfluences, float[] nodeWeights) {
        this(restPosition, u, v, null, nodeInfluences, nodeWeights);
    }
}

