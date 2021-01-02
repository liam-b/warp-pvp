package com.anywhich.mc.warppvp;

import com.anywhich.mc.configutil.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WarpPvpConfig extends Config { // TODO: maybe this should be singleton?
    public List<Vector> playerSpawns = new ArrayList<>();
    public List<Vector> ringSpawns = new ArrayList<>();

    public WarpPvpConfig(JavaPlugin plugin) {
        super(plugin);
    }
}
