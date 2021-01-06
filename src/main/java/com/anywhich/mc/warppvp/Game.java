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
    public final PowerRingsManager powerRingsManager;

    public final Map<Player, PlayerData> playerData;
    public final World world;

    private boolean inCountdown = true;

    public Game(WarpPvpConfig config, Map<Player, PlayerData> playerData, World world) {
        this.playerData = playerData.entrySet().stream().filter(entry -> !entry.getKey().isDead() && entry.getValue().isEligibleToPlay()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // TODO: when this game is in it's own world, the game should be aborted unless all players in the world are eligible

        warpEffectManager = new WarpEffectManager(this.playerData, world);
        gearManager = new GearManager(warpEffectManager, this.playerData);
        abilitiesManager = new AbilitiesManager(warpEffectManager, this.playerData);
        perksManager = new PerksManager(this.playerData);
        equipmentManager = new EquipmentManager(this.playerData, gearManager);
        playerManager = new PlayerManager(this.playerData, world, config);
        scoreboardManager = new ScoreboardManager(this.playerData.keySet());
        powerRingsManager = new PowerRingsManager(this.playerData, world, config);

        this.playerData.keySet().forEach(player -> {
            resetPlayer(player);
            equipmentManager.supplyEquipment(player);
        });
        playerManager.randomlySpreadPlayers();
        startCountdown();

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        playerData.keySet().forEach(this::resetPlayer);

        warpEffectManager.destroy();
        gearManager.destroy();
        abilitiesManager.destroy();
        perksManager.destroy();
        equipmentManager.destroy();
        playerManager.destroy();
        scoreboardManager.destroy();
        powerRingsManager.destroy();

        playerData.keySet().forEach(player -> {
            PlayerManager.resetPlayer(player);
            player.teleport(world.getSpawnLocation());
        });
    }

    private void startCountdown() {
        queueTitles(() -> {
            inCountdown = false;
            playerData.forEach((player, data) -> abilitiesManager.resetPlayerCooldown(player, abilitiesManager.abilities.get(data.selectedAbility).cooldown));
        }, "5", "4", "3", "2", "1", "Start!");
    }

    private void queueTitles(Runnable onFinish, String ... titles) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        for (int i = 0; i < titles.length; i++) {
            String countdownNumber = titles[i];
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> playerData.keySet().forEach(player -> player.sendTitle(countdownNumber, "", 2, 16, 2)), i * 20L);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, onFinish, (titles.length - 1) * 20L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        if (inCountdown) event.getTo().set(from.getX(), from.getY(), from.getZ());
    }
}
