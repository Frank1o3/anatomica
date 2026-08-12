package com.frank1o3.anatomica.client.gui;

import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
import com.frank1o3.franklylib.client.gui.style.FranklyUiStyle;
import com.frank1o3.franklylib.client.gui.style.FranklyUiStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A FranklyLib-styled widget for per-face UV editing on the player's skin
 * backdrop.
 * Renders face bounding boxes with labeled/colored borders, allowing face
 * selection and corner dragging.
 */
public final class FaceUVPicker extends AbstractWidget {

    private static final int TEXTURE_SIZE = 64;
    private static final int HANDLE_RADIUS = 3;

    @SuppressWarnings("unused")
    private final int side; // -1 for Left, 1 for Right
    private final Consumer<UVLayout> onCommit;
    private final Consumer<UVDirection> onSelectFace;

    private UVLayout layout;
    private UVDirection selectedDirection = UVDirection.NORTH;

    private boolean draggingMin;
    private boolean draggingMax;
    private @Nullable Identifier style;

    public FaceUVPicker(int x, int y, int size, int side, UVLayout layout,
            Consumer<UVLayout> onCommit, Consumer<UVDirection> onSelectFace) {
        super(x, y, size, size, Component.empty());
        this.side = side;
        this.layout = layout.copy();
        this.onCommit = onCommit;
        this.onSelectFace = onSelectFace;
    }

    public void updateLayout(UVLayout newLayout) {
        this.layout = newLayout.copy();
    }

    public UVDirection getSelectedDirection() {
        return selectedDirection;
    }

    public void setSelectedDirection(UVDirection dir) {
        if (dir != null && dir != this.selectedDirection) {
            this.selectedDirection = dir;
            if (onSelectFace != null) {
                onSelectFace.accept(dir);
            }
        }
    }

    /**
     * Sets the selected direction without firing the onSelectFace callback. Use for
     * initial sync.
     */
    public void setSelectedDirectionSilently(UVDirection dir) {
        if (dir != null) {
            this.selectedDirection = dir;
        }
    }

    public UVLayout getLayout() {
        return layout;
    }

    /** Applies a FranklyLib resource-pack style to the picker frame. */
    public FaceUVPicker style(@Nullable Identifier style) {
        this.style = style;
        return this;
    }

