package com.frank1o3.anatomica.client.model;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.physics.SoftbodyGridLayout;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A subdivided-cuboid mesh (a grid of quads per face rather than one quad per face),
 * producing a visibly smoother deformation than {@link BoxDeformableModel} because
 * many more vertices each blend a weighted average of nearby physics nodes instead of
 * every corner following exactly one dominant node.
 *
 * <p>
 * Goes through the exact same {@code NodeSkinning} path as every other model — the
 * only difference from {@link BoxDeformableModel} is vertex density and influence
 * count per vertex, not a different deformation mechanism.
 */
public final class OrganicMeshDeformableModel implements IDeformableModel {

    private static final int SUBDIVISIONS_PER_FACE = 6;
    private static final int INFLUENCES_PER_VERTEX = 4;
    private static final Identifier ID = Anatomica.id("organic");

    private final ModelVertex[] vertices;
    private final int[] indices;

    public OrganicMeshDeformableModel() {
        SoftbodyGridLayout.Layout layout = SoftbodyGridLayout.build();
        Vec3[] nodeRest = layout.restPositions();

        float hx = SoftbodyGridLayout.HALF_WIDTH;
        float hy = SoftbodyGridLayout.HALF_HEIGHT;
        float minZ = -SoftbodyGridLayout.DEPTH * 0.5f;
        float maxZ = SoftbodyGridLayout.DEPTH * 0.5f;

        List<Vec3> positions = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        int steps = SUBDIVISIONS_PER_FACE;
        int vertsPerRow = steps + 1;

        for (int face = 0; face < 6; face++) {
            int base = positions.size();
            for (int y = 0; y < vertsPerRow; y++) {
                float fv = (float) y / steps;
                for (int x = 0; x < vertsPerRow; x++) {
                    float fu = (float) x / steps;
                    Vec3 pos = faceVertex(face, fu, fv, hx, hy, minZ, maxZ);
                    positions.add(pos);
                    uvs.add(new float[] { fu, fv });
                }
            }
            for (int y = 0; y < steps; y++) {
                for (int x = 0; x < steps; x++) {
                    int a = base + y * vertsPerRow + x;
                    int b = base + y * vertsPerRow + x + 1;
                    int c = base + (y + 1) * vertsPerRow + x;
                    int d = base + (y + 1) * vertsPerRow + x + 1;
                    indexList.add(a);
                    indexList.add(b);
                    indexList.add(c);
                    indexList.add(b);
                    indexList.add(d);
                    indexList.add(c);
                }
            }
        }

        vertices = new ModelVertex[positions.size()];
        for (int i = 0; i < vertices.length; i++) {
            Vec3 pos = positions.get(i);
            float[] uv = uvs.get(i);
            NodeWeighting.Result weighting = NodeWeighting.nearest(pos, nodeRest, INFLUENCES_PER_VERTEX);
            vertices[i] = new ModelVertex(pos, uv[0], uv[1], weighting.influences(), weighting.weights());
        }

        indices = indexList.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Maps a face index (0..5) + a [0,1]x[0,1] parametrization to a world position on
     * that face of the box. Same general parametrized-grid-per-face approach as any
     * subdivided-cuboid generator.
     */
    private static Vec3 faceVertex(int face, float u, float v, float hx, float hy, float minZ, float maxZ) {
        float xAcrossU = -hx + u * (hx * 2f);
        float yAcrossU = -hy + u * (hy * 2f);
        float yAcrossV = -hy + v * (hy * 2f);
        float zAcrossV = lerpZ(v, minZ, maxZ);
        return switch (face) {
            case 0 -> new Vec3(xAcrossU, yAcrossV, minZ);          // back
            case 1 -> new Vec3(xAcrossU, yAcrossV, maxZ);          // front
            case 2 -> new Vec3(-hx, yAcrossU, zAcrossV);           // left
            case 3 -> new Vec3(hx, yAcrossU, zAcrossV);            // right
            case 4 -> new Vec3(xAcrossU, -hy, zAcrossV);           // bottom
            default -> new Vec3(xAcrossU, hy, zAcrossV);           // top
        };
    }

    private static float lerpZ(float t, float minZ, float maxZ) {
        return minZ + t * (maxZ - minZ);
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
        return Component.translatable("model.anatomica.organic");
    }
}
