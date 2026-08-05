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

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Attaches the player's configured body model, left and right side, to the
 * "body"
 * part of the player model, deformed each frame by that player's physics
 * engine(s).
 *
 * <p>
 * All actual mesh submission is delegated to FranklyLib's
 * {@link FranklyAttachmentRenderer} — this class's only job is resolving which
 * config/model/engine applies to a given player render state and calling into
 * FranklyLib with it. Per-tick physics simulation ({@code IPhysicsEngine#tick})
 * does
 * <b>not</b> happen here — this class only calls
 * {@code interpolate(partialTick)}
 * (via {@link BoundMeshDeformer}) once per frame. The actual {@code tick(...)}
 * calls
 * belong on a client tick event, once per tracked player per game tick — see
 * {@code AnatomicaClient} / a dedicated ticking hook.
 *
 * <p>
 * <b>Verify before compiling — this file makes a few best-effort guesses about
 * your
 * exact Minecraft/Loom mappings that this project can't check without your
 * build:</b>
 * <ul>
 * <li>{@link RenderLayer#submit}'s exact parameter list (assumed here to end in
 * a
 * {@code float partialTick}) — adjust to match whatever your version's
 * {@code RenderLayer} base class actually declares.</li>
 * <li>{@link #resolveUuid(PlayerRenderState)} — assumes
 * {@code PlayerRenderState}
 * exposes the player's UUID somehow (commonly via an embedded profile field);
 * point this at whatever field/method your mappings actually expose.</li>
 * </ul>
 * The surrounding logic (config lookup, per-side attachment points, mirroring,
 * hide-in-armor check) is the part that matters and shouldn't need to change
 * once
 * those two seams are wired up correctly.
 */
public final class BodyRenderLayer extends RenderLayer<PlayerRenderState, PlayerModel> {

    private static final float SIDE_X_OFFSET = 0.045f;
    private static final Identifier BODY_TARGET_PART_KEY = Identifier.fromNamespaceAndPath("anatomica", "body");

    public BodyRenderLayer(RenderLayerParent<PlayerRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector renderQueue, int packedLight,
            PlayerRenderState renderState, float partialTick) {

        UUID uuid = resolveUuid(renderState);
        if (uuid == null || !EntityBodyData.has(uuid)) {
            return;
        }

        BodyConfig config = EntityBodyData.get(uuid);

        // TODO: if (!config.showInArmor() && <renderState is wearing a chestplate>)
        // return;
        // Wire this up once you've confirmed how PlayerRenderState exposes equipped
        // armor — deliberately left out rather than guessing a method name here.

        IDeformableModel model = resolveModel(config.modelId());
        if (model == null) {
            return;
        }

        ClientBodyPhysics physics = ClientBodyPhysics.get(uuid);
        RenderType renderType = RenderType.entityCutoutNoCull(bodyTextureLocation());
        /// entityCutoutNoCull

        renderSide(poseStack, renderQueue, renderState, packedLight, model,
                physics.leftEngine(), buildAttachmentPoint(config, -1), renderType, partialTick);
        renderSide(poseStack, renderQueue, renderState, packedLight, model,
                physics.rightEngine(), buildAttachmentPoint(config, 1), renderType, partialTick);
    }

    private void renderSide(PoseStack poseStack, SubmitNodeCollector renderQueue, PlayerRenderState renderState,
            int packedLight, IDeformableModel model, IPhysicsEngine engine, AttachmentPoint attachment,
            RenderType renderType, float partialTick) {
        FranklyAttachmentRenderer.render(
                poseStack,
                renderQueue,
                renderState,
                getParentModel(),
                attachment,
                ModelMeshCache.get(model),
                new BoundMeshDeformer(model, engine),
                renderType,
                packedLight,
                0,
                0xFFFFFFFF,
                partialTick);
    }

    private AttachmentPoint buildAttachmentPoint(BodyConfig config, int side) {
        float sideSign = Math.signum(side);
        Vec3 offset = new Vec3(
                config.offsetX() + sideSign * (SIDE_X_OFFSET + config.spread()),
                config.offsetY(),
                config.offsetZ());
        float scale = 0.5f + config.size();
        return new AttachmentPoint(BODY_TARGET_PART_KEY.getPath(), offset, Vec3.ZERO, scale);
    }

    // Model instances are cheap to reuse and expensive to keep reallocating:
    // without
    // this, resolveModel() would call factory.create() fresh every single frame,
    // which
    // both defeats ModelMeshCache (keyed by instance identity — a new instance
    // every
    // frame never hits its cache) and reallocates every model's ModelVertex[] array
    // 20+ times a second for no reason, since IDeformableModel instances are
    // stateless.
    private static final java.util.Map<Identifier, IDeformableModel> MODEL_INSTANCE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private IDeformableModel resolveModel(Identifier modelId) {
        return MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.MODELS.get(id);
            return factory != null ? factory.create() : null;
        });
    }

    private UUID resolveUuid(PlayerRenderState renderState) {
        // TODO verify: point this at whatever field/method your PlayerRenderState
        // mappings actually expose for the player's UUID.
        return renderState.gameProfileId();
    }

    private Identifier bodyTextureLocation() {
        // Placeholder — replace once a real body texture asset exists, e.g.
        // assets/anatomica/textures/models/body.png.
        return Identifier.fromNamespaceAndPath("anatomica", "textures/models/body.png");
    }
}
