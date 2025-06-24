package io.p2vman.cyn;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.p2vman.cyn.waypint.WaypointManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Cyn extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WaypointManager(), this);

        getCommand("testd").setExecutor((a,b,c,g) -> {
            GsonComponentSerializer sr = GsonComponentSerializer.gson();
            JsonObject dialog_body = new JsonObject();
            dialog_body.addProperty("title", "Rules");
            dialog_body.addProperty("external_title", "Rules");
            dialog_body.addProperty("can_close_with_escape", false);

            dialog_body.addProperty("after_action", "close");

            {
                JsonObject body = new JsonObject();
                body.addProperty("type", "minecraft:plain_message");

                JsonArray contents = new JsonArray();

                contents.add(sr.serializeToTree(
                        Component.text("test").appendNewline()
                ));

                body.add("contents", contents);

                dialog_body.add("body", body);
            }


            Dialog dialog = new DialogImpl(DialogType.NOTICE, dialog_body);

            dialog.show((Player) a);

            return false;
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
