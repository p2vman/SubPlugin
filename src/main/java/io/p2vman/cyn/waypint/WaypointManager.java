package io.p2vman.cyn.waypint;

import io.p2vman.cyn.Cyn;
import io.p2vman.cyn.WorldMap;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AllArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WaypointManager implements Listener {
    public WorldMap<Map<Long, Waypoint>> waypoints = new WorldMap<>();

    public Location findSafeSpawn(Location center) {
        Block base = center.getBlock();

        int[][] offsets = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        for (int[] offset : offsets) {
            int dx = offset[0];
            int dz = offset[1];

            Block floor = base.getRelative(dx, 0, dz);
            Block head = floor.getRelative(BlockFace.UP);

            if (((floor.getType().isSolid()
                    && head.getType().isAir()) || head.getType() == Material.SNOW)
                    && head.getRelative(BlockFace.UP).getType().isAir()) {
                return head.getLocation().add(0.5, 0, 0.5);
            }
        }

        Block head = base.getRelative(BlockFace.UP);

        if (base.getType().isSolid()
                && head.getType().isAir()
                && head.getRelative(BlockFace.UP).getType().isAir()) {
            return head.getLocation().add(0.5, 0, 0.5);
        }

        return null;
    }


    public WaypointManager(Cyn plugin) {
        plugin.getCommand("wtp").setExecutor((@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) -> {
            if (!(sender instanceof Player player)) return false;
            if (args.length != 6) {
                sender.sendMessage("This is Internal Command");
                return true;
            }

            World world = player.getWorld();

            try {
                getOrCreate(world, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])).ifPresent((b1) -> {
                    if (b1.location.distance((int) player.getX(), (int) player.getY(), (int) player.getZ()) < 8) {
                        getOrCreate(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])).ifPresentOrElse((b2) -> {
                            Location sb = findSafeSpawn(new Location(world, b2.location.x, b2.location.y-2, b2.location.z));
                            if (sb != null) player.teleport(sb);
                            else {
                                b1.link.remove(new Vector3i(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));
                            }
                        }, () -> {
                            b1.link.remove(new Vector3i(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));
                        });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean has(Location location) {
        return waypoints.computeIfAbsent(location.getWorld(), a).containsKey(pack(location));
    }

    public Function<Key, Object2ObjectArrayMap<Long, Waypoint>> a = (k) -> new Object2ObjectArrayMap<>();

    public Optional<Waypoint> getOrCreate(World world, int x, int y, int z) {
        if (!(world.getType(x,y,z) == Material.LODESTONE && world.getType(x,y-1,z) == Material.CHISELED_STONE_BRICKS)) {
            return Optional.empty();
        }

        return Optional.of(waypoints.computeIfAbsent(world, a).computeIfAbsent(
                (((long)x & 0x3FFFFF) << 42) | (((long)y & 0x3FFFFF) << 21) | ((long)z & 0x1FFFFF),
                key -> new Waypoint(new Vector3i(x, y, z), new ObjectArrayList<>())
        ));
    }


    public Optional<Waypoint> getOrCreate(Location location) {
        int x = NumberConversions.floor(location.x());
        int y = NumberConversions.floor(location.y());
        int z = NumberConversions.floor(location.z());
        World world = location.getWorld();

        if (!(world.getType(x,y,z) == Material.LODESTONE && world.getType(x,y-1,z) == Material.CHISELED_STONE_BRICKS)) {
            return Optional.empty();
        }

        return Optional.of(waypoints.computeIfAbsent(world, a).computeIfAbsent(
                (((long)x & 0x3FFFFF) << 42) | (((long)y & 0x3FFFFF) << 21) | ((long)z & 0x1FFFFF),
                key -> new Waypoint(new Vector3i(x, y, z), new ObjectArrayList<>())
        ));
    }

    private long pack(Vector3i vec) {
        long x = vec.x & 0x3FFFFF;
        long y = vec.y & 0x1FFFFF;
        long z = vec.z & 0x1FFFFF;

        return ((x) << 42) | (y << 21) | z;
    }

    private long pack(Location location) {
        long x = NumberConversions.floor(location.x()) & 0x3FFFFF; // 22 бита
        long y = NumberConversions.floor(location.y()) & 0x1FFFFF; // 21 бит
        long z = NumberConversions.floor(location.z()) & 0x1FFFFF; // 21 бит

        return (x << 42) | (y << 21) | z;
    }

    @AllArgsConstructor
    public static class Waypoint {
        public Vector3i location;
        public List<Vector3i> link;
    }

    NamespacedKey link_key = new NamespacedKey("cyn", "link");


    private Vector3i unpack(long packed) {
        int x = (int) ((packed >> 42) & 0x3FFFFF);
        int y = (int) ((packed >> 21) & 0x1FFFFF);
        int z = (int) (packed & 0x1FFFFF);

        if ((x & (1 << 21)) != 0) x |= ~0x3FFFFF;
        if ((y & (1 << 20)) != 0) y |= ~0x1FFFFF;
        if ((z & (1 << 20)) != 0) z |= ~0x1FFFFF;

        return new Vector3i(x, y, z);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.LODESTONE) {
            long packed = pack(block.getLocation());
            Map<Long, Waypoint> map = waypoints.get(block.getWorld());
            if (map != null && map.containsKey(packed)) {
                map.remove(packed);
            }
        }
    }

    @EventHandler
    public void onPlayerRightClickBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() == Material.LODESTONE) {
                Player player = event.getPlayer();
                getOrCreate(clickedBlock.getLocation()).ifPresent((b) -> {
                    ItemStack stack = event.getItem();
                    if (stack != null && stack.getType() == Material.PAPER) {
                        stack.editPersistentDataContainer((container) -> {
                            if (container.has(link_key)) {
                                long ps = container.get(link_key, PersistentDataType.LONG);
                                Vector3i pos = unpack(ps);


                                double dist = b.location.distance(pos);

                                if (dist > 200 || dist < 2) {
                                    player.sendMessage(Component.text("Invalid distance."));
                                }
                                else {
                                    if (b.link.contains(pos)) {
                                        b.link.remove(pos);
                                        player.sendMessage(Component.text("Link remove."));
                                    } else {
                                        b.link.add(pos);
                                        player.sendMessage(Component.text("Link add."));
                                    }
                                }

                                container.remove(link_key);
                                player.sendMessage(Component.text("Link end."));
                            } else {
                                container.set(link_key, PersistentDataType.LONG, pack(b.location));
                                player.sendMessage(Component.text("Link start."));
                            }
                        });
                        return;
                    }


                    CommonDialogData data = new CommonDialogData(
                            net.minecraft.network.chat.Component.literal("WayPoint ["+b.location.x+":"+b.location.y+":"+b.location.z+"]"),
                            Optional.empty(),
                    true,
                            false,
                            DialogAction.CLOSE,
                            List.of(new PlainMessage(net.minecraft.network.chat.Component.literal("test"), 200)),
                            new ObjectArrayList<>()
                    );

                    List<ActionButton> buttons = new ObjectArrayList<>();

                    for (Vector3i vec : b.link) {
                        CommonButtonData data1 = new CommonButtonData(
                                net.minecraft.network.chat.Component.literal(vec.x+":"+vec.y+":"+vec.z),
                                50
                        );

                        buttons.add(new ActionButton(data1, Optional.of(new StaticAction(new ClickEvent.RunCommand("/wtp " +b.location.x+" "+b.location.y+" "+b.location.z+ " "+vec.x+" "+vec.y+" "+vec.z)))));
                    }
                    if (b.link.isEmpty()) {
                        CommonButtonData data1 = new CommonButtonData(
                                net.minecraft.network.chat.Component.literal("Help"),
                                30
                        );


                        buttons.add(new ActionButton(data1, Optional.of(new StaticAction(new ClickEvent.OpenUrl(URI.create("https://www.alax.com/"))))));
                    }


                    MultiActionDialog dialog = new MultiActionDialog(data, buttons, Optional.empty(), 4);


                    player.playSound(player.getLocation(), Sound.BLOCK_LODESTONE_HIT, 5, 1);
                    ServerPlayer p = ((CraftPlayer) player).getHandle();

                    p.openDialog(Holder.direct(dialog));
                });
            }
        }
    }
}
