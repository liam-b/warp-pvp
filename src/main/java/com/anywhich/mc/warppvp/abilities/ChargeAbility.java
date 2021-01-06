package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class ChargeAbility extends Ability {
    public static final Abilities NAME = Abilities.CHARGE;
    public static final int COOLDOWN = 20 * 6;

    public static final double CHARGE_SPEED = 0.45;
    public static final double SELECTION_RANGE = 8;

    public static final int DESTINATION_DISABLE_TIMEOUT = 25;
    public static final double DESTINATION_DISABLE_RANGE = 1;
    public static final int HIT_COOLDOWN = 20;

    public static final double RANGE = 1.0;
    public static final double KNOCKBACK = 0.9;
    public static final double KNOCKBACK_ANGLE = 0.6;
    public static final double WARP = 0.4;
    public static final double DAMAGE = 4;
    private static final ImpactSignature impactSignature = new ImpactSignature(RANGE, KNOCKBACK, KNOCKBACK_ANGLE, WARP, DAMAGE);

    private final Map<Player, ChargeDestination> chargeDestinations = new HashMap<>();
    private final Map<LivingEntity, Integer> chargeHitCooldown = new HashMap<>();
    private final Random random = new Random();

    public ChargeAbility(AbilitiesManager abilitiesManager, WarpEffectManager warpEffectManager) {
        super(abilitiesManager, warpEffectManager, NAME, COOLDOWN);
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    protected void onUse(Player player) {
        Vector direction = player.getLocation().getDirection();
        Vector targetPosition = direction.multiply(SELECTION_RANGE).add(player.getLocation().toVector());
//        RayTraceResult traceResult = player.getWorld().rayTraceBlocks(player.getLocation(), direction, SELECTION_RANGE, FluidCollisionMode.NEVER, false);
//        if (traceResult != null) targetPosition = traceResult.getHitPosition();
        chargeDestinations.put(player, new ChargeDestination(targetPosition.toLocation(player.getWorld()), DESTINATION_DISABLE_TIMEOUT));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f);
    }

    protected void onTick() {
        List<Player> chargeDestinationsToRemove = new ArrayList<>();
        chargeDestinations.forEach((Player player, ChargeDestination destination) -> {
            Vector difference = destination.location.clone().subtract(player.getLocation()).toVector();
            if (difference.length() < DESTINATION_DISABLE_RANGE || destination.timeout <= 0) chargeDestinationsToRemove.add(player);
            else player.setVelocity(difference.normalize().multiply(CHARGE_SPEED));

            player.getLocation().getNearbyLivingEntities(RANGE).forEach(hitEntity -> {
                if (hitEntity != player) {
                    if (!chargeHitCooldown.containsKey(hitEntity)) {
                        impactSignature.applyFromPlayer(hitEntity, player, warpEffectManager);
                        chargeHitCooldown.put(hitEntity, HIT_COOLDOWN);
                    } else {
                        Vector hitPlayerDifference = hitEntity.getLocation().subtract(player.getLocation()).toVector().normalize();
                        hitPlayerDifference.setY(KNOCKBACK_ANGLE).normalize();
                        hitEntity.setVelocity(hitPlayerDifference.multiply(KNOCKBACK));
                    }
                }
            });

            player.getWorld().spawnParticle(Particle.BLOCK_DUST, player.getLocation().add(new Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5)), 2, Material.DIRT.createBlockData());

            player.setFallDistance(0);
            destination.timeout -= 1;
        });

        chargeDestinationsToRemove.forEach(chargeDestinations::remove);

        List<LivingEntity> hitCooldownsToRemove = new ArrayList<>();
        chargeHitCooldown.forEach((LivingEntity player, Integer cooldown) -> {
            if (cooldown > 0) chargeHitCooldown.put(player, cooldown - 1);
            else hitCooldownsToRemove.add(player);
        });

        hitCooldownsToRemove.forEach(chargeHitCooldown::remove);
    }
}

class ChargeDestination {
    public final Location location;
    public int timeout;

    public ChargeDestination(Location location, int timeout) {
        this.location = location;
        this.timeout = timeout;
    }
}