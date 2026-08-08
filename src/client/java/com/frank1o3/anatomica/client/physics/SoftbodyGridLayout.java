package com.frank1o3.anatomica.client.physics;

import com.frank1o3.franklylib.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the fixed 3x3x3 node grid every built-in soft-body attachment uses: its rest
 * positions, which nodes are pinned (the back layer, anchored to the body), and which
 * pairs of nodes are connected by a distance constraint.
 *
 * <p>
 * This is pulled out as its own utility, separate from {@link SoftbodyPhysicsEngine},
 * so that deformable models can compute their node-influence weights
 * ({@code NodeWeighting}) against the exact same rest-position layout the engine will
 * simulate, without needing a live engine instance at model-construction time — both
 * sides just call {@link #build} with the same dimensions.
 *
 * <p>
 * If a future physics engine wants a different topology, give it its own layout
 * utility and its own matching model set (or a model built generically enough to
 * accept an arbitrary layout) — this class is deliberately specific to the one
 * built-in grid shape rather than a general "any topology" abstraction.
 */
public final class SoftbodyGridLayout {

    public static final int COLS = 3;
    public static final int ROWS = 3;
    public static final int LAYERS = 3;
    public static final int NODE_COUNT = COLS * ROWS * LAYERS;

    /** Half-extent of the grid along X (left/right) and Y (up/down), in model-local units. */
    public static final float HALF_WIDTH = 0.14f;
    public static final float HALF_HEIGHT = 0.14f;
    /** Full depth of the grid along Z (back/front, back = fixed anchor layer). */
    public static final float DEPTH = 0.18f;

    public record Layout(Vec3[] restPositions, boolean[] fixed, List<int[]> constraintPairs) {
    }

    private SoftbodyGridLayout() {
    }

    public static int index(int x, int y, int z) {
        return (z * ROWS + y) * COLS + x;
    }

    public static Layout build() {
        Vec3[] rest = new Vec3[NODE_COUNT];
        boolean[] fixed = new boolean[NODE_COUNT];

        for (int z = 0; z < LAYERS; z++) {
            float fz = (float) z / (LAYERS - 1);
            float posZ = -fz * DEPTH; // z=0 layer is the back/anchor layer (at attachment surface)
            for (int y = 0; y < ROWS; y++) {
                float fy = (float) y / (ROWS - 1);
                float posY = -HALF_HEIGHT + fy * (HALF_HEIGHT * 2f);
                for (int x = 0; x < COLS; x++) {
                    float fx = (float) x / (COLS - 1);
                    float posX = -HALF_WIDTH + fx * (HALF_WIDTH * 2f);

                    int i = index(x, y, z);
                    rest[i] = new Vec3(posX, posY, posZ);
                    fixed[i] = z == 0; // back layer anchored to the body
                }
            }
        }

        List<int[]> pairs = new ArrayList<>();
        for (int z = 0; z < LAYERS; z++) {
            for (int y = 0; y < ROWS; y++) {
                for (int x = 0; x < COLS; x++) {
                    int i = index(x, y, z);
                    if (x + 1 < COLS) {
                        pairs.add(new int[] { i, index(x + 1, y, z) });
                    }
                    if (y + 1 < ROWS) {
                        pairs.add(new int[] { i, index(x, y + 1, z) });
                    }
                    if (z + 1 < LAYERS) {
                        pairs.add(new int[] { i, index(x, y, z + 1) });
                    }
                }
            }
        }

        return new Layout(rest, fixed, pairs);
    }
}
