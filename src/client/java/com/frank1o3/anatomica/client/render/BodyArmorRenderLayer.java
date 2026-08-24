package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.client.data.EntityBodyData;
import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.model.IDeformableModel;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.franklylib.Vec3;
import com.frank1o3.franklylib.client.render.AttachmentPoint;
import com.frank1o3.franklylib.client.render.FranklyAttachmentRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.frank1o3.anatomica.physics.BodyAttachmentMath;

/** Renders the configured body mesh with the worn chestplate's equipment texture. */
public final class BodyArmorRenderLayer<S extends AvatarRenderState, M extends HumanoidModel<S>>
        extends RenderLayer<S, M> {

    private static final Map<Identifier, IDeformableModel> MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Identifier, IDeformableModel> CLOTH_MODEL_INSTANCE_CACHE = new ConcurrentHashMap<>();

    private final EquipmentAssetManager equipmentAssets;

    public BodyArmorRenderLayer(RenderLayerParent<S, M> parent, EquipmentAssetManager equipmentAssets) {
        super(parent);
        this.equipmentAssets = equipmentAssets;
    }

    /** Invalidates resource-dependent model instances after a resource reload. */
    public static void clearModelCache() {
        MODEL_INSTANCE_CACHE.clear();
        CLOTH_MODEL_INSTANCE_CACHE.clear();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector renderQueue, int packedLight,
            S state, float limbAngle, float limbDistance) {
        BodyRenderState bodyState = BodyRenderState.get(state);
        if (bodyState == null || !bodyState.hasConfig) {
            return;
        }

        IBodyConfig config = EntityBodyData.INSTANCE.get(bodyState.uuid);
        if (!config.breastsEnabled() || !config.showInArmor()) {
            return;
        }

        ItemStack chestplate = state.chestEquipment;
        var equippable = chestplate.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.CHEST) {
            return;
        }
        var asset = equippable.assetId().orElse(null);
        if (asset == null) {
            return;
        }
        var layers = equipmentAssets.get(asset).getLayers(EquipmentClientInfo.LayerType.HUMANOID);
        if (layers.isEmpty()) {
            return;
        }

        IDeformableModel model = resolveModel(config.modelId());
        if (model == null) {
            return;
        }

        ClientBodyPhysics physics = ClientBodyPhysics.get(bodyState.uuid);
        physics.ensureEngines(config);
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        int dyedColor = DyedItemColor.getOrDefault(chestplate, 0);
        boolean glint = chestplate.hasFoil();

        renderSide(poseStack, renderQueue, state, packedLight, config, model, physics.leftEngine(), -1,
                layers, dyedColor, glint, partialTick);
        renderSide(poseStack, renderQueue, state, packedLight, config, model, physics.rightEngine(), 1,
                layers, dyedColor, glint, partialTick);

        // Cloth garment layer rendered with chestplate equipment texture
        if (config.clothEnabled()) {
            IDeformableModel clothModel = resolveClothModel(config.clothModelId());
            if (clothModel != null && physics.clothEngine() != null) {
                AttachmentPoint clothAttachment = BodyAttachmentMath.forCloth(config);
                for (EquipmentClientInfo.Layer layer : layers) {
                    int color = colorForLayer(layer, dyedColor);
                    if (color == 0) {
                        continue;
                    }
                    var renderType = RenderTypes.armorCutoutNoCull(layer.getTextureLocation(EquipmentClientInfo.LayerType.HUMANOID));
                    FranklyAttachmentRenderer.render(poseStack, renderQueue, state, getParentModel(), clothAttachment,
                            ModelMeshCache.get(clothModel, UVLayout.DEFAULT_TORSO),
                            new BoundMeshDeformer(clothModel, physics.clothEngine()), renderType,
                            packedLight, OverlayTexture.NO_OVERLAY, ARGB.opaque(color), partialTick);
                    if (glint) {
                        FranklyAttachmentRenderer.render(poseStack, renderQueue, state, getParentModel(), clothAttachment,
                                ModelMeshCache.get(clothModel, UVLayout.DEFAULT_TORSO),
                                new BoundMeshDeformer(clothModel, physics.clothEngine()),
                                RenderTypes.armorEntityGlint(), packedLight, OverlayTexture.NO_OVERLAY, -1, partialTick);
                    }
                }
            }
        }
    }

    private void renderSide(PoseStack poseStack, SubmitNodeCollector renderQueue, S state, int packedLight,
            IBodyConfig config, IDeformableModel model, com.frank1o3.anatomica.physics.IPhysicsEngine engine,
            int side, java.util.List<EquipmentClientInfo.Layer> layers, int dyedColor, boolean glint,
            float partialTick) {
        AttachmentPoint attachment = BodyAttachmentMath.forBreastSide(config, side);
        for (EquipmentClientInfo.Layer layer : layers) {
            int color = colorForLayer(layer, dyedColor);
            if (color == 0) {
                continue;
            }
            var renderType = RenderTypes.armorCutoutNoCull(layer.getTextureLocation(EquipmentClientInfo.LayerType.HUMANOID));
            FranklyAttachmentRenderer.render(poseStack, renderQueue, state, getParentModel(), attachment,
                    ModelMeshCache.get(model, side < 0 ? UVLayout.DEFAULT_LEFT : UVLayout.DEFAULT_RIGHT),
                    new BoundMeshDeformer(model, engine), renderType,
                    packedLight, OverlayTexture.NO_OVERLAY, ARGB.opaque(color), partialTick);
            if (glint) {
                FranklyAttachmentRenderer.render(poseStack, renderQueue, state, getParentModel(), attachment,
                        ModelMeshCache.get(model, side < 0 ? UVLayout.DEFAULT_LEFT : UVLayout.DEFAULT_RIGHT),
                        new BoundMeshDeformer(model, engine),
                        RenderTypes.armorEntityGlint(), packedLight, OverlayTexture.NO_OVERLAY, -1, partialTick);
            }
        }
    }

    private static int colorForLayer(EquipmentClientInfo.Layer layer, int dyedColor) {
        return layer.dyeable()
                .map(dyeable -> dyedColor != 0 ? dyedColor : dyeable.colorWhenUndyed().orElse(0))
                .orElse(-1);
    }

    private static IDeformableModel resolveModel(Identifier modelId) {
        return MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.INNER_MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }

    private static IDeformableModel resolveClothModel(Identifier modelId) {
        return CLOTH_MODEL_INSTANCE_CACHE.computeIfAbsent(modelId, id -> {
            ModelFactory factory = AnatomicaRegistries.CLOTH_MODELS.get(id).get().value();
            return factory != null ? factory.create() : null;
        });
    }
}
