package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlastAbility extends Ability {
    public static final Abilities ABILITY = Abilities.BLAST;
    public static final int COOLDOWN = 20 * 10;

    public static final double RANGE = 4.1;
    public static final double KNOCKBACK = 2.1;
    public static final double KNOCKBACK_ANGLE = 0.35;
    public static final double WARP = 0.6;
    public static final double DAMAGE = 6;
    private static final ImpactSignature impactSignature = new ImpactSignature(RANGE, KNOCKBACK, KNOCKBACK_ANGLE, WARP, DAMAGE);

    public static final int EFFECT_DURATION = 100;
    public static final int SPEED_AMPLIFIER = 1;

//    private final Set<Player> pendingWarpOnLanding = new HashSet<>();

    public BlastAbility(AbilityManager abilityManager, WarpEffectManager warpEffectManager) {
        super(abilityManager, warpEffectManager, ABILITY, COOLDOWN);
    }

    protected void onUse(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_DURATION, SPEED_AMPLIFIER, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, EFFECT_DURATION, 1, false, false, true));


        impactSignature.createExplosion(player, warpEffectManager, 1000);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 1, 1.5f);
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_POLAR_BEAR_WARNING, 1, 0.7f);
    }

//    @EventHandler
//    public void onEntityDamage(EntityDamageEvent event) {
//        if (event.getEntityType() == EntityType.PLAYER) {
//            Player player = (Player) event.getEntity();
//
//            if (abilityManager.hasPlayerSelectedAbility(player, ABILITY)) {
//                if (event.getCause() == EntityDamageEvent.DamageCause.FALL && pendingWarpOnLanding.contains(player)) {
//                    warpEffectManager.increasePlayerWarpLevel(player, WARP_LEVEL);
//                    pendingWarpOnLanding.remove(player);
//                }
//            }
//        }
//    }
}
