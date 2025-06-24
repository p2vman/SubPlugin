package io.p2vman.cyn.waypint;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.p2vman.cyn.Dialog;
import io.p2vman.cyn.DialogImpl;
import io.p2vman.cyn.DialogType;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.NumberConversions;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class WaypointManager implements Listener {
    public Map<Long, Waypoint> waypoints = new Object2ObjectArrayMap<>();

    public boolean has(Location location) {
        return waypoints.containsKey(pack(location));
    }

    public Optional<Waypoint> getOrCreate(Location location) {
        int x = NumberConversions.floor(location.x());
        int y = NumberConversions.floor(location.y());
        int z = NumberConversions.floor(location.z());
        World world = location.getWorld();

        if (!(world.getType(x,y,z) == Material.LODESTONE && world.getType(x,y-1,z) == Material.CHISELED_STONE_BRICKS)) {
            return Optional.empty();
        }

        return Optional.of(getOrDefault((((long)x & 0x3FFFFF) << 42) | (((long)y & 0x3FFFFF) << 21) | ((long)z & 0x1FFFFF), () -> {
            return new Waypoint(new Vector3i(x, y, z), new ObjectArrayList<>());
        }));
    }

    private long pack(int x, int y, int z) {
        return (((long)x & 0x3FFFFF) << 42) | (((long)y & 0x3FFFFF) << 21) | ((long)z & 0x1FFFFF);
    }

    private long pack(Location location) {
        return (((long)(int)location.x() & 0x3FFFFF) << 42) | (((long)(int)location.y() & 0x3FFFFF) << 21) | ((long)(int)location.z() & 0x1FFFFF);
    }

    @AllArgsConstructor
    public static class Waypoint {
        public Vector3i location;
        public List<Vector3i> link;
    }

    public Waypoint getOrDefault(Long key, Supplier<Waypoint> defaultValue) {
        Waypoint v;
        return (((v = waypoints.get(key)) != null) || waypoints.containsKey(key))
                ? v
                : defaultValue.get();
    }

    @EventHandler
    public void onPlayerRightClickBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() == Material.LODESTONE) {
                Player player = event.getPlayer();
                getOrCreate(clickedBlock.getLocation()).ifPresent((b) -> {
                    GsonComponentSerializer sr = GsonComponentSerializer.gson();
                    JsonObject dialog_body = new JsonObject();
                    dialog_body.addProperty("title", "WayPoint ["+b.location.x+":"+b.location.y+":"+b.location.z+"]");
                    dialog_body.addProperty("can_close_with_escape", true);

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

                    dialog.show(player);
                });
            }
        }
    }
}
