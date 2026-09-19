package com.frank1o3.anatomica.client.physics;

/**
 * Per-tetrahedron, signed-volume constraints for the soft-body node grid.
 *
 * <p>
 * Replaces the old "measure the whole grid's unsigned volume, then nudge every
 * free node's Z by a heuristic" approach. Each hex cell of the grid is split
 * into 5 tetrahedra; each tetrahedron gets its own constraint
 * {@code C = V - V0} where {@code V} is the <b>signed</b> volume and {@code V0}
 * the signed volume in the current target pose. The correction is the exact
 * (linearised) projection along the volume gradient, weighted by inverse mass,
 * so:
 * <ul>
 * <li>fixed (anchor) nodes never move, everything else absorbs the correction;</li>
 * <li>an inverted tetrahedron has the wrong sign and is pushed back out instead
 * of being counted as positive volume (the old {@code Math.abs} could not tell);</li>
 * <li>the correction acts in all directions, so it cooperates with the edge
 * constraints instead of fighting them along a single axis.</li>
 * </ul>
 *
 * <p>
 * No Minecraft dependencies, so it can be unit tested directly.
 */
public final class TetVolumeConstraints {

    private static final float GRADIENT_EPSILON = 1.0e-12f;
    /** Largest displacement (model units) one tet may apply to one node per solve call. */
    private static final float MAX_STEP = 0.02f;

    private final int count;
    /** 4 node indices per tetrahedron. */
    private final int[] tets;
    /** Signed target volume per tetrahedron. */
    private final float[] targetVolume;

    /** @param hexCells 8 node indices per cell, ordered as in {@code buildVolumeCells()}. */
    public TetVolumeConstraints(int[][] hexCells) {
        this.count = hexCells.length * 5;
        this.tets = new int[count * 4];
        this.targetVolume = new float[count];
        int t = 0;
        for (int[] c : hexCells) {
            t = put(t, c[0], c[1], c[3], c[4]);
            t = put(t, c[1], c[2], c[3], c[6]);
            t = put(t, c[1], c[3], c[4], c[6]);
            t = put(t, c[1], c[4], c[5], c[6]);
            t = put(t, c[3], c[4], c[6], c[7]);
        }
    }

    private int put(int t, int a, int b, int c, int d) {
        int o = t * 4;
        tets[o] = a;
        tets[o + 1] = b;
        tets[o + 2] = c;
        tets[o + 3] = d;
        return t + 1;
    }

    /** Sets every tetrahedron's target volume to its signed volume in this pose. Call when the pose changes. */
    public void setTargetPose(float[] x, float[] y, float[] z) {
        for (int t = 0; t < count; t++) {
            int o = t * 4;
            targetVolume[t] = signedVolume(x, y, z, tets[o], tets[o + 1], tets[o + 2], tets[o + 3]);
        }
    }

    /** Sum of signed tet volumes (debug / tests). Inversions reduce this instead of inflating it. */
    public float totalVolume(float[] x, float[] y, float[] z) {
        float sum = 0f;
        for (int t = 0; t < count; t++) {
            int o = t * 4;
            sum += signedVolume(x, y, z, tets[o], tets[o + 1], tets[o + 2], tets[o + 3]);
        }
        return sum;
    }

    public float totalTargetVolume() {
        float sum = 0f;
        for (float v : targetVolume) {
            sum += v;
        }
        return sum;
    }

