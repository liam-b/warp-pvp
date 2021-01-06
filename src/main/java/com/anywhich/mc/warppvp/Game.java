package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.gear.GearManager;
import com.anywhich.mc.warppvp.abilities.AbilitiesManager;
import com.anywhich.mc.warppvp.perks.PerksManager;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import com.destroystokyo.paper.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class Game implements Listener {
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

    public Game(WarpPvpConfig config, Map<Player, PlayerData> playerData) {
        this.playerData = playerData;

        world = Bukkit.createWorld(WorldCreator.name(config.worlds.game));
        world.setDifficulty(Difficulty.NORMAL);

        warpEffectManager = new WarpEffectManager(playerData, world);
        gearManager = new GearManager(warpEffectManager, playerData);
        abilitiesManager = new AbilitiesManager(warpEffectManager, playerData);
        perksManager = new PerksManager(playerData);
        equipmentManager = new EquipmentManager(playerData, gearManager);
        playerManager = new PlayerManager(playerData, world, config);
        scoreboardManager = new ScoreboardManager(playerData.keySet());
        powerRingsManager = new PowerRingsManager(playerData, world, config);

        playerData.keySet().forEach(player -> {
            PlayerManager.resetPlayer(player);
            equipmentManager.supplyEquipment(player);
        });
        playerManager.randomlySpreadPlayers();
        startCountdown();

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy(World world) {
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
