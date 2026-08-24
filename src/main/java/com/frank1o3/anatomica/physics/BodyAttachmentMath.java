package com.frank1o3.anatomica.physics;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.franklylib.Vec3;
import com.frank1o3.franklylib.client.render.AttachmentPoint;
import net.minecraft.util.Mth;

/**
 * Shared math utilities for computing attachment points and coordinate transformations
 * for body and garment rendering layers.
 */
public final class BodyAttachmentMath {

    public static final float SIDE_X_OFFSET = 0.10f;
    public static final float BASE_Y_OFFSET = 0.20f;
    public static final float BASE_Z_OFFSET = -0.125f;
    public static final String BODY_TARGET_PART = "body";

    private BodyAttachmentMath() {
    }

    /**
     * Builds the attachment point for an individual breast side.
     *
     * <p>In Minecraft model coordinates: +X is character's Left, -X is character's Right.
     * {@code side = -1} corresponds to Left breast (+X), {@code side = 1} to Right breast (-X).
     *
     * @param config the entity body configuration
     * @param side -1 for left side, 1 for right side
     * @return the resolved {@link AttachmentPoint}
     */
    public static AttachmentPoint forBreastSide(IBodyConfig config, int side) {
        float sideSign = -Math.signum((float) side);
        Vec3 offset = new Vec3(
                config.offsetX() + sideSign * (SIDE_X_OFFSET + config.spread()),
                BASE_Y_OFFSET + config.offsetY(),
                BASE_Z_OFFSET - config.offsetZ());
        float scale = 0.5f + config.size();

        // Cleavage is a presentation setting: rotate each breast away from the
        // centre of the chest around its anchored back layer. It deliberately
        // does not enter the physics simulation.
        float outwardAngle = Math.min(config.cleavage() * 100f, 10f) * Mth.DEG_TO_RAD;
        Vec3 rotation = new Vec3(0f, side * outwardAngle, 0f);
        return new AttachmentPoint(BODY_TARGET_PART, offset, rotation, scale);
    }

    /**
     * Builds the attachment point for the outer cloth layer.
     *
     * <p>Cloth is modeled as a single unified garment spanning both breasts rather than
     * split per-side meshes. It attaches to the center of the chest (X-offset without lateral
     * spread or cleavage tilt) so that both breast sides are covered symmetrically.
     *
     * @param config the entity body configuration
     * @return the resolved centered {@link AttachmentPoint}
     */
    public static AttachmentPoint forCloth(IBodyConfig config) {
        Vec3 offset = new Vec3(
                config.offsetX(),
                BASE_Y_OFFSET + config.offsetY(),
                BASE_Z_OFFSET - config.offsetZ());
        float scale = 0.5f + config.size();
        return new AttachmentPoint(BODY_TARGET_PART, offset, Vec3.ZERO, scale);
    }
}
