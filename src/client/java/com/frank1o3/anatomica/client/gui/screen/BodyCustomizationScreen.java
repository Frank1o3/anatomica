package com.frank1o3.anatomica.client.gui.screen;

import com.frank1o3.anatomica.client.networking.AnatomicaClientNetworking;
import com.frank1o3.anatomica.config.AnatomicaConfig;
import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.data.EntityBodyData;
import com.frank1o3.franklylib.client.gui.BaseFranklyScreen;
import com.frank1o3.franklylib.client.gui.FranklyButton;
import com.frank1o3.franklylib.client.gui.FranklyCheckbox;
import com.frank1o3.franklylib.client.gui.FranklySlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * The primary body customization screen: one slider per continuous
 * {@link BodyConfig}
 * field, one checkbox per boolean field, and a button through to
 * {@link ModelSelectScreen} for picking the physics engine / model. Edits a
 * working
 * copy of the local player's config and only pushes it to the server (via
 * {@link AnatomicaClientNetworking#sendLocalConfig}) when a slider is released
 * or a
 * toggle is clicked — never continuously while dragging.
 */
public final class BodyCustomizationScreen extends BaseFranklyScreen {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 220;
    private static final int CONTENT_PADDING = 12;
    private static final int ROW_HEIGHT = 22;
    private static final int SLIDER_WIDTH = 216;

    private BodyConfig working;

    public BodyCustomizationScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.anatomica.body_customization"), parent, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        Minecraft client = Minecraft.getInstance();
        working = client.player != null
                ? EntityBodyData.get(client.player.getUUID()).copy()
                : new BodyConfig();

        int x = panelX() + CONTENT_PADDING;
        int y = panelY() + 24;
        int sliderX = x;

        addRenderableWidget(sizeSlider(sliderX, y));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_x", sliderX, y, working.offsetX(), working::setOffsetX));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_y", sliderX, y, working.offsetY(), working::setOffsetY));
        y += ROW_HEIGHT;
        addRenderableWidget(offsetSlider("offset_z", sliderX, y, working.offsetZ(), working::setOffsetZ));
        y += ROW_HEIGHT;
        addRenderableWidget(spreadSlider(sliderX, y));
        y += ROW_HEIGHT;
        addRenderableWidget(bounceSlider(sliderX, y));
        y += ROW_HEIGHT;
        addRenderableWidget(softnessSlider(sliderX, y));
        y += ROW_HEIGHT + 4;

        int checkboxX = x;
        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(checkboxX, y, 14, 14)
                .label(Component.translatable("option.anatomica.physics_enabled"))
                .checked(working.physicsEnabled())
                .onToggle(value -> {
                    working.setPhysicsEnabled(value);
                    pushToServer();
                })
                .build());
        y += 18;

        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(checkboxX, y, 14, 14)
                .label(Component.translatable("option.anatomica.independent_sides"))
                .checked(working.independentSides())
                .onToggle(value -> {
                    working.setIndependentSides(value);
                    pushToServer();
                })
                .build());
        y += 18;

        addRenderableWidget(FranklyCheckbox.builder()
                .bounds(checkboxX, y, 14, 14)
                .label(Component.translatable("option.anatomica.show_in_armor"))
                .checked(working.showInArmor())
                .onToggle(value -> {
                    working.setShowInArmor(value);
                    pushToServer();
                })
                .build());
        y += 22;

        addRenderableWidget(FranklyButton.builder()
                .bounds(x, y, SLIDER_WIDTH / 2 - 4, 20)
                .message(Component.translatable("screen.anatomica.select_model"))
                .onPress(btn -> minecraft.gui.setScreen(new ModelSelectScreen(this, working, this::pushToServer)))
                .build());

        addRenderableWidget(FranklyButton.builder()
                .bounds(x + SLIDER_WIDTH / 2 + 4, y, SLIDER_WIDTH / 2 - 4, 20)
                .message(Component.translatable("gui.done"))
                .onPress(btn -> onClose())
                .build());
    }

    private FranklySlider sizeSlider(int x, int y) {
        return FranklySlider.builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .range(AnatomicaConfig.SIZE.min(), AnatomicaConfig.SIZE.max())
                .step(0.01)
                .initialValue(working.size())
                .label(Component.translatable("option.anatomica.size"))
                .formatterString(v -> String.format("%.0f%%", v * 100.0))
                .onValueChanged(v -> working.setSize((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider offsetSlider(String translationKey, int x, int y, float initial,
            java.util.function.Consumer<Float> setter) {
        return FranklySlider.builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
                .range(-0.5, 0.5)
                .step(0.01)
                .initialValue(initial)
                .label(Component.translatable("option.anatomica." + translationKey))
                .formatterString(v -> String.format("%.2f", v))
                .onValueChanged(v -> setter.accept((float) (double) v))
                .onValueCommitted(v -> pushToServer())
                .build();
    }

    private FranklySlider spreadSlider(int x, int y) {
        return FranklySlider.builder()
                .bounds(x, y, SLIDER_WIDTH, 20)
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
        // Base panel chrome (dim overlay, border, title) is already drawn by
        // BaseFranklyScreen before this is called; nothing extra needed here yet. A
        // live entity preview (via FranklyGuiUtils.drawScaledEntityPreview) could be
        // added here later if the panel is widened to make room for one.
    }
}