    private float scale() {
        return (float) width / TEXTURE_SIZE;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        var player = Minecraft.getInstance().player;
        // Background frame & skin texture.
        FranklyUiStyles.resolve(style, new FranklyUiStyle(0xFF222222, 0xFF222222, 0xFF222222,
                0xFF555577, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0,
                FranklyUiStyle.BorderType.SQUARE, 0, 1))
                .drawBox(graphics, getX() - 1, getY() - 1, width + 2, height + 2, isHoveredOrFocused(), active);
        if (player != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, player.getSkin().body().texturePath(),
                    getX(), getY(), // destination x, y
                    0.0f, 0.0f, // source u, v (top-left of the skin)
                    width, height, // destination width, height (scaled up to widget size)
                    TEXTURE_SIZE, TEXTURE_SIZE, // srcWidth, srcHeight (sample the full 64x64)
                    TEXTURE_SIZE, TEXTURE_SIZE); // textureWidth, textureHeight (for UV normalization)
        } else {
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF_222222);
        }
        float s = scale();

        // Render all non-selected face quads first (faded)
        for (Map.Entry<UVDirection, UVQuad> entry : layout.getAllSides().entrySet()) {
            UVDirection dir = entry.getKey();
            UVQuad quad = entry.getValue();
            if (dir == selectedDirection || quad == null)
                continue;
            drawQuadBorder(graphics, dir, quad, s, true);
        }

        // Render selected face quad on top (full opacity + handles)
        UVQuad selectedQuad = layout.get(selectedDirection);
        if (selectedQuad != null) {
            drawQuadBorder(graphics, selectedDirection, selectedQuad, s, false);

            int rx1 = getX() + (int) (selectedQuad.x1() * s);
            int ry1 = getY() + (int) (selectedQuad.y1() * s);
            int rx2 = getX() + (int) (selectedQuad.x2() * s);
            int ry2 = getY() + (int) (selectedQuad.y2() * s);

            drawHandle(graphics, rx1, ry1);
            drawHandle(graphics, rx2, ry2);
        }
    }

    private void drawQuadBorder(GuiGraphicsExtractor graphics, UVDirection dir, UVQuad quad, float s, boolean faded) {
        int rx1 = getX() + (int) (quad.x1() * s);
        int ry1 = getY() + (int) (quad.y1() * s);
        int rx2 = getX() + (int) (quad.x2() * s);
        int ry2 = getY() + (int) (quad.y2() * s);

        int color = dir.getFaceColor(faded);

        // Top, bottom, left, right border lines
        graphics.fill(rx1, ry1, rx2, ry1 + 1, color);
        graphics.fill(rx1, ry2 - 1, rx2, ry2, color);
        graphics.fill(rx1, ry1, rx1 + 1, ry2, color);
        graphics.fill(rx2 - 1, ry1, rx2, ry2, color);

        // Short face code label inside box if clear
        if (!faded && rx2 - rx1 > 8 && ry2 - ry1 > 8) {
            graphics.text(Minecraft.getInstance().font, Component.literal(dir.getShortName()), rx1 + 2, ry1 + 2,
                    0xFF_FFFFFF, false);
        }
    }

    private void drawHandle(GuiGraphicsExtractor graphics, int cx, int cy) {
        graphics.fill(cx - HANDLE_RADIUS, cy - HANDLE_RADIUS, cx + HANDLE_RADIUS, cy + HANDLE_RADIUS, 0xFF_FFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) ((event.x() - getX()) / scale());
        int my = (int) ((event.y() - getY()) / scale());

        UVQuad selectedQuad = layout.get(selectedDirection);
        if (selectedQuad != null) {
            double distMin = Math.hypot(mx - selectedQuad.x1(), my - selectedQuad.y1());
            double distMax = Math.hypot(mx - selectedQuad.x2(), my - selectedQuad.y2());

            if (distMin <= 4) {
                draggingMin = true;
                draggingMax = false;
                return;
            } else if (distMax <= 4) {
                draggingMax = true;
                draggingMin = false;
                return;
            }
        }

        // Check if clicking inside another face quad to select it
        for (Map.Entry<UVDirection, UVQuad> entry : layout.getAllSides().entrySet()) {
            UVQuad q = entry.getValue();
            if (q != null) {
                int minX = Math.min(q.x1(), q.x2());
                int maxX = Math.max(q.x1(), q.x2());
                int minY = Math.min(q.y1(), q.y2());
                int maxY = Math.max(q.y1(), q.y2());
                if (mx >= minX - 1 && mx <= maxX + 1 && my >= minY - 1 && my <= maxY + 1) {
                    setSelectedDirection(entry.getKey());
                    return;
                }
            }
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        UVQuad quad = layout.get(selectedDirection);
        if (quad == null)
            return;

        int mx = Mth.clamp((int) ((event.x() - getX()) / scale()), 0, TEXTURE_SIZE);
        int my = Mth.clamp((int) ((event.y() - getY()) / scale()), 0, TEXTURE_SIZE);

        if (draggingMin) {
            int newX1 = Math.min(mx, quad.x2() - 1);
            int newY1 = Math.min(my, quad.y2() - 1);
            layout.put(selectedDirection, new UVQuad(newX1, newY1, quad.x2(), quad.y2()));
        } else if (draggingMax) {
            int newX2 = Math.max(mx, quad.x1() + 1);
            int newY2 = Math.max(my, quad.y1() + 1);
            layout.put(selectedDirection, new UVQuad(quad.x1(), quad.y1(), newX2, newY2));
        }
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        if (draggingMin || draggingMax) {
            if (onCommit != null) {
                onCommit.accept(layout);
            }
        }
        draggingMin = draggingMax = false;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
    }
}
