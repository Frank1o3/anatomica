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
 * A deformable cloth garment model fitting just outside and over both breast softbody grids.
 *
 * <p>
 * Unlike per-breast dome models, this model forms a unified shell covering both sides.
 * It is skinned against the cloth physics simulation and uses {@link UVDirection#NORTH}
 * for sampling the player's torso texture.
 */
public final class ClothDeformableModel implements IDeformableModel {

    private static final int SUBDIVISIONS = 16;
    private static final int INFLUENCES_PER_VERTEX = 4;
    private static final Identifier ID = Anatomica.id("cloth_basic");

    private static final float WIDTH_SCALE = 2.15f;
    private static final float HEIGHT_SCALE = 1.10f;
    private static final float DEPTH_SCALE = 1.05f;

    private final ModelVertex[] vertices;
    private final int[] indices;

    public ClothDeformableModel() {
        SoftbodyGridLayout.Layout layout = SoftbodyGridLayout.build();
        Vec3[] nodeRest = layout.restPositions();

        List<ModelVertex> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        float halfWidth = SoftbodyGridLayout.HALF_WIDTH * WIDTH_SCALE;
        float halfHeight = SoftbodyGridLayout.HALF_HEIGHT * HEIGHT_SCALE;
        float maxDepth = SoftbodyGridLayout.DEPTH * DEPTH_SCALE;

        for (int row = 0; row <= SUBDIVISIONS; row++) {
            float v = (float) row / SUBDIVISIONS;
            float y = -halfHeight + v * (halfHeight * 2f);
            for (int column = 0; column <= SUBDIVISIONS; column++) {
                float u = (float) column / SUBDIVISIONS;
                float x = -halfWidth + u * (halfWidth * 2f);

                float nx = x / halfWidth;
                float ny = y / halfHeight;

                // Smooth dome profile covering the chest area
                float radialSquared = Math.min(1.0f, nx * nx + ny * ny);
                float depthFactor = (float) Math.pow(Math.max(0.0f, 1.0f - radialSquared), 0.55f);

                float z = -maxDepth * depthFactor;
                Vec3 position = new Vec3(x, y, z);

                // Texture V inverted so top of chest aligns with top of UV quad
                float textureV = 1.0f - v;

                NodeWeighting.Result weighting = NodeWeighting.nearest(position, nodeRest, INFLUENCES_PER_VERTEX);
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
                // Outward-facing winding for surface pointing toward negative Z
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
        return Component.translatable("model.anatomica.cloth_basic");
    }
}
