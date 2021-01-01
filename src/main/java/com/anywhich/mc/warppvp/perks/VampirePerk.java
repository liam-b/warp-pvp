package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class VampirePerk extends Perk {
    public static final Perks NAME = Perks.VAMPIRE;
    public static final int EXTRA_HEARTS = 5;

    private final Map<Player, PlayerData> playerData;

    public VampirePerk(PerksManager perksManager, Map<Player, PlayerData> playerData) {
        super(perksManager);
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && playerData.containsKey(event.getEntity()) && event.getEntity() != killer && perksManager.hasPlayerSelectedPerk(killer, NAME)) {
            killer.setAbsorptionAmount(EXTRA_HEARTS);
        }
    }
}
