package com.frank1o3.anatomica.client.model;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.physics.SoftbodyGridLayout;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A smooth, chest-anchored breast profile for the soft-body engine.
 *
 * <p>Unlike {@link RoundedBreastDeformableModel} (a symmetric flattened dome), this
 * model biases the projection peak below the geometric center and stretches the lower
 * half of the falloff further than the upper half, so the silhouette reads as a
 * teardrop rather than a hemisphere. A secondary, smaller-radius bump is layered on
 * top near the biased peak to suggest a nipple without introducing a hard seam.
 *
 * <p>The whole surface still uses the front UV rectangle — a non-box shape doesn't
 * get pretend rectangular side faces; the usual front torso texture stays continuous
 * over the curved surface.
 */
public final class BreastDeformableModel implements IDeformableModel {

    private static final int SUBDIVISIONS = 28;
    private static final int INFLUENCES_PER_VERTEX = 14;
    private static final Identifier ID = Anatomica.id("breast");

    /** How far below dead-center (in normalized [-1,1] space) the mound's peak sits. */
    private static final float VERTICAL_BIAS = 0.18f;
    /** Falloff stretch above the biased center — smaller = tapers off sooner (flatter top). */
    private static final float UPPER_FULLNESS = 0.90f;
    /** Falloff stretch below the biased center — larger = reaches further (fuller underside). */
    private static final float LOWER_FULLNESS = 1.30f;
    /** Extra forward protrusion of the nipple bump, as a fraction of DEPTH. */
    private static final float NIPPLE_HEIGHT = 0.16f;
    /** Radius (normalized) of the nipple bump's falloff — smaller = a tighter, more defined tip. */
    private static final float NIPPLE_RADIUS = 0.32f;

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

                // Bias the mound's peak below dead-center and stretch the lower half's
                // falloff further than the upper half's, so the profile reads as a
                // teardrop instead of a symmetric dome.
                float biasedNy = ny - VERTICAL_BIAS;
                float fullness = biasedNy >= 0f ? UPPER_FULLNESS : LOWER_FULLNESS;
                float scaledNy = biasedNy * fullness;

                float radialSquared = Math.min(1f, nx * nx + scaledNy * scaledNy);
                float depthFactor = (float) Math.pow(1f - radialSquared, 0.55f);

                // Nipple bump: a smaller, steeper secondary protrusion centered a bit
                // below the base mound's own peak, blended in with a smooth falloff
                // rather than a hard point so it doesn't create a visible seam.
                float dnx = nx;
                float dny = ny - VERTICAL_BIAS * 0.6f;
                float nippleDistSquared = (dnx * dnx + dny * dny) / (NIPPLE_RADIUS * NIPPLE_RADIUS);
                float nippleFactor = nippleDistSquared < 1f
                        ? (float) Math.pow(1f - nippleDistSquared, 2.0) * NIPPLE_HEIGHT
                        : 0f;

                float z = -SoftbodyGridLayout.DEPTH * depthFactor - SoftbodyGridLayout.DEPTH * nippleFactor;
                Vec3 position = new Vec3(x, y, z);

                NodeWeighting.Result weighting = NodeWeighting.nearest(position, nodeRest,
                        INFLUENCES_PER_VERTEX);
                vertexList.add(new ModelVertex(position, u, v, UVDirection.NORTH,
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