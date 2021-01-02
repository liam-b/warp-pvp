package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.WarpPvp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Perk implements Listener {
    protected final PerksManager perksManager;
    private final Perks name;

    public Perk(PerksManager perksManager, Perks name) {
        this.perksManager = perksManager;
        this.name = name;

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    protected void sendPerkActionbar(Player player, String message) {
        player.sendActionBar(Perks.COLOR + "[" + name.toCapital() + "] " + ChatColor.RESET + message);
    }
}
