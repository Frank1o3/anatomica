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
 * A cuboid deformable model with 6 faces (24 vertices), each face tagged with
 * its {@link UVDirection}
 * for per-face UV remapping.
 */
public final class BoxDeformableModel implements IDeformableModel {

    private static final int INFLUENCES_PER_VERTEX = 3;
    private static final Identifier ID = Anatomica.id("box");

    private final ModelVertex[] vertices;
    private final int[] indices;

    public BoxDeformableModel() {
        SoftbodyGridLayout.Layout layout = SoftbodyGridLayout.build();
        Vec3[] nodeRest = layout.restPositions();

        float hx = SoftbodyGridLayout.HALF_WIDTH;
        float hy = SoftbodyGridLayout.HALF_HEIGHT;
        float minZ = 0.0f;
        float maxZ = -SoftbodyGridLayout.DEPTH;

        List<ModelVertex> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        // Helper to add a quad face (4 vertices + 6 indices)
        // Vertices are added in order: bottom-left (0,0), bottom-right (1,0), top-right
        // (1,1), top-left (0,1)
        addFace(vertexList, indexList, nodeRest, new Vec3(-hx, -hy, minZ), new Vec3(hx, -hy, minZ),
                new Vec3(hx, hy, minZ), new Vec3(-hx, hy, minZ), null, false, false); // Back
        addFace(vertexList, indexList, nodeRest, new Vec3(-hx, -hy, maxZ), new Vec3(hx, -hy, maxZ),
                new Vec3(hx, hy, maxZ), new Vec3(-hx, hy, maxZ), UVDirection.NORTH, true, true); // Front
        addFace(vertexList, indexList, nodeRest, new Vec3(-hx, -hy, maxZ), new Vec3(-hx, -hy, minZ),
                new Vec3(-hx, hy, minZ), new Vec3(-hx, hy, maxZ), UVDirection.WEST, false, true); // Left
        addFace(vertexList, indexList, nodeRest, new Vec3(hx, -hy, minZ), new Vec3(hx, -hy, maxZ),
                new Vec3(hx, hy, maxZ), new Vec3(hx, hy, minZ), UVDirection.EAST, false, true); // Right
        addFace(vertexList, indexList, nodeRest, new Vec3(-hx, -hy, minZ), new Vec3(hx, -hy, minZ),
                new Vec3(hx, -hy, maxZ), new Vec3(-hx, -hy, maxZ), UVDirection.UP, true, false); // Top (-hy)
        addFace(vertexList, indexList, nodeRest, new Vec3(-hx, hy, maxZ), new Vec3(hx, hy, maxZ),
                new Vec3(hx, hy, minZ), new Vec3(-hx, hy, minZ), UVDirection.DOWN, false, false); // Bottom (+hy)

        vertices = vertexList.toArray(new ModelVertex[0]);
        indices = indexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static void addFace(List<ModelVertex> vertexList, List<Integer> indexList, Vec3[] nodeRest,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, UVDirection dir, boolean reverseWinding, boolean verticalFace) {
        int base = vertexList.size();
        float[][] uvs = verticalFace
                ? new float[][] { { 0f, 1f }, { 1f, 1f }, { 1f, 0f }, { 0f, 0f } }
                : new float[][] { { 0f, 0f }, { 1f, 0f }, { 1f, 1f }, { 0f, 1f } };
        Vec3[] positions = { v0, v1, v2, v3 };

        for (int i = 0; i < 4; i++) {
            NodeWeighting.Result w = NodeWeighting.nearest(positions[i], nodeRest, INFLUENCES_PER_VERTEX);
            vertexList.add(new ModelVertex(positions[i], uvs[i][0], uvs[i][1], dir, w.influences(), w.weights()));
        }

        // Two outward-facing triangles. Front and top parameterizations run in
        // the opposite direction and therefore need their winding reversed.
        indexList.add(base);
        indexList.add(reverseWinding ? base + 2 : base + 1);
        indexList.add(reverseWinding ? base + 1 : base + 2);
        indexList.add(base);
        indexList.add(reverseWinding ? base + 3 : base + 2);
        indexList.add(reverseWinding ? base + 2 : base + 3);
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
        return Component.translatable("model.anatomica.box");
    }
}
