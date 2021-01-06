package com.anywhich.mc.warppvp;

import com.anywhich.mc.commandutil.Command;
import com.anywhich.mc.minigameutil.MinigamePlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public final class WarpPvp extends JavaPlugin implements Listener, MinigamePlugin {
    public final WarpPvpConfig config = new WarpPvpConfig(this);
    private Lobby lobby;

    @Override
    public void onEnable() {
        config.saveDefaults();
        lobby = new Lobby(config);

        Command mainCommand = new Command("warp");

        mainCommand.addSubCommand("start").addUsage(new Class<?>[0], (CommandSender sender, List<Object> args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                if (!lobby.isGamePlying()) {
                    lobby.startGame();
                } else sender.sendMessage("Game is already running");
            }
        });

        mainCommand.addSubCommand("stop").addUsage(new Class<?>[0], (CommandSender sender, List<Object> args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                if (lobby.isGamePlying()) {
                    lobby.stopGame();
                } else sender.sendMessage("No game to stop");
            }
        });

        Command settingsCommand = mainCommand.addSubCommand("settings");

        settingsCommand.addSubCommand("doPowerRings").addUsage(new Class<?>[]{Boolean.class}, (CommandSender sender, List<Object> args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                config.doPowerRings = (Boolean) args.get(0);
                config.save();

                sender.sendMessage("Set doPowerRings to " + String.valueOf((Boolean)args.get(0)));
            }
        });

        Command spawnsCommand = settingsCommand.addSubCommand("spawns");

        spawnsCommand.addSubCommand("add").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                if (sender instanceof Player) {
                    Vector position = ((Player) sender).getLocation().toVector();
                    config.playerSpawns.add(position);
                    config.save();

                    sender.sendMessage("Added spawn location " + position.toBlockVector());
                }
            }
        });

        spawnsCommand.addSubCommand("clear").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                config.playerSpawns.clear();
                config.save();

                sender.sendMessage("Cleared all spawn locations");
            }
        });

        Command ringsCommand = settingsCommand.addSubCommand("rings");

        ringsCommand.addSubCommand("add").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                if (sender instanceof Player) {
                    Vector position = ((Player) sender).getLocation().toVector();
                    config.ringSpawns.add(position);
                    config.save();

                    sender.sendMessage("Added ring spawn location " + position.toBlockVector());
                }
            }
        });

        ringsCommand.addSubCommand("clear").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                config.ringSpawns.clear();
                config.save();

                sender.sendMessage("Cleared all ring spawn locations");
            }
        });

        mainCommand.addSubCommand("changelog").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) {
                try {
                    URL url = new URL("https://api.github.com/repos/liam-b/warp-pvp/commits?since=2021-1-02T00:00:00Z");
                    URLConnection request = url.openConnection();
                    request.connect();

                    JsonParser jp = new JsonParser();
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                    root.getAsJsonArray().forEach(element -> {
                        sender.sendMessage(element.getAsJsonObject().get("commit").getAsJsonObject().get("message").getAsString());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mainCommand.addSubCommand("loadout").addUsage(new Class<?>[0], (sender, args) -> {
            if (isSenderAllowedToRunCommand(sender)) lobby.openLoadoutSelection((Player) sender);
        });

        mainCommand.build(getCommand("warp"));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        config.save();
        HandlerList.unregisterAll((Listener) this);
    }

    private boolean isSenderAllowedToRunCommand(CommandSender sender) {
        Player player = (Player) sender;
        return player != null && (player.getWorld().getName().equals(config.worlds.lobby) || player.getWorld().getName().equals(config.worlds.game));
    }

    @Override
    public String getMinigameName() {
        return "warp";
    }

    @Override
    public World getMinigameWorld() {
        return lobby.getWorld();
    }
}