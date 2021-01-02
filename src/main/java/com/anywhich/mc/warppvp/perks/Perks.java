package com.anywhich.mc.warppvp.perks;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Perks {
    MARTYRDOM,
    VAMPIRE,
    WARP_SPECIALIST,
    UTILITY_EXPERT,
    HOT_STREAK;

    public static final ChatColor COLOR = ChatColor.AQUA;

    public String toCapital() {
        String text = toString().replace("_", " ").toLowerCase();
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public String toTitle() {
        return Arrays.stream(toString().split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}
