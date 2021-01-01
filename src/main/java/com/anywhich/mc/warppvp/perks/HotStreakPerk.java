package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class HotStreakPerk extends Perk {
    public static final Perks NAME = Perks.HOT_STREAK;
    private static final PotionEffect STRENGTH_EFFECT = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false, true); // TODO: make the strength effect less strong and increment it after each one or two kills
    public static final int STREAK_PER_LEVEL = 3;
    public static final int AMPLIFIER_MAX = 2;

    private final Map<Player, PlayerData> playerData;
    private final Map<Player, Integer> killStreaks = new HashMap<>();

    public HotStreakPerk(PerksManager perksManager, Map<Player, PlayerData> playerData) {
        super(perksManager);
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        killStreaks.remove(killed);

        Player killer = event.getEntity().getKiller();
        if (killer != null && playerData.containsKey(killed) && killed != killer && perksManager.hasPlayerSelectedPerk(killer, NAME)) {
            int killStreak = killStreaks.getOrDefault(killer, 0) + 1;
            killStreaks.put(killer, killStreak);

            int effectAmplifier = killStreak / STREAK_PER_LEVEL - 1;
            if (effectAmplifier >= 0) {
                killer.removePotionEffect(STRENGTH_EFFECT.getType());
                killer.addPotionEffect(STRENGTH_EFFECT.withAmplifier(Math.min(effectAmplifier, AMPLIFIER_MAX)));
            }
        }
    }
}
