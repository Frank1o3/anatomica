package com.frank1o3.anatomica.client.gui.screen;

import com.frank1o3.anatomica.client.gui.FaceUVPicker;
import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.client.config.AnatomicaConfig;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;
import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.config.Services;
import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
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

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 250;
    private static final int PADDING = 12;
    private static final int ROW_HEIGHT = 22;
    private static final int SLIDER_WIDTH = PANEL_WIDTH - (PADDING * 2);

    private enum Tab {
        GENERAL, PHYSICS, MODEL, UV
    }

    private enum Side {
        LEFT(-1, "option.anatomica.side.left"),
        RIGHT(1, "option.anatomica.side.right");

        final int id;
        final String translationKey;

        Side(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }
    }

    private Tab currentTab = Tab.GENERAL;
    private Side currentUvSide = Side.LEFT;
    private UVDirection currentUvFace = UVDirection.NORTH;
    private IBodyConfig working;
    private FaceUVPicker faceUvPicker;

    public BodyCustomizationScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.anatomica.body_customization"), parent, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        if (working == null) {
            Minecraft client = Minecraft.getInstance();
            working = client.player != null
                    ? Services.entityBodyData().get(client.player.getUUID()).copy()
                    : Services.bodyConfigSerializer().createDefaultBodyConfig();
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
        int previewWidth = 88;
        int sliderWidth = SLIDER_WIDTH - previewWidth - PADDING;

        addRenderableWidget(FranklyEntityPreviewWidget.builder()
                .bounds(x + sliderWidth + PADDING, y, previewWidth, 140)
                .previewSize(36)
                .entity(() -> Minecraft.getInstance().player)
                .build());

        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(x, y, 14, 14)
                .label(Component.translatable("option.anatomica.enable_breasts"))
                .checked(working.breastsEnabled())
                .onToggle(value -> {
                    working.setBreastsEnabled(value);
                    pushToServer();
                })
                .build());
        y += ROW_HEIGHT;

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
        addRenderableWidget(cleavageSlider(x, y, sliderWidth));
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
                    rebuildWidgets();
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
        y += 28;

        addDoneButton();
    }

    private Component modelDisplayName(Identifier id) {
        ModelFactory factory = AnatomicaRegistries.MODELS.get(id).get().value();
        return factory != null ? factory.create().displayName() : Component.literal(id.getPath());
    }

    private void initUvTab(int x, int y) {
        int pickerSize = 140;
        int rightX = x + pickerSize + 12;
        int rightWidth = SLIDER_WIDTH - pickerSize - 12;

        if (working.independentSides()) {
            addRenderableWidget(FranklyTabBar.<Side>builder()
                    .bounds(x, y, pickerSize, 16)
                    .tabs(List.of(Side.LEFT, Side.RIGHT))
                    .labelMapper(side -> Component.translatable(side.translationKey))
                    .current(currentUvSide)
                    .onSelect(side -> {
                        currentUvSide = side;
                        rebuildWidgets();
                    })
                    .build());
            y += 18;
        }

        UVLayout currentLayout = currentUvSide == Side.LEFT ? working.leftUvLayout() : working.rightUvLayout();

        Consumer<UVLayout> commitUv = layout -> {
            if (currentUvSide == Side.LEFT) {
                working.setLeftUvLayout(layout);
                if (!working.independentSides()) {
                    working.setRightUvLayout(layout.copy());
                }
            } else {
                working.setRightUvLayout(layout);
            }
            pushToServer();
            if (faceUvPicker != null) {
                faceUvPicker.updateLayout(layout);
            }
        };

        Consumer<UVDirection> onSelectFace = face -> {
            currentUvFace = face;
            rebuildWidgets();
        };

        faceUvPicker = new FaceUVPicker(x, y, pickerSize, currentUvSide.id, currentLayout, commitUv, onSelectFace);
        faceUvPicker.setSelectedDirectionSilently(currentUvFace);
        addRenderableWidget(faceUvPicker);

        // Right column: Face selection & Quad sliders
        int ry = panelY() + 42;

        addRenderableWidget(FranklyTabBar.<UVDirection>builder()
                .bounds(rightX, ry, rightWidth, 16)
                .tabs(List.of(UVDirection.NORTH, UVDirection.WEST, UVDirection.EAST, UVDirection.DOWN, UVDirection.UP))
                .labelMapper(dir -> Component.literal(dir.getShortName()))
                .current(currentUvFace)
                .onSelect(dir -> {
                    currentUvFace = dir;
                    if (faceUvPicker != null) {
                        faceUvPicker.setSelectedDirection(dir);
                    }
                    rebuildWidgets();
                })
                .build());
        ry += 20;

        UVQuad selectedQuad = currentLayout.get(currentUvFace);
        if (selectedQuad == null) {
            selectedQuad = new UVQuad(0, 0, 16, 16);
        }

        UVQuad finalQuad = selectedQuad;
        addRenderableWidget(uvCoordSlider("X1", rightX, ry, rightWidth, finalQuad.x1(), v -> {
            UVLayout updated = currentLayout.copy();
            updated.put(currentUvFace, finalQuad.withX1(v));
            commitUv.accept(updated);
        }));
        ry += 18;

        addRenderableWidget(uvCoordSlider("Y1", rightX, ry, rightWidth, finalQuad.y1(), v -> {
            UVLayout updated = currentLayout.copy();
            updated.put(currentUvFace, finalQuad.withY1(v));
            commitUv.accept(updated);
        }));
        ry += 18;

        addRenderableWidget(uvCoordSlider("X2", rightX, ry, rightWidth, finalQuad.x2(), v -> {
            UVLayout updated = currentLayout.copy();
            updated.put(currentUvFace, finalQuad.withX2(v));
            commitUv.accept(updated);
        }));
        ry += 18;

        addRenderableWidget(uvCoordSlider("Y2", rightX, ry, rightWidth, finalQuad.y2(), v -> {
            UVLayout updated = currentLayout.copy();
            updated.put(currentUvFace, finalQuad.withY2(v));
            commitUv.accept(updated);
        }));
        ry += 22;

        addRenderableWidget(FranklyButton.builder()
                .bounds(rightX, ry, rightWidth, 18)
                .message(Component.translatable("option.anatomica.reset_uv"))
                .onPress(btn -> {
                    UVLayout defaultLayout = currentUvSide == Side.LEFT ? UVLayout.DEFAULT_LEFT
                            : UVLayout.DEFAULT_RIGHT;
                    commitUv.accept(defaultLayout.copy());
                    rebuildWidgets();
                })
                .build());

        addDoneButton();
    }

    private void addDoneButton() {
        addRenderableWidget(FranklyButton.builder()
                .bounds(panelX() + PADDING, panelY() + PANEL_HEIGHT - 24, SLIDER_WIDTH, 20)
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
                .step(0.001)
                .initialValue(working.spread())
                .label(Component.translatable("option.anatomica.spread"))
                .formatterString(v -> String.format("%.3f", v))
                .onValueChanged(v -> working.setSpread((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider cleavageSlider(int x, int y, int width) {
        return FranklySlider.builder()
                .bounds(x, y, width, 20)
                .range(AnatomicaConfig.CLEAVAGE.min(), AnatomicaConfig.CLEAVAGE.max())
                .step(0.001)
                .initialValue(working.cleavage())
                .label(Component.translatable("option.anatomica.cleavage"))
                .formatterString(v -> String.format("%.3f", v))
                .onValueChanged(v -> working.setCleavage((float) (double) v))
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

    private FranklySlider uvCoordSlider(String label, int x, int y, int width, int initial, Consumer<Integer> setter) {
        return FranklySlider.builder()
                .bounds(x, y, width, 16)
                .range(0, 64)
                .step(1)
                .initialValue(initial)
                .label(Component.literal(label))
                .formatterString(v -> String.format("%d px", (int) Math.round(v)))
                .onValueChanged(v -> setter.accept((int) Math.round(v)))
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
