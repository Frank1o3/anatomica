package com.frank1o3.anatomica.client.model;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.physics.SoftbodyGridLayout;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * A smooth, chest-anchored breast profile for the soft-body engine.
 *
 * <p>
 * Unlike {@link RoundedBreastDeformableModel} (a symmetric flattened dome),
 * this
 * model biases the projection peak below the geometric center and stretches the
 * lower
 * half of the falloff further than the upper half, so the silhouette reads as a
 * teardrop rather than a hemisphere.
 *
 * <p>
 * The whole surface still uses the front UV rectangle — a non-box shape doesn't
 * get pretend rectangular side faces; the usual front torso texture (including
 * the
 * player's own clothing layer, since this mod renders over whatever the player
 * is
 * wearing) stays continuous over the curved surface. No separate nipple/areola
 * geometry is generated — that detail is left entirely to the skin texture
 * itself.
 */
public final class BreastDeformableModel implements IDeformableModel {
    private static final int SUBDIVISIONS = 24;
    private static final int INFLUENCES_PER_VERTEX = 8;
    private static final Identifier ID = Anatomica.id("breast");

    /**
     * How far below dead-center (in normalized [-1,1] space) the mound's peak sits.
     */
    private static final float VERTICAL_BIAS = 0.18f;
    /**
     * Falloff reach above the biased center. Larger = the surface stays projected
     * further before going flush with the chest (fuller); smaller = it tapers off
     * sooner (flatter). Divides {@code biasedNy}, so this is a "reach" value, not a
     * multiplier — do not flip this back to multiplication without also flipping
     * which value is larger, or the flush-point direction inverts (see the bug this
     * replaced: multiplying by a *larger* lower value made the bottom taper off
     * *sooner*, cutting it short instead of extending it).
     */
    private static final float UPPER_REACH = 0.9f;
    /**
     * Falloff reach below the biased center — larger than {@link #UPPER_REACH} so
     * the underside projects further before going flush, giving a fuller lower
     * curve.
     */
    private static final float LOWER_REACH = 1.05f;
    private static final float HORIZONTAL_REACH = 1.1f; // 1.0 = current width; <1 narrower/close-set, >1 wider/side-set
    private static final float CONE_MIX = 0.15f; // 0 = fully rounded dome, 1 = fully tapered/conical profile

    private final ModelVertex[] vertices;
    private final int[] indices;

    public BreastDeformableModel() {
        SoftbodyGridLayout.Layout layout = SoftbodyGridLayout.build();
        Vec3[] nodeRest = layout.restPositions();

        List<ModelVertex> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        for (int row = 0; row <= SUBDIVISIONS; row++) {
            float v = (float) row / SUBDIVISIONS;
            float y = -SoftbodyGridLayout.HALF_HEIGHT + v * SoftbodyGridLayout.HALF_HEIGHT * 2f;
            for (int column = 0; column <= SUBDIVISIONS; column++) {
                float u = (float) column / SUBDIVISIONS;
                float x = -SoftbodyGridLayout.HALF_WIDTH + u * SoftbodyGridLayout.HALF_WIDTH * 2f;

                float nx = x / SoftbodyGridLayout.HALF_WIDTH;
                float ny = y / SoftbodyGridLayout.HALF_HEIGHT;

                float biasedNy = ny - VERTICAL_BIAS;
                float reach = biasedNy >= 0f ? UPPER_REACH : LOWER_REACH;
                float scaledNy = biasedNy / reach;

                float scaledNx = nx / HORIZONTAL_REACH;
                float radialSquared = Math.min(1f,
                        scaledNx * scaledNx * 1.1f + // slightly wider horizontally
                                scaledNy * scaledNy * 0.9f // slightly compressed vertically
                );
                float roundedFactor = smoothFalloff(1f - radialSquared);
                float conicalFactor = Mth.clamp(1f - (float) Math.sqrt(radialSquared), 0f, 1f);
                float depthFactor = roundedFactor * (1f - CONE_MIX) + conicalFactor * CONE_MIX * conicalFactor;
                float gravity = Mth.clamp((ny + 1f) * 0.5f, 0f, 1f); // 0 bottom → 1 top
                depthFactor *= Mth.lerp(1.1f, 0.85f, gravity);

                float z = -SoftbodyGridLayout.DEPTH * depthFactor;
                Vec3 position = new Vec3(x, y, z);

                // v drives the mesh's actual Y position above and must stay as-is for
                // geometry. The texture V coordinate is a separate concern — inverted
                // here so the neck/collarbone area of the UV quad lands at the top of
                // the mesh (near the attachment point) instead of the bottom.
                float textureV = 1f - v;

                NodeWeighting.Result weighting = NodeWeighting.nearest(position, nodeRest,
                        INFLUENCES_PER_VERTEX);
                vertexList.add(new ModelVertex(position, u, textureV, UVDirection.NORTH,
                        weighting.influences(), weighting.weights()));
            }
        }

        int rowWidth = SUBDIVISIONS + 1;
        for (int row = 0; row < SUBDIVISIONS; row++) {
            for (int column = 0; column < SUBDIVISIONS; column++) {
                int a = row * rowWidth + column;
                int b = a + 1;
                int c = a + rowWidth;
                int d = c + 1;
                // The surface faces toward negative Z, so reverse the usual
                // XY-grid winding to produce outward-facing normals.
                indexList.add(a);
                indexList.add(c);
                indexList.add(b);
                indexList.add(b);
                indexList.add(c);
                indexList.add(d);
            }
        }

        vertices = vertexList.toArray(new ModelVertex[0]);
        indices = indexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static float smoothFalloff(float t) {
        // t in [0,1]: 0 at the flush boundary, 1 at the peak. Using t*t*(3-2t)
        // (cubic smoothstep) instead of Math.pow(t, exponent<1) gives zero slope
        // at both ends — the old pow(t, 0.55) had an unbounded slope at t=0 (the
        // flush boundary), which pinched triangles into near-zero-area folds
        // right at that ring, especially near the vertically-biased apex where
        // the boundary is crossed at a steeper angle. This trades a slightly
        // rounder peak for a boundary that closes cleanly.
        return t * t * (3f - 2f * t);
    }

    @Override
    public ModelVertex[] baseVertices() {
        return vertices;
    }

    @Override
    public int[] indices() {
        return indices;
    }

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public Component displayName() {
        return Component.translatable("model.anatomica.breast");
    }
}