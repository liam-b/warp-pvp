package com.anywhich.mc.warppvp;

import com.anywhich.mc.configutil.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WarpPvpConfig extends Config { // TODO: maybe this should be singleton?
    public List<Vector> spawns = new ArrayList<>();

    public WarpPvpConfig(JavaPlugin plugin) {
        super(plugin);
    }
}
