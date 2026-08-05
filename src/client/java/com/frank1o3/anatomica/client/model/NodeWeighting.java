package com.frank1o3.anatomica.client.model;

import com.frank1o3.franklylib.Vec3;

import java.util.Arrays;

/**
 * Computes inverse-distance weights from a vertex's rest position to the nearest few
 * nodes of a fixed node layout (see {@code SoftbodyGridLayout}). Used once at model
 * construction time to build each {@code ModelVertex}'s {@code nodeInfluences}/
 * {@code nodeWeights} — never at render time, which is why the resulting per-frame
 * skinning ({@code NodeSkinning}) is a cheap weighted blend rather than a search.
 */
public final class NodeWeighting {

    private NodeWeighting() {
    }

    /**
     * Returns the indices of the {@code k} nearest nodes to {@code vertexRestPosition}
     * and their normalized inverse-distance weights (summing to 1), as parallel arrays
     * {@code [influences, weights]}.
     */
    public static Result nearest(Vec3 vertexRestPosition, Vec3[] nodeRestPositions, int k) {
        int n = nodeRestPositions.length;
        int count = Math.min(k, n);

        int[] bestIndices = new int[count];
        float[] bestDist = new float[count];
        Arrays.fill(bestDist, Float.MAX_VALUE);

        for (int i = 0; i < n; i++) {
            Vec3 delta = nodeRestPositions[i].subtract(vertexRestPosition);
            float dist = delta.dot(delta); // squared distance, fine for ranking

            // Insertion into the small "best so far" arrays.
            for (int slot = 0; slot < count; slot++) {
                if (dist < bestDist[slot]) {
                    for (int shift = count - 1; shift > slot; shift--) {
                        bestDist[shift] = bestDist[shift - 1];
                        bestIndices[shift] = bestIndices[shift - 1];
                    }
                    bestDist[slot] = dist;
                    bestIndices[slot] = i;
                    break;
                }
            }
        }

        float[] weights = new float[count];
        float totalWeight = 0f;
        final float epsilon = 1.0e-4f;
        for (int i = 0; i < count; i++) {
            float dist = (float) Math.sqrt(bestDist[i]);
            float weight = 1.0f / (dist + epsilon);
            weights[i] = weight;
            totalWeight += weight;
        }
        if (totalWeight > 0f) {
            for (int i = 0; i < count; i++) {
                weights[i] /= totalWeight;
            }
        }

        return new Result(bestIndices, weights);
    }

    public record Result(int[] influences, float[] weights) {
    }
}
