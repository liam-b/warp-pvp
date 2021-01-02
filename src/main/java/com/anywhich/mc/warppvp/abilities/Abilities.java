package com.anywhich.mc.warppvp.abilities;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Abilities {
    HOP,
    CHARGE,
    SLAM,
    BLAST;

    public static final ChatColor COLOR = ChatColor.GOLD;

    public String toCapital() {
        String text = toString().replace("_", " ").toLowerCase();
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public String toTitle() {
        return Arrays.stream(toString().split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}
