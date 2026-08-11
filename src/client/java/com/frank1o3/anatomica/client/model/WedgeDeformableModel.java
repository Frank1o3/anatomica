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
 * A wedge/prism-shaped alternative to {@link BoxDeformableModel}: the flat
 * bottom (base, nearest the torso) is unchanged, but instead of a flat top
 * cap, the front wall slants back to meet the body-attachment plane at a
 * ridge — no top face exists at all. Cross-section in the Y-Z plane is a
 * right triangle rather than a rectangle, which should read as a smoother
 * ramp into the neck/chest than the box's hard top edge.
 *
 * <p>
 * This is a standalone experiment, not a replacement for
 * {@link BoxDeformableModel} — nothing here is wired into the box models. If it
 * looks better in-game it can be adopted; the UV/rotation
 * treatment on {@link BoxDeformableModel} was fixed independently of this.
 */
public final class WedgeDeformableModel implements IDeformableModel {

    private static final int INFLUENCES_PER_VERTEX = 3;
    private static final Identifier ID = Anatomica.id("wedge");

    private final ModelVertex[] vertices;
    private final int[] indices;

    public WedgeDeformableModel() {
        SoftbodyGridLayout.Layout layout = SoftbodyGridLayout.build();
        Vec3[] nodeRest = layout.restPositions();

        float hx = SoftbodyGridLayout.HALF_WIDTH;
        float hy = SoftbodyGridLayout.HALF_HEIGHT;
        float minZ = 0.0f; // back, flush against the body
        float maxZ = -SoftbodyGridLayout.DEPTH; // front, sticking out furthest

        List<ModelVertex> vertexList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();

        // Back plane — flush against the body. Not textured (dir = null), same
        // treatment as BoxDeformableModel's back face.
        addFace(vertexList, indexList, nodeRest, null,
                uv(new Vec3(-hx, -hy, minZ), 0f, 0f),
                uv(new Vec3(hx, -hy, minZ), 1f, 0f),
                uv(new Vec3(hx, hy, minZ), 1f, 1f),
                uv(new Vec3(-hx, hy, minZ), 0f, 1f));

        // Bottom — unchanged from the box's footprint. y=+hy is physically the
        // base, closest to the torso; "stays as it is" per the ask.
        addFace(vertexList, indexList, nodeRest, UVDirection.DOWN,
                uv(new Vec3(-hx, hy, minZ), 0f, 0f),
                uv(new Vec3(hx, hy, minZ), 1f, 0f),
                uv(new Vec3(hx, hy, maxZ), 1f, 1f),
                uv(new Vec3(-hx, hy, maxZ), 0f, 1f));

        // Slanted front face — the ramp. Runs from the back-top edge (y=-hy,
        // z=minZ) down to the bottom-front edge (y=+hy, z=maxZ).
        //
        // The winding is reversed compared to the original version so the NORTH
        // face is oriented correctly instead of appearing upside down.
        addFace(vertexList, indexList, nodeRest, UVDirection.NORTH,
                uv(new Vec3(-hx, -hy, minZ), 0f, 0f),
                uv(new Vec3(-hx, hy, maxZ), 0f, 1f),
                uv(new Vec3(hx, hy, maxZ), 1f, 1f),
                uv(new Vec3(hx, -hy, minZ), 1f, 0f));

        // Left triangular end cap. Only 3 of the box's 4 WEST corners exist —
        // there's no "front-top" vertex on a wedge — so this reuses WEST's
        // corner UVs minus that one corner.
        addFace(vertexList, indexList, nodeRest, UVDirection.WEST,
                uv(new Vec3(-hx, -hy, minZ), 1f, 0f), // back/top
                uv(new Vec3(-hx, hy, minZ), 1f, 1f), // back/bottom
                uv(new Vec3(-hx, hy, maxZ), 0f, 1f)); // front/bottom

        // Right triangular end cap, mirrored.
        addFace(vertexList, indexList, nodeRest, UVDirection.EAST,
                uv(new Vec3(hx, -hy, minZ), 0f, 0f), // back/top
                uv(new Vec3(hx, hy, minZ), 0f, 1f), // back/bottom
                uv(new Vec3(hx, hy, maxZ), 1f, 1f)); // front/bottom

        vertices = vertexList.toArray(new ModelVertex[0]);
        indices = indexList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static VertexUV uv(Vec3 position, float u, float v) {
        return new VertexUV(position, u, v);
    }

    private static void addFace(List<ModelVertex> vertexList, List<Integer> indexList, Vec3[] nodeRest,
            UVDirection dir, VertexUV... corners) {
        int base = vertexList.size();
        for (VertexUV corner : corners) {
            NodeWeighting.Result w = NodeWeighting.nearest(corner.position(), nodeRest, INFLUENCES_PER_VERTEX);
            vertexList.add(new ModelVertex(corner.position(), corner.u(), corner.v(), dir, w.influences(),
                    w.weights()));
        }
        if (corners.length == 4) {
            indexList.add(base);
            indexList.add(base + 1);
            indexList.add(base + 2);
            indexList.add(base);
            indexList.add(base + 2);
            indexList.add(base + 3);
        } else if (corners.length == 3) {
            indexList.add(base);
            indexList.add(base + 1);
            indexList.add(base + 2);
        } else {
            throw new IllegalArgumentException("addFace expects 3 or 4 corners, got " + corners.length);
        }
    }

    private record VertexUV(Vec3 position, float u, float v) {
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
        return Component.translatable("model.anatomica.wedge");
    }
}