    /**
     * One Gauss-Seidel pass over all tetrahedra.
     *
     * @param invMass   inverse mass per node, 0 for fixed nodes
     * @param stiffness 0..1 fraction of the error removed this pass (1 = fully satisfied per tet)
     */
    public void solve(float[] x, float[] y, float[] z, float[] invMass, float stiffness) {
        if (stiffness <= 0f) {
            return;
        }
        float k = Math.min(stiffness, 1f);

        for (int t = 0; t < count; t++) {
            int o = t * 4;
            int a = tets[o], b = tets[o + 1], c = tets[o + 2], d = tets[o + 3];
            float wa = invMass[a], wb = invMass[b], wc = invMass[c], wd = invMass[d];
            if (wa + wb + wc + wd <= 0f) {
                continue;
            }

            float e1x = x[b] - x[a], e1y = y[b] - y[a], e1z = z[b] - z[a];
            float e2x = x[c] - x[a], e2y = y[c] - y[a], e2z = z[c] - z[a];
            float e3x = x[d] - x[a], e3y = y[d] - y[a], e3z = z[d] - z[a];

            // Gradients of V = e1 . (e2 x e3) / 6 w.r.t. b, c, d (a is minus their sum).
            final float s = 1f / 6f;
            float gbx = (e2y * e3z - e2z * e3y) * s;
            float gby = (e2z * e3x - e2x * e3z) * s;
            float gbz = (e2x * e3y - e2y * e3x) * s;
            float gcx = (e3y * e1z - e3z * e1y) * s;
            float gcy = (e3z * e1x - e3x * e1z) * s;
            float gcz = (e3x * e1y - e3y * e1x) * s;
            float gdx = (e1y * e2z - e1z * e2y) * s;
            float gdy = (e1z * e2x - e1x * e2z) * s;
            float gdz = (e1x * e2y - e1y * e2x) * s;
            float gax = -(gbx + gcx + gdx);
            float gay = -(gby + gcy + gdy);
            float gaz = -(gbz + gcz + gdz);

            float volume = e1x * gbx + e1y * gby + e1z * gbz;

            float ga2 = gax * gax + gay * gay + gaz * gaz;
            float gb2 = gbx * gbx + gby * gby + gbz * gbz;
            float gc2 = gcx * gcx + gcy * gcy + gcz * gcz;
            float gd2 = gdx * gdx + gdy * gdy + gdz * gdz;
            float denominator = wa * ga2 + wb * gb2 + wc * gc2 + wd * gd2;
            if (denominator < GRADIENT_EPSILON) {
                continue; // fully collapsed tet: gradient carries no direction
            }

            float lambda = -k * (volume - targetVolume[t]) / denominator;

            // Safety: cap the largest per-node displacement so an inverted or
            // badly stretched tet cannot teleport nodes.
            float m = Math.max(
                    Math.max(wa * (float) Math.sqrt(ga2), wb * (float) Math.sqrt(gb2)),
                    Math.max(wc * (float) Math.sqrt(gc2), wd * (float) Math.sqrt(gd2)));
            float maxDisplacement = Math.abs(lambda) * m;
            if (maxDisplacement > MAX_STEP) {
                lambda *= MAX_STEP / maxDisplacement;
            }

            x[a] += wa * lambda * gax;
            y[a] += wa * lambda * gay;
            z[a] += wa * lambda * gaz;
            x[b] += wb * lambda * gbx;
            y[b] += wb * lambda * gby;
            z[b] += wb * lambda * gbz;
            x[c] += wc * lambda * gcx;
            y[c] += wc * lambda * gcy;
            z[c] += wc * lambda * gcz;
            x[d] += wd * lambda * gdx;
            y[d] += wd * lambda * gdy;
            z[d] += wd * lambda * gdz;
        }
    }

    private static float signedVolume(float[] x, float[] y, float[] z, int a, int b, int c, int d) {
        float e1x = x[b] - x[a], e1y = y[b] - y[a], e1z = z[b] - z[a];
        float e2x = x[c] - x[a], e2y = y[c] - y[a], e2z = z[c] - z[a];
        float e3x = x[d] - x[a], e3y = y[d] - y[a], e3z = z[d] - z[a];
        return (e1x * (e2y * e3z - e2z * e3y)
                + e1y * (e2z * e3x - e2x * e3z)
                + e1z * (e2x * e3y - e2y * e3x)) / 6f;
    }
}
