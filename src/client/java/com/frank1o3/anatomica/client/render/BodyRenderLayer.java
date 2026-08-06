package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.data.EntityBodyData;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import com.frank1o3.franklylib.Vec3;
import com.frank1o3.franklylib.client.render.AttachmentPoint;
import com.frank1o3.franklylib.client.render.FranklyAttachmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import com.frank1o3.anatomica.client.mixin.accessors.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

/**
 * Attaches the player's configured body model, left and right side, to the
 * "body"
 * attachment point, deformed each frame by that player's physics engine(s).
 *
 * <p>
 * Generic over {@code S}/{@code M} the same way {@code GenderLayer} is in the
 * mod
 * this was forked from, so {@code AvatarRendererMixin} can pass {@code this}
 * straight
 * through as the {@link RenderLayerParent} without an unchecked cast to a
 * concrete
 * model type.
 *
 * <p>
 * Per-tick physics simulation happens in {@link BodyPhysicsTicker}, once per
 * tracked player per game tick — this class only interpolates + submits
 * geometry.
 */
public final class BodyRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    private static final float SIDE_X_OFFSET = 0.045f;
    private static final String BODY_TARGET_PART = "body";

    // Model instances are cheap to reuse and expensive to keep reallocating:
    // without
    // this, resolveModel() would call factory.create() fresh every frame, defeating
    // ModelMeshCache (keyed by instance identity) and reallocating every model's
    // ModelVertex[] array 20+ times a second for no reason.
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

        IDeformableModel model = resolveModel(config.modelId());
        if (model == null)
            return;

        RenderType renderType = resolveBodyRenderType(renderState);
        if (renderType == null)
            return; // entity not actually visible this pass

        ClientBodyPhysics physics = ClientBodyPhysics.get(uuid);
        physics.ensureEngines(config);
        ModelMeshCache.TextureRegion region = new ModelMeshCache.TextureRegion(
                config.textureX1(), config.textureY1(), config.textureX2(), config.textureY2());
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        renderSide(poseStack, renderQueue, renderState, packedLight, model, region,
                physics.leftEngine(), buildAttachmentPoint(config, -1), renderType, partialTick);
        renderSide(poseStack, renderQueue, renderState, packedLight, model, region,
                physics.rightEngine(), buildAttachmentPoint(config, 1), renderType, partialTick);
    }

    private void renderSide(PoseStack poseStack, SubmitNodeCollector renderQueue, S renderState,
            int packedLight, IDeformableModel model, ModelMeshCache.TextureRegion region, IPhysicsEngine engine,
            AttachmentPoint attachment, RenderType renderType, float partialTick) {
        FranklyAttachmentRenderer.render(
                poseStack, renderQueue, renderState, getParentModel(), attachment,
                ModelMeshCache.get(model, region), new BoundMeshDeformer(model, engine),
                renderType, packedLight, 0, 0xFFFFFFFF, partialTick);
    }

    private AttachmentPoint buildAttachmentPoint(BodyConfig config, int side) {
        float sideSign = Math.signum(side);
        Vec3 offset = new Vec3(
                config.offsetX() + sideSign * (SIDE_X_OFFSET + config.spread()),
                config.offsetY(),
                config.offsetZ());
        float scale = 0.5f + config.size();
        return new AttachmentPoint(BODY_TARGET_PART, offset, Vec3.ZERO, scale);
    }

    private IDeformableModel resolveModel(Identifier modelId) {
        return MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }
}