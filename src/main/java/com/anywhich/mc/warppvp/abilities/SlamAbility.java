package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class SlamAbility extends Ability {
    public static final Abilities ABILITY = Abilities.SLAM;
    public static final int COOLDOWN = 20 * 7;

    public static final double LAUNCH_VELOCITY = 1.2;

    public static final double RANGE = 1.8;
    public static final double KNOCKBACK = 0.8;
    public static final double KNOCKBACK_ANGLE = 0.8;
    public static final double WARP = 0.6;
    public static final double DAMAGE = 7;
    private static final ImpactSignature impactSignature = new ImpactSignature(RANGE, KNOCKBACK, KNOCKBACK_ANGLE, WARP, DAMAGE);
    public static final double IMPACT_VELOCITY_MIN = 0.5;

    private final Set<Player> pendingExplosionOnLanding = new HashSet<>();
    private final Set<Player> pendingLeaveGround = new HashSet<>();

    public SlamAbility(AbilityManager abilityManager, WarpEffectManager warpEffectManager) {
        super(abilityManager, warpEffectManager, ABILITY, COOLDOWN);
    }

    protected void onUse(Player player) {
        Vector horizontalDirection = player.getLocation().getDirection();
        Vector boostVelocity = horizontalDirection.multiply(LAUNCH_VELOCITY);
        player.setVelocity(boostVelocity);
        pendingExplosionOnLanding.add(player);
        pendingLeaveGround.add(player);

        player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation(), 3, 0, 0, 0, 0.1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1.2f);
    }

    @Override
    protected void onTick() {
        List<Player> toRemove = new ArrayList<>();
        pendingExplosionOnLanding.forEach(player -> {
            if (player.isOnGround() && player.getVelocity().getY() <= Double.MIN_NORMAL && player.getVelocity().length() <= IMPACT_VELOCITY_MIN) {
                if (!pendingLeaveGround.contains(player)) {
                    impactSignature.createExplosion(player, warpEffectManager, 200);
                    pendingExplosionOnLanding.remove(player);
                }

                toRemove.add(player);
            }

            if (!player.isOnGround()) pendingLeaveGround.remove(player);
        });

        toRemove.forEach(player -> {
            pendingExplosionOnLanding.remove(player);
            pendingLeaveGround.remove(player);
        });
    }

//    @EventHandler
//    public void onEntityDamage(EntityDamageEvent event) {
//        if (event.getEntityType() == EntityType.PLAYER) {
//            Player player = (Player) event.getEntity();
//
//            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && pendingExplosionOnLanding.contains(player)) {
//                impactSignature.createExplosion(player, warpEffectManager, 200);
//                pendingExplosionOnLanding.remove(player);
//            }
//        }
//    }
}
