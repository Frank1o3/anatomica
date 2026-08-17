package com.frank1o3.anatomica.uv;

import net.minecraft.network.chat.Component;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Directions with editable UV regions. SOUTH is intentionally omitted: Anatomica
 * attachment meshes sit against the player model and never render a back face.
 */
public enum UVDirection {
    EAST("east", "East", "E", 0xFFFF4444, new Vector3f(1, 0, 0)),
    WEST("west", "West", "W", 0xFF44FF44, new Vector3f(-1, 0, 0)),
    DOWN("down", "Down", "D", 0xFF4444FF, new Vector3f(0, -1, 0)),
    UP("up", "Up", "U", 0xFFFFFF44, new Vector3f(0, 1, 0)),
    NORTH("north", "Front", "N", 0xFFFF44FF, new Vector3f(0, 0, 1));

    private final String saveName;
    private final String displayName;
    private final String shortName;
    private final int baseColor;
    private final Vector3fc floatVector;

    UVDirection(String saveName, String displayName, String shortName, int baseColor, Vector3f vector) {
        this.saveName = saveName;
        this.displayName = displayName;
        this.shortName = shortName;
        this.baseColor = baseColor;
        this.floatVector = vector;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public int getBaseColor() {
        return baseColor;
    }

    public int getFaceColor(boolean faded) {
        if (!faded) {
            return baseColor;
        }
        int alpha = 0x55;
        int rgb = baseColor & 0x00FFFFFF;
        return (alpha << 24) | rgb;
    }

    public Vector3f getUnitVector() {
        return new Vector3f(this.floatVector);
    }

    public Component getDirectionText(int side) {
        // side: -1 = left, 1 = right
        if (this == EAST || this == WEST) {
            boolean isLeft = (side == -1);
            boolean isInner = (this == EAST && isLeft) || (this == WEST && !isLeft);
            String key = isInner ? "option.anatomica.face.inner" : "option.anatomica.face.outer";
            return Component.translatable(key);
        }
        return Component.translatable("option.anatomica.face." + saveName);
    }
}
