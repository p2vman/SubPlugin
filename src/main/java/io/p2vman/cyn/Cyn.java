package io.p2vman.cyn;

import io.p2vman.cyn.waypint.WaypointManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Cyn extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WaypointManager(this), this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
