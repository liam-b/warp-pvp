package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.gear.GearManager;
import com.anywhich.mc.warppvp.abilities.AbilitiesManager;
import com.anywhich.mc.warppvp.perks.PerksManager;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Game {
    public final WarpEffectManager warpEffectManager;
    public final GearManager gearManager;
    public final AbilitiesManager abilitiesManager;
    public final PerksManager perksManager;
    public final EquipmentManager equipmentManager;
    public final PlayerManager playerManager;
    public final ScoreboardManager scoreboardManager;

    public final Map<Player, PlayerData> playerData;

    public Game(WarpPvpConfig config, Map<Player, PlayerData> playerData, World world) {
        this.playerData = playerData.entrySet().stream().filter(entry -> entry.getValue().isEligibleToPlay()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        warpEffectManager = new WarpEffectManager(this.playerData, world);
        gearManager = new GearManager(warpEffectManager, this.playerData);
        abilitiesManager = new AbilitiesManager(warpEffectManager, this.playerData);
        perksManager = new PerksManager(this.playerData);
        equipmentManager = new EquipmentManager(this.playerData, gearManager);
        playerManager = new PlayerManager(this.playerData, world, config);
        scoreboardManager = new ScoreboardManager(this.playerData.keySet());

        this.playerData.keySet().forEach(player -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(0);
            player.getInventory().clear();
            equipmentManager.supplyEquipment(player);
        });
        playerManager.randomlySpreadPlayers();
    }

    public void destroy() {
        playerData.keySet().forEach(player -> {
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(0);
            player.getInventory().clear();
        });

        warpEffectManager.destroy();
        gearManager.destroy();
        abilitiesManager.destroy();
        perksManager.destroy();
        equipmentManager.destroy();
        playerManager.destroy();
        scoreboardManager.destroy();
    }
}
