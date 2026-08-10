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
 * A subdivided-cuboid mesh (a grid of quads per face rather than one quad per
 * face),
 * producing a visibly smoother deformation than {@link BoxDeformableModel}.
 * Each face's vertices are pre-tagged with their corresponding
 * {@link UVDirection}.
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
        float minZ = 0.0f;
        float maxZ = -SoftbodyGridLayout.DEPTH;

        List<Vec3> positions = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<UVDirection> directions = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        int steps = SUBDIVISIONS_PER_FACE;
        int vertsPerRow = steps + 1;

        for (int face = 0; face < 6; face++) {
            int base = positions.size();
            UVDirection dir = faceDirection(face);
            for (int y = 0; y < vertsPerRow; y++) {
                float fv = (float) y / steps;
                for (int x = 0; x < vertsPerRow; x++) {
                    float fu = (float) x / steps;
                    Vec3 pos = faceVertex(face, fu, fv, hx, hy, minZ, maxZ);
                    positions.add(pos);
                    uvs.add(new float[] { fu, fv });
                    directions.add(dir);
                }
            }
            for (int y = 0; y < steps; y++) {
                for (int x = 0; x < steps; x++) {
                    int a = base + y * vertsPerRow + x;
                    int b = base + y * vertsPerRow + x + 1;
                    int c = base + (y + 1) * vertsPerRow + x;
                    int d = base + (y + 1) * vertsPerRow + x + 1;
                    if (faceHasReversedWinding(face)) {
                        indexList.add(a);
                        indexList.add(c);
                        indexList.add(b);
                        indexList.add(b);
                        indexList.add(c);
                        indexList.add(d);
                    } else {
                        indexList.add(a);
                        indexList.add(b);
                        indexList.add(c);
                        indexList.add(b);
                        indexList.add(d);
                        indexList.add(c);
                    }
                }
            }
        }

        vertices = new ModelVertex[positions.size()];
        for (int i = 0; i < vertices.length; i++) {
            Vec3 pos = positions.get(i);
            float[] uv = uvs.get(i);
            UVDirection dir = directions.get(i);
            NodeWeighting.Result weighting = NodeWeighting.nearest(pos, nodeRest, INFLUENCES_PER_VERTEX);
            vertices[i] = new ModelVertex(pos, uv[0], uv[1], dir, weighting.influences(), weighting.weights());
        }

        indices = indexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static UVDirection faceDirection(int face) {
        return switch (face) {
            case 1 -> UVDirection.NORTH; // front
            case 2 -> UVDirection.WEST; // left
            case 3 -> UVDirection.EAST; // right
            case 4 -> UVDirection.UP; // top (-hy)
            case 5 -> UVDirection.DOWN; // bottom (+hy)
            default -> null; // back (attachment)
        };
    }

    /**
     * Faces whose parameterization produces an inward normal in the default order.
     */
    private static boolean faceHasReversedWinding(int face) {
        return face == 1 || face == 3 || face == 4;
    }

    private static Vec3 faceVertex(int face, float u, float v, float hx, float hy, float minZ, float maxZ) {
        // v is always the vertical axis for the four wall faces: v=1 -> y=-hy ("up"),
        // v=0 -> y=+hy ("down"), matching ModelMeshCache's flip formula. u is
        // horizontal
        // (X for back/front, Z-depth for west/east).
        float verticalY = -hy + (1f - v) * (hy * 2f);
        float xAcrossU = -hx + u * (hx * 2f);
        float zAcrossU = minZ + u * (maxZ - minZ);
        float zAcrossV = minZ + v * (maxZ - minZ); // still used by top/bottom
        return switch (face) {
            case 0 -> new Vec3(xAcrossU, verticalY, minZ); // back
            case 1 -> new Vec3(xAcrossU, verticalY, maxZ); // front (NORTH)
            case 2 -> new Vec3(-hx, verticalY, zAcrossU); // left (WEST)
            case 3 -> new Vec3(hx, verticalY, zAcrossU); // right (EAST)
            case 4 -> new Vec3(xAcrossU, -hy, zAcrossV); // top
            default -> new Vec3(xAcrossU, hy, zAcrossV); // bottom
        };
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
