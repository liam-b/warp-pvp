package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.ParticleHelper;
import com.anywhich.mc.warppvp.WarpEffectManager;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
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

    private static final double RANGE_MAX = 1.3;
    private static final double RANGE_MIN = 0.7;

    private static final double WARP_LEVEL_MAX = 0.2;
    private static final double WARP_LEVEL_MIN = 0.07;

    public WarpGrenade(WarpEffectManager warpEffectManager, Set<Player> players) {
        super(warpEffectManager, players, COUNT_MAX, ITEM, ITEM_SLOT, SUPPLY_MAX, SUPPLY_PERIOD);
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
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Location hitLocation = event.getEntity().getLocation();
        ParticleHelper.particleSphere(hitLocation, RANGE_MAX, 100, Color.fromRGB(60, 60, 60));

        hitLocation.getNearbyPlayers(RANGE_MAX).forEach(player -> {
            if (player != event.getEntity().getShooter() || true) {
                Vector playerTorso = player.getLocation().toVector().midpoint(player.getEyeLocation().toVector());
                double distanceToPlayer = hitLocation.toVector().distance(playerTorso);
                double normalisedDistance = map(distanceToPlayer, RANGE_MIN, RANGE_MAX, 0, 1);
                double warpLevel = map(1 - normalisedDistance, 0, 1, WARP_LEVEL_MIN, WARP_LEVEL_MAX);
                warpEffectManager.increasePlayerWarpLevel(player, warpLevel);
            }
        });
    }

    private double map(double number, double fromMin, double fromMax, double toMin, double toMax) {
        double value = (number - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
        return Math.max(toMin, Math.min(value, toMax));
    }
}
