package com.frank1o3.anatomica.client.gui.screen;

import com.frank1o3.anatomica.client.gui.TextureRegionPicker;
import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.config.AnatomicaConfig;
import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.data.EntityBodyData;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import com.frank1o3.franklylib.client.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BodyCustomizationScreen extends BaseFranklyScreen {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 200;
    private static final int PADDING = 12;
    private static final int ROW_HEIGHT = 22;
    private static final int SLIDER_WIDTH = 216;

    private enum Tab {
        GENERAL, PHYSICS, MODEL, UV
    }

    private Tab currentTab = Tab.GENERAL;
    private BodyConfig working;

    public BodyCustomizationScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.anatomica.body_customization"), parent, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        if (working == null) {
            Minecraft client = Minecraft.getInstance();
            working = client.player != null
                    ? EntityBodyData.get(client.player.getUUID()).copy()
                    : new BodyConfig();
        }

        int x = panelX() + PADDING;
        int tabY = panelY() + 20;

        addRenderableWidget(FranklyTabBar.<Tab>builder()
                .bounds(x, tabY, SLIDER_WIDTH, 16)
                .tabs(List.of(Tab.GENERAL, Tab.PHYSICS, Tab.MODEL, Tab.UV))
                .labelMapper(tab -> Component.translatable("tab.anatomica." + tab.name().toLowerCase()))
                .current(currentTab)
                .onSelect(this::switchTab)
                .build());

        int y = tabY + 22;
        switch (currentTab) {
            case GENERAL -> initGeneralTab(x, y);
            case PHYSICS -> initPhysicsTab(x, y);
            case MODEL -> initModelTab(x, y);
            case UV -> initUvTab(x, y);
        }
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        rebuildWidgets();
    }

    private void initGeneralTab(int x, int y) {
        addRenderableWidget(FranklyEntityPreviewWidget.builder()
                .bounds(x + SLIDER_WIDTH - 64, y, 64, 96)
                .previewSize(28)
                .entity(() -> Minecraft.getInstance().player)
                .build());

        int sliderWidth = SLIDER_WIDTH - 72;
        addRenderableWidget(sizeSlider(x, y, sliderWidth));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_x", x, y, sliderWidth, working.offsetX(), working::setOffsetX));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_y", x, y, sliderWidth, working.offsetY(), working::setOffsetY));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_z", x, y, sliderWidth, working.offsetZ(), working::setOffsetZ));
        y += ROW_HEIGHT;
        addRenderableWidget(spreadSlider(x, y, sliderWidth));
        y += ROW_HEIGHT + 4;

        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(x, y, 14, 14)
                .label(Component.translatable("option.anatomica.show_in_armor"))
                .checked(working.showInArmor())
                .onToggle(value -> {
                    working.setShowInArmor(value);
                    pushToServer();
                })
                .build());

        addDoneButton();
    }

    private void initPhysicsTab(int x, int y) {
        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(x, y, 14, 14)
                .label(Component.translatable("option.anatomica.physics_enabled"))
                .checked(working.physicsEnabled())
                .onToggle(value -> {
                    working.setPhysicsEnabled(value);
                    pushToServer();
                })
                .build());
        y += 20;

        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(x, y, 14, 14)
                .label(Component.translatable("option.anatomica.independent_sides"))
                .checked(working.independentSides())
                .onToggle(value -> {
                    working.setIndependentSides(value);
                    pushToServer();
                })
                .build());
        y += 24;

        addRenderableWidget(bounceSlider(x, y));
        y += ROW_HEIGHT;
        addRenderableWidget(softnessSlider(x, y));

        addDoneButton();
    }

    private void initModelTab(int x, int y) {
        List<Identifier> engineIds = new ArrayList<>();
        AnatomicaRegistries.PHYSICS_ENGINES.keySet().forEach(engineIds::add);

        addRenderableWidget(FranklyDropdown.<Identifier>builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .options(engineIds)
                .current(working.physicsEngineId())
                .labelMapper(id -> Component.literal(id.getPath()))
                .onSelect(id -> {
                    working.setPhysicsEngineId(id);
                    pushToServer();
                })
                .build());
        y += 28;

        List<Identifier> modelIds = new ArrayList<>();
        AnatomicaRegistries.MODELS.keySet().forEach(modelIds::add);

        addRenderableWidget(FranklyDropdown.<Identifier>builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .options(modelIds)
                .current(working.modelId())
                .labelMapper(this::modelDisplayName)
                .onSelect(id -> {
                    working.setModelId(id);
                    pushToServer();
                })
                .build());

        addDoneButton();
    }

    private Component modelDisplayName(Identifier id) {
        ModelFactory factory = AnatomicaRegistries.MODELS.get(id).get().value();
        return factory != null ? factory.create().displayName() : Component.literal(id.getPath());
    }

    private void initUvTab(int x, int y) {
        Consumer<int[]> commit = region -> {
            working.setTextureRegion(region[0], region[1], region[2], region[3]);
            pushToServer();
        };
        int pickerSize = Math.min(panelHeight - 60, SLIDER_WIDTH);
        addRenderableWidget(new TextureRegionPicker(x, y, pickerSize, working, commit));

        addRenderableWidget(FranklyButton.builder()
                .bounds(x, panelY() + panelHeight - 26, SLIDER_WIDTH / 2 - 4, 20)
                .message(Component.translatable("option.anatomica.reset_uv"))
                .onPress(btn -> {
                    working.setTextureRegion(0, 0, 64, 64);
                    pushToServer();
                    rebuildWidgets();
                })
                .build());

        addRenderableWidget(FranklyButton.builder()
                .bounds(x + SLIDER_WIDTH / 2 + 4, panelY() + panelHeight - 26, SLIDER_WIDTH / 2 - 4, 20)
                .message(Component.translatable("gui.done"))
                .onPress(btn -> onClose())
                .build());
    }

    private void addDoneButton() {
        addRenderableWidget(FranklyButton.builder()
                .bounds(panelX() + PADDING, panelY() + panelHeight - 26, SLIDER_WIDTH, 20)
                .message(Component.translatable("gui.done"))
                .onPress(btn -> onClose())
                .build());
    }

    private FranklySlider sizeSlider(int x, int y, int width) {
        return FranklySlider.builder()
                .bounds(x, y, width, 20)
                .range(AnatomicaConfig.SIZE.min(), AnatomicaConfig.SIZE.max())
                .step(0.01)
                .initialValue(working.size())
                .label(Component.translatable("option.anatomica.size"))
                .formatterString(v -> String.format("%.0f%%", v * 100.0))
                .onValueChanged(v -> working.setSize((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider offsetSlider(String key, int x, int y, int width, float initial,
            java.util.function.Consumer<Float> setter) {
        return FranklySlider.builder()
                .bounds(x, y, width, 20)
                .range(-0.5, 0.5)
                .step(0.01)
                .initialValue(initial)
                .label(Component.translatable("option.anatomica." + key))
                .formatterString(v -> String.format("%.2f", v))
                .onValueChanged(v -> setter.accept((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider spreadSlider(int x, int y, int width) {
        return FranklySlider.builder()
                .bounds(x, y, width, 20)
                .range(AnatomicaConfig.SPREAD.min(), AnatomicaConfig.SPREAD.max())
                .step(0.005)
                .initialValue(working.spread())
                .label(Component.translatable("option.anatomica.spread"))
                .formatterString(v -> String.format("%.3f", v))
                .onValueChanged(v -> working.setSpread((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider bounceSlider(int x, int y) {
        return FranklySlider.builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .range(0.0, 1.0)
                .step(0.01)
                .initialValue(working.bounceStrength())
                .label(Component.translatable("option.anatomica.bounce_strength"))
                .formatterString(v -> String.format("%.0f%%", v * 100.0))
                .onValueChanged(v -> working.setBounceStrength((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider softnessSlider(int x, int y) {
        return FranklySlider.builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .range(0.0, 1.0)
                .step(0.01)
                .initialValue(working.softness())
                .label(Component.translatable("option.anatomica.softness"))
                .formatterString(v -> String.format("%.0f%%", v * 100.0))
                .onValueChanged(v -> working.setSoftness((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private void pushToServer() {
        AnatomicaClientNetworking.sendLocalConfig(working);
    }

    @Override
    protected void renderPanelContent(GuiGraphicsExtractor graphics, int panelX, int panelY,
            int mouseX, int mouseY, float delta) {
    }
}