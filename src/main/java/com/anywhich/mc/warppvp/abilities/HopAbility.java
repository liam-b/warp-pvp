package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class HopAbility extends Ability {
    public static final Abilities NAME = Abilities.HOP;
    public static final int COOLDOWN = 20 * 7;

    public static final double RANGE = 2.6;
    public static final double KNOCKBACK = 0.7;
    public static final double KNOCKBACK_ANGLE = 0.8;
    public static final double WARP = 0.35;
    public static final double DAMAGE = 3;
    private static final ImpactSignature impactSignature = new ImpactSignature(RANGE, KNOCKBACK, KNOCKBACK_ANGLE, WARP, DAMAGE);

    public static final double VERTICAL_VELOCITY = 1.45;
    public static final int SLOW_FALL_DURATION = 80; // could make it so that this is only removed when ground is touched

    private final Set<Player> hoppingPlayers = new HashSet<>();

    public HopAbility(AbilitiesManager abilitiesManager, WarpEffectManager warpEffectManager) {
        super(abilitiesManager, warpEffectManager, NAME, COOLDOWN);
    }

    protected void onUse(Player player) {
        player.setVelocity(player.getVelocity().setY(VERTICAL_VELOCITY));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, SLOW_FALL_DURATION, 0, false, false, false));
        hoppingPlayers.add(player);

        impactSignature.createExplosion(player, warpEffectManager, 200);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2, 0.5f);
    }

    @Override
    protected void onTick() {
        hoppingPlayers.forEach(player -> {
            if (player.getVelocity().getY() < 0 && player.getTicksLived() % 2 == 0) {
                player.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, player.getLocation().add(new Vector(0, -0.5, 0)), 1, 0, 0, 0, 0);
            }
        });

        hoppingPlayers.removeIf(player -> player.getPotionEffect(PotionEffectType.SLOW_FALLING) == null);
//        hoppingPlayers.removeIf(player -> player.isOnGround() && player.getVelocity().getY() <= 0);
    }
}
