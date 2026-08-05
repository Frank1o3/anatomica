package com.frank1o3.anatomica.client.gui.screen;

import com.frank1o3.anatomica.config.BodyConfig;
import com.frank1o3.anatomica.model.ModelFactory;
import com.frank1o3.anatomica.registry.AnatomicaRegistries;
import com.frank1o3.franklylib.client.gui.BaseFranklyScreen;
import com.frank1o3.franklylib.client.gui.FranklyButton;
import com.frank1o3.franklylib.client.gui.FranklyDropdown;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets the player pick which registered physics engine and deformable model
 * drive
 * their body attachment, via two {@link FranklyDropdown}s populated directly
 * from
 * {@link AnatomicaRegistries}. Any mod registering its own engine/model shows
 * up here
 * automatically — nothing about this screen is aware of the built-in
 * {@code softbody}/{@code box}/{@code organic} entries specifically.
 */
public final class ModelSelectScreen extends BaseFranklyScreen {

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 120;

    private final BodyConfig working;
    private final Runnable onChange;

    // Built once in init() rather than calling factory.create() from labelMapper
    // every
    // frame a dropdown is open/rendering — model construction (especially the
    // subdivided organic mesh) isn't free enough to redo per-frame just for a name.
    private final Map<Identifier, Component> modelDisplayNames = new HashMap<>();

    public ModelSelectScreen(Screen parent, BodyConfig working, Runnable onChange) {
        super(Component.translatable("screen.anatomica.select_model"), parent, PANEL_WIDTH, PANEL_HEIGHT);
        this.working = working;
        this.onChange = onChange;
    }

    @Override
    protected void init() {
        super.init();

        int x = panelX() + 12;
        int y = panelY() + 28;
        int width = PANEL_WIDTH - 24;

        List<Identifier> physicsEngineIds = new ArrayList<>();
        AnatomicaRegistries.PHYSICS_ENGINES.keySet().forEach(physicsEngineIds::add);

        addRenderableWidget(FranklyDropdown.<Identifier>builder()
                .bounds(x, y, width, 20)
                .options(physicsEngineIds)
                .current(working.physicsEngineId())
                .labelMapper(id -> Component.literal(id.getPath()))
                .onSelect(id -> {
                    working.setPhysicsEngineId(id);
                    onChange.run();
                })
                .build());

        y += 28;

        List<Identifier> modelIds = new ArrayList<>();
        AnatomicaRegistries.MODELS.keySet().forEach(modelIds::add);
        modelIds.forEach(id -> modelDisplayNames.put(id, modelDisplayName(id)));

        addRenderableWidget(FranklyDropdown.<Identifier>builder()
                .bounds(x, y, width, 20)
                .options(modelIds)
                .current(working.modelId())
                .labelMapper(id -> modelDisplayNames.getOrDefault(id, Component.literal(id.getPath())))
                .onSelect(id -> {
                    working.setModelId(id);
                    onChange.run();
                })
                .build());

        y += 32;

        addRenderableWidget(FranklyButton.builder()
                .bounds(x, y, width, 20)
                .message(Component.translatable("gui.back"))
                .onPress(btn -> onClose())
                .build());
    }

    private Component modelDisplayName(Identifier id) {
        ModelFactory factory = AnatomicaRegistries.MODELS.get(id).get().value();
        return factory != null ? factory.create().displayName() : Component.literal(id.getPath());
    }

    @Override
    protected void renderPanelContent(GuiGraphicsExtractor graphics, int panelX, int panelY,
            int mouseX, int mouseY, float delta) {
    }
}
