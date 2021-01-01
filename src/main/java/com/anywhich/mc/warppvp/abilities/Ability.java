package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public abstract class Ability implements Listener {
//    private static final double GROUND_DISTANCE_LIMIT = 1;
    public final Abilities ABILITY;
    public final int COOLDOWN;

    protected final WarpEffectManager warpEffectManager;
    protected final AbilityManager abilityManager;
    private final int onTickTaskId;

    public Ability(AbilityManager abilityManager, WarpEffectManager warpEffectManager, Abilities ability, int cooldown) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
        this.warpEffectManager = warpEffectManager;
        this.abilityManager = abilityManager;
        this.COOLDOWN = cooldown;
        this.ABILITY = ability;
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onTickTaskId);
        HandlerList.unregisterAll(this);
    }

    abstract protected void onUse(Player player);
    protected void onTick() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (abilityManager.hasPlayerSelectedAbility(player, ABILITY)) {
            if (event.getItem() != null && event.getItem().getType() == Material.STONE_SWORD && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                if (abilityManager.isPlayerAbilityReady(player)) {
                    abilityManager.resetPlayerCooldown(player, COOLDOWN);
                    onUse(player);
                }
            }
        }
    }

//    private static double getDistanceFromGround(Entity entity){
//        Location location = entity.getLocation().clone();
//        while (!location.getBlock().getType().isSolid()) {
//            location.subtract(new Vector(0, 1, 0));
//        }
//
//        return entity.getLocation().getY() - location.getBlockY() - 1;
//    }
}
