package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.ParticleHelper;
import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class WarpGrenade extends Gear {
    public static final int SUPPLY_PERIOD = 100;
    public static final int SUPPLY_MAX = 6;
    public static final int COUNT_MAX = 12;

    public static final ItemStack ITEM = new ItemStack(Material.SNOWBALL, 1);
    public static final int ITEM_SLOT = 1;

    static {
        ItemMeta grenadeMeta = ITEM.getItemMeta();
        grenadeMeta.setDisplayName(ChatColor.RESET + "Warp Grenade");
        ITEM.setItemMeta(grenadeMeta);
    }

    private static final double RANGE = 1.3;
    private static final double WARP_DIRECT_HIT = 0.10;
    private static final double WARP_INDIRECT_HIT = 0.07;

    public WarpGrenade(WarpEffectManager warpEffectManager, Map<Player, PlayerData> playerData) {
        super(warpEffectManager, playerData, COUNT_MAX, ITEM, ITEM_SLOT, SUPPLY_MAX, SUPPLY_PERIOD);
    }

    @EventHandler
    public void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent event) {
        if (event.getProjectile().getType() == EntityType.SNOWBALL) {
            event.getItemStack().setAmount(event.getItemStack().getAmount() - 1);
            event.setCancelled(true);

            Player player = event.getPlayer();
            Snowball ball = player.getWorld().spawn(player.getEyeLocation(), Snowball.class);
            ball.setVelocity(player.getLocation().getDirection().multiply(1.5));
            ball.setShooter(player);

            player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1f);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Location hitLocation = event.getEntity().getLocation();
        ParticleHelper.particleSphere(hitLocation, RANGE, 100, Color.fromRGB(60, 60, 60));
        hitLocation.getWorld().playSound(hitLocation, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 2f);

        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
            Player hitPlayer = (Player) event.getHitEntity();
            if (hitPlayer != event.getEntity().getShooter()) {
                warpEffectManager.increasePlayerWarpLevel(hitPlayer, WARP_DIRECT_HIT);
            }
        } else {
            hitLocation.getNearbyPlayers(RANGE).forEach(player -> {
                if (player != event.getEntity().getShooter()) {
                    warpEffectManager.increasePlayerWarpLevel(player, WARP_INDIRECT_HIT);
                }
            });
        }
    }
}
