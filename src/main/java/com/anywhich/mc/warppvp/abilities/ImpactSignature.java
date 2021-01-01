package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.ParticleHelper;
import com.anywhich.mc.warppvp.WarpEffectManager;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ImpactSignature {
    private final double range;
    private final double knockback;
    private final double knockbackAngle;
    private final double warpLevel;
    private final double damage;

    public ImpactSignature(double range, double knockback, double knockbackAngle, double warpLevel, double damage) {
        this.range = range;
        this.knockback = knockback;
        this.knockbackAngle = knockbackAngle;
        this.warpLevel = warpLevel;
        this.damage = damage;
    }

    public void createExplosion(Player player, WarpEffectManager warpEffectManager, int numParticles) {
        ParticleHelper.particleSphere(player.getLocation(), range, numParticles, Color.fromRGB(200, 0, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.5f);

        player.getLocation().getNearbyLivingEntities(range).forEach(hitEntity -> {
            if (hitEntity != player) {
                applyFromPlayer(hitEntity, player, warpEffectManager);
            }
        });

    }

    public void applyFromPlayer(LivingEntity hitEntity, Player player, WarpEffectManager warpEffectManager) {
        ParticleHelper.particleExplosion(hitEntity.getEyeLocation(), 22, 0.5);

        if (damage != 0) hitEntity.damage(damage, player);
        if (knockback != 0) {
            Vector hitPlayerDifference = hitEntity.getLocation().subtract(player.getLocation()).toVector().normalize();
            hitPlayerDifference.setY(knockbackAngle).normalize();
            hitEntity.setVelocity(hitPlayerDifference.multiply(knockback));
        }
        if (hitEntity instanceof Player && warpLevel != 0) warpEffectManager.increasePlayerWarpLevel((Player) hitEntity, warpLevel);
    }
}
