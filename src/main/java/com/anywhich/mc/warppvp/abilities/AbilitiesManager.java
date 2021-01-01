package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class AbilitiesManager {
    private final Map<Player, PlayerData> playerData;
    private final Map<Player, AbilityCooldown> playerCooldowns = new HashMap<>();
    private final Map<Abilities, Ability> abilities = new HashMap<>();
    private final int onTickTaskId;

    public AbilitiesManager(WarpEffectManager warpEffectManager, Map<Player, PlayerData> playerData) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);

        abilities.put(Abilities.HOP, new HopAbility(this, warpEffectManager));
        abilities.put(Abilities.CHARGE, new ChargeAbility(this, warpEffectManager));
        abilities.put(Abilities.SLAM, new SlamAbility(this, warpEffectManager));
        abilities.put(Abilities.BLAST, new BlastAbility(this, warpEffectManager));

        this.playerData = playerData;
        playerData.forEach((player, data) -> resetPlayerCooldown(player, abilities.get(data.selectedAbility).cooldown));
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onTickTaskId);
        for (Ability ability : abilities.values()) ability.destroy();
    }

    private void onTick() {
        playerCooldowns.forEach((player, cooldown) -> {
            if (cooldown.decrement()) {
                player.setExp(1 - cooldown.getPercentage());
                player.setLevel(0);
            }
        });
    }

    public boolean hasPlayerSelectedAbility(Player player, Abilities ability) {
        return playerData.containsKey(player) && playerData.get(player).selectedAbility == ability;
    }

    public boolean isPlayerAbilityReady(Player player) {
        return playerCooldowns.get(player).getPercentage() == 0;
    }

    public void resetPlayerCooldown(Player player, int cooldown) {
        playerCooldowns.put(player, new AbilityCooldown(cooldown));
    }
}

class AbilityCooldown {
    private final int cooldown;
    private int value;

    public AbilityCooldown(int cooldown) {
        this.cooldown = cooldown;
        value = cooldown;
    }

    public float getPercentage() {
        return value / (float)cooldown;
    }

    public boolean decrement() {
        if (value > 0) {
            value--;
            return true;
        } else return false;
    }
}