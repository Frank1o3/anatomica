package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.client.mixin.accessors.LivingEntityRendererAccessor;
import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.data.EntityBodyData;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.franklylib.Vec3;
import com.frank1o3.franklylib.client.render.AttachmentPoint;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

/**
 * Attaches the player's configured body model, left and right side, to the
 * "body"
 * attachment point, deformed each frame by that player's physics engine(s).
 */
public final class BodyRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    private static final float SIDE_X_OFFSET = 0.10f;
    private static final String BODY_TARGET_PART = "body";

    private static final Map<Identifier, IDeformableModel> MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();
    private final RenderLayerParent<S, M> context;

    public BodyRenderLayer(RenderLayerParent<S, M> parent) {
        super(parent);
        this.context = parent;
    }

    private @Nullable RenderType resolveBodyRenderType(S state) {
        boolean bodyVisible = !state.isInvisible;
        boolean translucent = state.isInvisible && !state.isInvisibleToPlayer;
        boolean glowing = state.appearsGlowing();
        var renderer = (LivingEntityRenderer<?, ?, ?>) context;
        return ((LivingEntityRendererAccessor) renderer).invokeGetRenderType(state, bodyVisible, translucent, glowing);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector renderQueue, int packedLight,
            S renderState, float limbAngle, float limbDistance) {

        BodyRenderState bodyState = BodyRenderState.get(renderState);
        if (bodyState == null || !bodyState.hasConfig)
            return;

        UUID uuid = bodyState.uuid;
        BodyConfig config = EntityBodyData.get(uuid);

        if (!config.breastsEnabled())
            return;

        IDeformableModel model = resolveModel(config.modelId());
        if (model == null)
            return;

        RenderType renderType = resolveBodyRenderType(renderState);
        if (renderType == null)
            return; // entity not rendering this pass

        ClientBodyPhysics physics = ClientBodyPhysics.get(uuid);
        physics.ensureEngines(config);

        UVLayout leftLayout = config.leftUvLayout();
        UVLayout rightLayout = config.independentSides() ? config.rightUvLayout() : config.leftUvLayout();
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        renderSide(poseStack, renderQueue, renderState, packedLight, model, leftLayout,
                physics.leftEngine(), buildAttachmentPoint(config, -1), renderType, partialTick);
        renderSide(poseStack, renderQueue, renderState, packedLight, model, rightLayout,
                physics.rightEngine(), buildAttachmentPoint(config, 1), renderType, partialTick);
    }

    private void renderSide(PoseStack poseStack, SubmitNodeCollector renderQueue, S renderState,
            int packedLight, IDeformableModel model, UVLayout layout, IPhysicsEngine engine,
            AttachmentPoint attachment, RenderType renderType, float partialTick) {
        AnatomicaAttachmentRenderer.render(
                poseStack, renderQueue, renderState, getParentModel(), attachment,
                ModelMeshCache.get(model, layout), new BoundMeshDeformer(model, engine),
                renderType, packedLight, 0, 0xFFFFFFFF, partialTick);
    }

    private static final float BASE_Y_OFFSET = 0.20f;
    private static final float BASE_Z_OFFSET = -0.125f;

    private AttachmentPoint buildAttachmentPoint(BodyConfig config, int side) {
        // In Minecraft model coordinates: +X is character's Left, -X is character's Right.
        // side = -1 is Left breast -> +X. side = 1 is Right breast -> -X.
        float sideSign = -Math.signum(side);
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

    private IDeformableModel resolveModel(Identifier modelId) {
        return MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }
}
