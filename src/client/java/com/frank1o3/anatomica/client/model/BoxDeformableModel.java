package com.frank1o3.anatomica.client.model;

import com.frank1o3.anatomica.Anatomica;
import com.frank1o3.anatomica.client.physics.SoftbodyGridLayout;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelVertex;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * An 8-vertex cuboid, weighted toward whichever handful of {@link SoftbodyGridLayout}
 * nodes are nearest each corner. This is a genuine special case of the same
 * node-skinning path every model goes through — not a separate rendering mode — so it
 * naturally "degenerates" to a simple deforming box rather than needing its own
 * branch anywhere in the renderer.
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
        float minZ = -SoftbodyGridLayout.DEPTH * 0.5f;
        float maxZ = SoftbodyGridLayout.DEPTH * 0.5f;

        Vec3[] corners = new Vec3[] {
                new Vec3(-hx, -hy, minZ), new Vec3(hx, -hy, minZ),
                new Vec3(hx, hy, minZ), new Vec3(-hx, hy, minZ),
                new Vec3(-hx, -hy, maxZ), new Vec3(hx, -hy, maxZ),
                new Vec3(hx, hy, maxZ), new Vec3(-hx, hy, maxZ)
        };
        float[][] uv = new float[][] {
                { 0f, 0f }, { 1f, 0f }, { 1f, 1f }, { 0f, 1f },
                { 0f, 0f }, { 1f, 0f }, { 1f, 1f }, { 0f, 1f }
        };

        vertices = new ModelVertex[corners.length];
        for (int i = 0; i < corners.length; i++) {
            NodeWeighting.Result weighting = NodeWeighting.nearest(corners[i], nodeRest, INFLUENCES_PER_VERTEX);
            vertices[i] = new ModelVertex(corners[i], uv[i][0], uv[i][1], weighting.influences(), weighting.weights());
        }

        // Same winding convention as any simple box generator: two triangles per face, CCW.
        indices = new int[] {
                0, 1, 2, 0, 2, 3, // back  (z = minZ)
                4, 6, 5, 4, 7, 6, // front (z = maxZ)
                0, 3, 7, 0, 7, 4, // left
                1, 5, 6, 1, 6, 2, // right
                0, 4, 5, 0, 5, 1, // bottom
                3, 2, 6, 3, 6, 7  // top
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
        return Component.translatable("model.anatomica.box");
    }
}
