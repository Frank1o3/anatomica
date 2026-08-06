package com.frank1o3.anatomica.config.keys;

import com.frank1o3.anatomica.config.BodyConfigKey;
import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;

public final class UVLayoutConfigKey extends BodyConfigKey<UVLayout> {

    public UVLayoutConfigKey(String id, UVLayout defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public UVLayout clamp(UVLayout value) {
        if (value == null) return defaultValue();
        Map<UVDirection, UVQuad> clamped = new EnumMap<>(UVDirection.class);
        for (Map.Entry<UVDirection, UVQuad> entry : value.getAllSides().entrySet()) {
            UVDirection dir = entry.getKey();
            UVQuad quad = entry.getValue();
            if (quad != null) {
                clamped.put(dir, new UVQuad(quad.x1(), quad.y1(), quad.x2(), quad.y2()));
            }
        }
        return new UVLayout(clamped);
    }

    @Override
    public void write(CompoundTag tag, UVLayout value) {
        CompoundTag layoutTag = new CompoundTag();
        if (value != null) {
            for (Map.Entry<UVDirection, UVQuad> entry : value.getAllSides().entrySet()) {
                UVQuad quad = entry.getValue();
                if (quad != null) {
                    CompoundTag quadTag = new CompoundTag();
                    quadTag.putInt("x1", quad.x1());
                    quadTag.putInt("y1", quad.y1());
                    quadTag.putInt("x2", quad.x2());
                    quadTag.putInt("y2", quad.y2());
                    layoutTag.put(entry.getKey().getSaveName(), quadTag);
                }
            }
        }
        tag.put(id(), layoutTag);
    }

    @Override
    public UVLayout read(CompoundTag tag) {
        if (!tag.contains(id())) {
            return defaultValue();
        }
        CompoundTag layoutTag = tag.getCompound(id()).orElseGet(CompoundTag::new);
        Map<UVDirection, UVQuad> map = new EnumMap<>(UVDirection.class);
        for (UVDirection dir : UVDirection.values()) {
            if (layoutTag.contains(dir.getSaveName())) {
                CompoundTag quadTag = layoutTag.getCompound(dir.getSaveName()).orElseGet(CompoundTag::new);
                int x1 = quadTag.getIntOr("x1", 0);
                int y1 = quadTag.getIntOr("y1", 0);
                int x2 = quadTag.getIntOr("x2", 0);
                int y2 = quadTag.getIntOr("y2", 0);
                map.put(dir, new UVQuad(x1, y1, x2, y2));
            }
        }
        return map.isEmpty() ? defaultValue() : clamp(new UVLayout(map));
    }
}
