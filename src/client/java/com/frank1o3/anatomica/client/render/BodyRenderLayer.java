package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.client.mixin.accessors.LivingEntityRendererAccessor;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.client.data.EntityBodyData;
import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.franklylib.Vec3;
import com.frank1o3.franklylib.client.render.AttachmentPoint;
import com.frank1o3.franklylib.client.render.FranklyAttachmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.frank1o3.anatomica.physics.BodyAttachmentMath;

/**
 * Attaches the player's configured body model (inner breasts and outer cloth garment)
 * to the "body" attachment point, deformed each frame by that player's physics engine(s).
 *
 * <p>
 * Geometry submission is delegated entirely to FranklyLib's
 * {@link FranklyAttachmentRenderer} — Anatomica no longer keeps its own copy of the
 * attachment-transform/quad-submission code.
 */
public final class BodyRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {

    private static final Map<Identifier, IDeformableModel> MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, IDeformableModel> CLOTH_MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();
    private final RenderLayerParent<S, M> context;

    public BodyRenderLayer(RenderLayerParent<S, M> parent) {
        super(parent);
        this.context = parent;
    }

    /** Invalidates resource-dependent model instances after a resource reload. */
    public static void clearModelCache() {
        MODEL_INSTANCE_CACHE.clear();
        CLOTH_MODEL_INSTANCE_CACHE.clear();
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
        IBodyConfig config = EntityBodyData.INSTANCE.get(uuid);

        if (!config.breastsEnabled())
            return;

        // The matching armor layer renders the deformed surface with the
        // chestplate texture. Keeping the skin pass out of this case prevents it
        // from being drawn over vanilla armor.
        if (!renderState.chestEquipment.isEmpty())
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

        // Inner realistic layer (solid flesh tone fill)
        renderSide(poseStack, renderQueue, renderState, packedLight, model, leftLayout,
                physics.leftEngine(), BodyAttachmentMath.forBreastSide(config, -1), renderType, config.innerColor(), partialTick);
        renderSide(poseStack, renderQueue, renderState, packedLight, model, rightLayout,
                physics.rightEngine(), BodyAttachmentMath.forBreastSide(config, 1), renderType, config.innerColor(), partialTick);

        // Outer cloth layer (samples player torso texture region)
        if (config.clothEnabled()) {
            IDeformableModel clothModel = resolveClothModel(config.clothModelId());
            if (clothModel != null && physics.clothEngine() != null) {
                FranklyAttachmentRenderer.render(
                        poseStack, renderQueue, renderState, getParentModel(),
                        BodyAttachmentMath.forCloth(config),
                        ModelMeshCache.get(clothModel, UVLayout.DEFAULT_TORSO),
                        new BoundMeshDeformer(clothModel, physics.clothEngine()),
                        renderType, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF, partialTick);
            }
        }
    }

    private void renderSide(PoseStack poseStack, SubmitNodeCollector renderQueue, S renderState,
            int packedLight, IDeformableModel model, UVLayout layout, IPhysicsEngine engine,
            AttachmentPoint attachment, RenderType renderType, int color, float partialTick) {
        FranklyAttachmentRenderer.render(
                poseStack, renderQueue, renderState, getParentModel(), attachment,
                ModelMeshCache.get(model, layout), new BoundMeshDeformer(model, engine),
                renderType, packedLight, OverlayTexture.NO_OVERLAY, color, partialTick);
    }

    private IDeformableModel resolveModel(Identifier modelId) {
        return MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.INNER_MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }

    private IDeformableModel resolveClothModel(Identifier modelId) {
        return CLOTH_MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.CLOTH_MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }
}
