package com.anywhich.mc.warppvp;

import com.anywhich.mc.commandutil.Command;
import com.anywhich.mc.warppvp.abilities.LoadoutSelectionMenu;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WarpPvp extends JavaPlugin implements Listener {
    public final WarpPvpConfig config = new WarpPvpConfig(this);

    private final Map<Player, PlayerData> playerData = new HashMap<>();
    private LoadoutSelectionMenu loadoutSelectionMenu;
    private Game game;

    @Override
    public void onEnable() {
        config.saveDefaults();

        loadoutSelectionMenu = new LoadoutSelectionMenu(playerData);

        Command mainCommand = new Command("warp");

        mainCommand.addSubCommand("start").addUsage(new Class<?>[]{}, (CommandSender sender, List<Object> args) -> {
            Bukkit.broadcastMessage(ChatColor.GOLD + "[Starting warp pvp game]");
            if (game != null) game.destroy();
            game = new Game(config, new HashMap<>(playerData), getServer().getWorld("world"));
        });

        mainCommand.addSubCommand("stop").addUsage(new Class<?>[]{}, (CommandSender sender, List<Object> args) -> {
            if (game != null) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "[Stopped warp pvp game]");
                game.destroy();
                game = null;
            } else sender.sendMessage("No game to stop");
        });

        Command spawnsCommand = mainCommand.addSubCommand("spawns");

        spawnsCommand.addSubCommand("add").addUsage(new Class<?>[0], (sender, args) -> {
            if (sender instanceof Player) {
                Vector position = ((Player) sender).getLocation().toVector();
                config.spawns.add(position);
                config.save();

                sender.sendMessage("Added spawn location " + position.toBlockVector());
            }
        });

        spawnsCommand.addSubCommand("clear").addUsage(new Class<?>[0], (sender, args) -> {
            config.spawns.clear();
            config.save();

            sender.sendMessage("Cleared all spawn locations");
        });

        mainCommand.addSubCommand("loadout").addUsage(new Class<?>[0], (sender, args) -> {
            if (sender instanceof Player) loadoutSelectionMenu.openInventory((Player) sender);
        });

        mainCommand.addSubCommand("test").addUsage(new Class<?>[0], (sender, args) -> {
            if (sender instanceof Player) ((Player) sender).damage(10);
        });

        mainCommand.build(getCommand("warp"));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        config.save();

        loadoutSelectionMenu.destroy();
        loadoutSelectionMenu = null;
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler
    public void onPlayerInteract(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerData.remove(player);
    }
}