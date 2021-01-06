package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.abilities.LoadoutSelectionMenu;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Lobby implements Listener {
    private final WarpPvpConfig config;
    private final Map<Player, PlayerData> playerData = new HashMap<>();
    private final LoadoutSelectionMenu loadoutSelectionMenu;
    private final World world;

    private Game game = null;

    public Lobby(WarpPvpConfig config) {
        this.config = config;

        Bukkit.createWorld(WorldCreator.name(config.worlds.game)); // HACK: this pre-generates the game world
        world = Bukkit.createWorld(WorldCreator.name(config.worlds.lobby));
        loadoutSelectionMenu = new LoadoutSelectionMenu(this, playerData);

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startGame() {
        if (!isGamePlying() && allPlayersAreReady()) {
            game = new Game(config, new HashMap<>(playerData));
            sendMessageToPlayers("[Starting warp pvp game]");
        } else sendMessageToPlayers("[Not all players are ready to start]");
    }

    public void stopGame() {
        if (isGamePlying()) {
            game.destroy(world);
            game = null;

            sendMessageToPlayers("[Stopped warp pvp game]");
        }
    }

    public boolean isGamePlying() {
        return game != null;
    }

    private boolean allPlayersAreReady() {
        return playerData.entrySet().stream().allMatch(entry -> !entry.getKey().isDead() && (entry.getValue().isEligibleToPlay() || entry.getValue().isSpectating));
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(config.worlds.lobby) || player.getWorld().getName().equals(config.worlds.game)) playerData.putIfAbsent(player, new PlayerData());
        else playerData.remove(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals(config.worlds.lobby)) playerData.put(player, new PlayerData());
        else if (player.getWorld().getName().equals(config.worlds.game)) {
            PlayerManager.resetPlayer(player);
            player.teleport(world.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerData.remove(player);
    }

    private void sendMessageToPlayers(String message) {
        playerData.keySet().forEach(player -> player.sendMessage(message));
    }

    public void openLoadoutSelection(Player player) {
        loadoutSelectionMenu.openInventory(player);
    }

    public World getWorld() {
        return world;
    }
}
