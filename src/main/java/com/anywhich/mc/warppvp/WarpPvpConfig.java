package com.anywhich.mc.warppvp;

import com.anywhich.mc.configutil.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WarpPvpConfig extends Config { // TODO: maybe this should be singleton?
    public WorldsConfig worlds = new WorldsConfig();

    public List<Vector> playerSpawns = new ArrayList<>();
    public List<Vector> ringSpawns = new ArrayList<>();

    public boolean doPowerRings = false;

    public WarpPvpConfig(JavaPlugin plugin) {
        super(plugin);
    }
}

class WorldsConfig extends Config {
    public String lobby = "world_warp_lobby";
    public String game = "world_warp_game";
}