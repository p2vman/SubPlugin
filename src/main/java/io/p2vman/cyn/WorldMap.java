package io.p2vman.cyn;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WorldMap<T> extends Object2ObjectArrayMap<Key, T> {

    public T get(World k) {
        return super.get(k.key());
    }

    public T put(World s, T t) {
        return super.put(s.key(), t);
    }

    public T computeIfAbsent(World key, @NotNull Function<? super Key, ? extends T> mappingFunction) {
        return super.computeIfAbsent(key.key(), mappingFunction);
    }

    public boolean containsKey(World k) {
        return super.containsKey(k.key());
    }
}
