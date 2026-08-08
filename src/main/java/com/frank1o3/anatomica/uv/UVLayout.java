package com.frank1o3.anatomica.uv;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class UVLayout {

    public static final UVLayout DEFAULT_LEFT = new UVLayout(
            new UVQuad(24, 21, 28, 26), // EAST
            new UVQuad(16, 21, 20, 26), // WEST
            new UVQuad(20, 25, 24, 27), // DOWN
            new UVQuad(20, 17, 24, 21), // UP
            new UVQuad(20, 21, 24, 26)  // NORTH
    );

    public static final UVLayout DEFAULT_RIGHT = new UVLayout(
            new UVQuad(28, 21, 32, 26), // EAST
            new UVQuad(20, 21, 24, 26), // WEST
            new UVQuad(24, 25, 28, 27), // DOWN
            new UVQuad(24, 17, 28, 21), // UP
            new UVQuad(24, 21, 28, 26)  // NORTH
    );

    private final EnumMap<UVDirection, @Nullable UVQuad> quads = new EnumMap<>(UVDirection.class);

    public UVLayout(Map<UVDirection, @Nullable UVQuad> map) {
        this.quads.putAll(map);
        fillMissing();
    }

    public UVLayout(UVQuad east, UVQuad west, UVQuad down, UVQuad up, UVQuad north) {
        quads.put(UVDirection.EAST, east);
        quads.put(UVDirection.WEST, west);
        quads.put(UVDirection.DOWN, down);
        quads.put(UVDirection.UP, up);
        quads.put(UVDirection.NORTH, north);
    }

    public UVLayout() {
        this(Collections.emptyMap());
    }

    private void fillMissing() {
        for (UVDirection dir : UVDirection.values()) {
            quads.putIfAbsent(dir, null);
        }
    }

    public void put(UVDirection dir, UVQuad quad) {
        quads.put(dir, quad);
    }

    public @Nullable UVQuad get(UVDirection dir) {
        return quads.get(dir);
    }

    public boolean has(UVDirection dir) {
        return quads.containsKey(dir) && quads.get(dir) != null;
    }

    public Map<UVDirection, @Nullable UVQuad> getAllSides() {
        return Collections.unmodifiableMap(quads);
    }

    public UVLayout copy() {
        UVLayout copy = new UVLayout();
        copy.quads.putAll(this.quads);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UVLayout other)) return false;
        return quads.equals(other.quads);
    }

    @Override
    public int hashCode() {
        return quads.hashCode();
    }
}
