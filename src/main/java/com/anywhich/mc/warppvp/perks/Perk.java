package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.WarpPvp;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Perk implements Listener {
    protected final PerksManager perksManager;

    public Perk(PerksManager perksManager) {
        this.perksManager = perksManager;

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }
}
