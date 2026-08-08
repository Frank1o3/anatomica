package com.frank1o3.anatomica.client.render;

import com.frank1o3.franklylib.Mesh;
import com.frank1o3.franklylib.MeshVertex;
import com.frank1o3.franklylib.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Anatomica's entity-layer geometry submission. Player render types consume
 * quads, so each indexed triangle is submitted as a degenerate quad.
 */
record AnatomicaGeometryRenderCommand(Mesh mesh, Vec3[] deformedPositions, int light, int overlay, int color)
        implements SubmitNodeCollector.CustomGeometryRenderer {

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        if (mesh == null || deformedPositions == null) {
            return;
        }

        Matrix4f positionMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        for (int index = 0; index < mesh.indices().length; index += 3) {
            int aIndex = mesh.indices()[index];
            int bIndex = mesh.indices()[index + 1];
            int cIndex = mesh.indices()[index + 2];
            Vec3 aPosition = deformedPositions[aIndex];
            Vec3 bPosition = deformedPositions[bIndex];
            Vec3 cPosition = deformedPositions[cIndex];
            if (aPosition == null || bPosition == null || cPosition == null) {
                continue;
            }

            Vec3 normal = bPosition.subtract(aPosition).cross(cPosition.subtract(aPosition)).normalize();
            Vector3f transformedNormal = new Vector3f(normal.x(), normal.y(), normal.z()).mul(normalMatrix);
            MeshVertex a = mesh.vertices()[aIndex];
            MeshVertex b = mesh.vertices()[bIndex];
            MeshVertex c = mesh.vertices()[cIndex];

            submit(vertexConsumer, positionMatrix, aPosition, a, transformedNormal);
            submit(vertexConsumer, positionMatrix, bPosition, b, transformedNormal);
            submit(vertexConsumer, positionMatrix, cPosition, c, transformedNormal);
            submit(vertexConsumer, positionMatrix, cPosition, c, transformedNormal);
        }
    }

    private void submit(VertexConsumer consumer, Matrix4f matrix, Vec3 position, MeshVertex vertex,
            Vector3f normal) {
        Vector4f transformed = new Vector4f(position.x(), position.y(), position.z(), 1f).mul(matrix);
        consumer.addVertex(transformed.x(), transformed.y(), transformed.z(), color, vertex.u(), vertex.v(),
                overlay, light, normal.x(), normal.y(), normal.z());
    }
}
