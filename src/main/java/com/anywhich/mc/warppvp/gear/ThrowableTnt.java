package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.WarpEffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ThrowableTnt extends Gear {
    public static final int SUPPLY_PERIOD = 200;
    public static final int SUPPLY_MAX = 3;
    public static final int COUNT_MAX = 6;

    public static final ItemStack ITEM = new ItemStack(Material.TNT, 1);
    public static final int ITEM_SLOT = 2;

    public static final double KILL_REGISTER_RADIUS = 5;

    private final List<TNTPrimed> tntEntities = new ArrayList<>();

    public ThrowableTnt(WarpEffectManager warpEffectManager, Set<Player> players) {
        super(warpEffectManager, players, COUNT_MAX, ITEM, ITEM_SLOT, SUPPLY_MAX, SUPPLY_PERIOD);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.TNT && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
            event.setCancelled(true);

            TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
            tnt.setVelocity(player.getLocation().getDirection().multiply(0.5));
            tnt.setFuseTicks(50);
            tnt.setSource(player);
            tntEntities.add(tnt);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (tnt.getSource() != null) {
                event.getEntity().getLocation().getNearbyPlayers(KILL_REGISTER_RADIUS).forEach((Player player) -> {
                    if (player != tnt.getSource()) player.damage(0.1, tnt.getSource());
//                    if (player != tnt.getSource()) {
//                        EntityDamageEvent damageEvent = new EntityDamageEvent(tnt.getSource(), EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, 0);
//                        player.setLastDamageCause(damageEvent);
//                        Bukkit.getServer().getPluginManager().callEvent(damageEvent);
//                    }
                });
                tnt.getLocation().createExplosion(3, false, false);

                tntEntities.remove(tnt);
                event.setCancelled(true);
            }
        }
    }

    @Override
    protected void onTick() {
        List<TNTPrimed> toRemove = new ArrayList<>();
        tntEntities.forEach(tnt -> {
            if (tnt.getVelocity().getY() <= Float.MIN_NORMAL && tnt.isOnGround()) {
                tnt.setVelocity(tnt.getVelocity().setY(0.2));
                toRemove.add(tnt);
            }
        });

        tntEntities.removeAll(toRemove);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM && event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)     {
            event.setCancelled(true);
        }
    }
}