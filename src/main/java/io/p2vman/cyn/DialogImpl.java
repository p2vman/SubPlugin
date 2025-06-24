package io.p2vman.cyn;

import com.google.gson.JsonObject;
import com.mojang.serialization.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DialogImpl implements Dialog {
    private final Holder<net.minecraft.server.dialog.Dialog> dialog;
    public DialogImpl(DialogType type, JsonObject object) {
        Optional<Holder.Reference<MapCodec<? extends net.minecraft.server.dialog.Dialog>>> a = BuiltInRegistries.DIALOG_TYPE.get(ResourceLocation.parse(type.id));
        this.dialog = Holder.direct(a.orElseThrow().value().decode(JsonOps.INSTANCE, new JsonMapLike(object)).getOrThrow());
    }


    @Override
    public void show(Player p) {
        ServerPlayer player = ((CraftPlayer) p).getHandle();

        player.openDialog(dialog);
    }
}
