package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.Game;
import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class GearManager implements Listener {
    private final Gear[] gear;

    public GearManager(WarpEffectManager warpEffectManager, Map<Player, PlayerData> playerData) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        gear = new Gear[] {
                new WarpGrenade(warpEffectManager, playerData.keySet()),
                new ThrowableTnt(warpEffectManager, playerData.keySet())
        };
    }

    public void destroy() {
        HandlerList.unregisterAll(this);

        for (Gear gear : gear) {
            gear.destroy();
        }
    }

    public Gear[] getGear() {
        return gear;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
//        event.getItemDrop().remove();
    }
}
