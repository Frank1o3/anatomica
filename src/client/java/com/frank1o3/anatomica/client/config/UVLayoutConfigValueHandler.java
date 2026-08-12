package com.frank1o3.anatomica.client.config;

import com.frank1o3.anatomica.uv.UVDirection;
import com.frank1o3.anatomica.uv.UVLayout;
import com.frank1o3.anatomica.uv.UVQuad;
import com.frank1o3.franklylib.config.ConfigFieldEntry;
import com.frank1o3.franklylib.config.ConfigValueException;
import com.frank1o3.franklylib.config.ConfigValueHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.EnumMap;
import java.util.Map;

/** JSON representation for the five editable faces in an {@link UVLayout}. */
final class UVLayoutConfigValueHandler implements ConfigValueHandler<UVLayout> {

    @Override
    public JsonElement toJson(UVLayout value) {
        JsonObject layout = new JsonObject();
        if (value == null) {
            return layout;
        }
        for (Map.Entry<UVDirection, UVQuad> entry : value.getAllSides().entrySet()) {
            UVQuad quad = entry.getValue();
            if (quad == null) {
                continue;
            }
            JsonObject quadJson = new JsonObject();
            quadJson.addProperty("x1", quad.x1());
            quadJson.addProperty("y1", quad.y1());
            quadJson.addProperty("x2", quad.x2());
            quadJson.addProperty("y2", quad.y2());
            layout.add(entry.getKey().getSaveName(), quadJson);
        }
        return layout;
    }

    @Override
    public UVLayout fromJson(JsonElement json) {
        if (!json.isJsonObject()) {
            throw new ConfigValueException("Expected a UV layout object");
        }
        Map<UVDirection, UVQuad> quads = new EnumMap<>(UVDirection.class);
        JsonObject layout = json.getAsJsonObject();
        for (UVDirection direction : UVDirection.values()) {
            JsonElement face = layout.get(direction.getSaveName());
            if (face == null || face.isJsonNull()) {
                continue;
            }
            if (!face.isJsonObject()) {
                throw new ConfigValueException("Expected an object for UV face '" + direction.getSaveName() + "'");
            }
            JsonObject quad = face.getAsJsonObject();
            quads.put(direction, new UVQuad(readCoordinate(quad, "x1"), readCoordinate(quad, "y1"),
                    readCoordinate(quad, "x2"), readCoordinate(quad, "y2")));
        }
        if (quads.isEmpty()) {
            throw new ConfigValueException("A UV layout must contain at least one face");
        }
        return new UVLayout(quads);
    }

    @Override
    public UVLayout clamp(UVLayout value, ConfigFieldEntry entry) {
        return value;
    }

    private static int readCoordinate(JsonObject quad, String name) {
        JsonElement value = quad.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new ConfigValueException("Expected numeric UV coordinate '" + name + "'");
        }
        return value.getAsInt();
    }
}
