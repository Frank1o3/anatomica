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
 * A smooth, chest-anchored breast profile for the soft-body engine. Unlike the
 * box and organic models, its visible surface is a rounded dome that reaches
 * the chest at its perimeter rather than retaining cuboid side walls.
 *
 * <p>
 * The whole surface uses the front UV rectangle. That intentionally avoids
 * pretending a non-box shape has separate rectangular side faces; the usual
 * front torso texture remains continuous over the curved surface.
 * </p>
 */
public final class BreastDeformableModel implements IDeformableModel {

    private static final int SUBDIVISIONS = 14;
    private static final int INFLUENCES_PER_VERTEX = 8;
    private static final Identifier ID = Anatomica.id("breast");

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
                float radialSquared = Math.min(1f, nx * nx + ny * ny);
                // A softened hemisphere: the centre has full projection while
                // the edge meets the chest anchor smoothly.
                float depthFactor = (float) Math.pow(1f - radialSquared, 0.55f);
                Vec3 position = new Vec3(x, y, -SoftbodyGridLayout.DEPTH * depthFactor);

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
