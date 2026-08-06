package com.frank1o3.anatomica.client.gui;

import com.frank1o3.anatomica.config.BodyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * Drag either corner of the rectangle over the player's own skin to pick which
 * region
 * of it the body mesh samples from. Anatomica's models are one continuous UV
 * space
 * (not six discrete cuboid faces), so there's only ever one rectangle to manage
 * —
 * this is a different problem shape than a per-face UV editor, not a shrunk
 * copy of one.
 */
public final class TextureRegionPicker extends AbstractWidget {

    private static final int TEXTURE_SIZE = 64;
    private static final int HANDLE_RADIUS = 3;

    private final Consumer<int[]> onCommit;
    private int x1, y1, x2, y2;
    private boolean draggingMin, draggingMax;

    public TextureRegionPicker(int x, int y, int size, BodyConfig config, Consumer<int[]> onCommit) {
        super(x, y, size, size, Component.empty());
        this.onCommit = onCommit;
        this.x1 = config.textureX1();
        this.y1 = config.textureY1();
        this.x2 = config.textureX2();
        this.y2 = config.textureY2();
    }

    private float scale() {
        return (float) width / TEXTURE_SIZE;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        var player = Minecraft.getInstance().player;
        graphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0xFF_555577);
        if (player != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, player.getSkin().body().id(),
                    getX(), getY(), 0, 0, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
        } else {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF_222222);
        }

        int rx1 = getX() + (int) (x1 * scale());
        int ry1 = getY() + (int) (y1 * scale());
        int rx2 = getX() + (int) (x2 * scale());
        int ry2 = getY() + (int) (y2 * scale());

        graphics.fill(rx1, ry1, rx2, ry1 + 1, 0xFF_66CC66);
        graphics.fill(rx1, ry2 - 1, rx2, ry2, 0xFF_66CC66);
        graphics.fill(rx1, ry1, rx1 + 1, ry2, 0xFF_66CC66);
        graphics.fill(rx2 - 1, ry1, rx2, ry2, 0xFF_66CC66);

        drawHandle(graphics, rx1, ry1);
        drawHandle(graphics, rx2, ry2);
    }

    private void drawHandle(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.fill(cx - HANDLE_RADIUS, cy - HANDLE_RADIUS, cx + HANDLE_RADIUS, cy + HANDLE_RADIUS, 0xFF_FFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) ((event.x() - getX()) / scale());
        int my = (int) ((event.y() - getY()) / scale());
        double distMin = Math.hypot(mx - x1, my - y1);
        double distMax = Math.hypot(mx - x2, my - y2);
        draggingMin = distMin <= distMax;
        draggingMax = !draggingMin;
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        int mx = Mth.clamp((int) ((event.x() - getX()) / scale()), 0, TEXTURE_SIZE);
        int my = Mth.clamp((int) ((event.y() - getY()) / scale()), 0, TEXTURE_SIZE);
        if (draggingMin) {
            x1 = Math.min(mx, x2 - 1);
            y1 = Math.min(my, y2 - 1);
        } else if (draggingMax) {
            x2 = Math.max(mx, x1 + 1);
            y2 = Math.max(my, y1 + 1);
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (draggingMin || draggingMax) {
            onCommit.accept(new int[] { x1, y1, x2, y2 });
        }
        draggingMin = draggingMax = false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
    }
}