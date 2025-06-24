package io.p2vman.cyn;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapLike;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.stream.Stream;

@AllArgsConstructor
public class JsonMapLike implements MapLike<JsonElement> {
    public JsonObject object;
    @Nullable
    @Override
    public JsonElement get(final JsonElement key) {
        final JsonElement element = object.get(key.getAsString());
        if (element instanceof JsonNull) {
            return null;
        }
        return element;
    }

    @Nullable
    @Override
    public JsonElement get(final String key) {
        final JsonElement element = object.get(key);
        if (element instanceof JsonNull) {
            return null;
        }
        return element;
    }

    @Override
    public Stream<Pair<JsonElement, JsonElement>> entries() {
        return object.entrySet().stream().map(e -> Pair.of(new JsonPrimitive(e.getKey()), e.getValue()));
    }

    @Override
    public String toString() {
        return "MapLike[" + object + "]";
    }
}
