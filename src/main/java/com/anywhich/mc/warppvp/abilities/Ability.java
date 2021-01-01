package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Ability implements Listener {
//    private static final double GROUND_DISTANCE_LIMIT = 1;
    public final Abilities name;
    public final int cooldown;

    protected final WarpEffectManager warpEffectManager;
    protected final AbilitiesManager abilitiesManager;
    private final int onTickTaskId;

    public Ability(AbilitiesManager abilitiesManager, WarpEffectManager warpEffectManager, Abilities name, int cooldown) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
        this.warpEffectManager = warpEffectManager;
        this.abilitiesManager = abilitiesManager;
        this.cooldown = cooldown;
        this.name = name;
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
        if (abilitiesManager.hasPlayerSelectedAbility(player, name)) {
            if (event.getItem() != null && event.getItem().getType() == Material.STONE_SWORD && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                if (abilitiesManager.isPlayerAbilityReady(player)) {
                    abilitiesManager.resetPlayerCooldown(player, cooldown);
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
