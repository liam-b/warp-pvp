package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager implements Listener {
    private static final int REGEN_INTERVAL = 45;
    private static final PotionEffect KILL_REGEN_POTION = new PotionEffect(PotionEffectType.REGENERATION, 60, 2, false, false, true);
    private static final double SPAWN_PLAYER_EXCLUSION_RANGE = 10;
    private static final int SPAWN_EXCLUSION_ATTEMPTS_MAX = 8;

    private final int regenTaskId;
    private final Map<Player, PlayerData> playerData;
    private final List<Location> spawnLocations;

    public PlayerManager(Map<Player, PlayerData> playerData, World world, WarpPvpConfig config) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        regenTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::regenTask, 0, REGEN_INTERVAL);

        spawnLocations = config.playerSpawns.stream().map(vector -> vector.toLocation(world)).collect(Collectors.toList());
        this.playerData = playerData;
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(regenTaskId);
        HandlerList.unregisterAll(this);
    }

    public void randomlySpreadPlayers() {
        List<Player> players = new ArrayList<>(playerData.keySet());
        Collections.shuffle(spawnLocations);
        for (int i = 0; i < players.size(); i++) {
            if (!spawnLocations.isEmpty() && i % spawnLocations.size() < spawnLocations.size()) {
                Location location = spawnLocations.get(i % spawnLocations.size());
                players.get(i).teleport(location);
            }
        }
    }

    private void regenTask() {
        playerData.keySet().forEach(player -> increasePlayerHealth(player, 1));
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) event.setCancelled(true);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && playerData.containsKey(event.getEntity()) && playerData.containsKey(killer) && event.getEntity() != killer) {
            killer.addPotionEffect(KILL_REGEN_POTION);
            killer.playSound(killer.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 1);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (playerData.containsKey(event.getPlayer())) {
            int spawnAttempts = 0;
            Location potentialSpawn;
            do {
                potentialSpawn = random(spawnLocations);
                spawnAttempts++;
            }
            while (potentialSpawn.getNearbyPlayers(SPAWN_PLAYER_EXCLUSION_RANGE).stream().anyMatch(player -> player != event.getPlayer()) && spawnAttempts < SPAWN_EXCLUSION_ATTEMPTS_MAX); // TODO: could also check for tnt in area
            event.setRespawnLocation(potentialSpawn);
        }
    }

    private void increasePlayerHealth(Player player, double amount) {
        if (!player.isDead()) {
            double health = player.getHealth() + amount;
            player.setHealth(Math.min(health, 20));
        }
    }

    public static <T> T random(Collection<T> collection) {
        int num = (int) (Math.random() * collection.size());
        for(T t: collection) if (--num < 0) return t;
        throw new AssertionError();
    }
}
