package com.anywhich.mc.commandutil;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Queue;

public enum CommandArgumentTypes {
    INTEGER(Integer.class, 1, "<integer>", (CommandSender sender, Queue<String> args) -> Integer.parseInt(args.poll())),
    BOOLEAN(Boolean.class, 1, "<boolean>", (CommandSender sender, Queue<String> args) -> Boolean.parseBoolean(args.poll())),
    DECIMAL(Float.class, 1, "<decimal>", (CommandSender sender, Queue<String> args) -> Float.parseFloat(args.poll())),
    POSITION(Vector.class, 3, "<position.x> <position.y> <position.z>", (CommandSender sender, Queue<String> args) -> interpretVector(args.poll(), args.poll(), args.poll(), sender));

    // TODO: system for argument type with multiple raw arguments

    public final Class<?> type;
    public final int numValues;
    public final String completion;
    public final CommandArgumentParser parser;

    CommandArgumentTypes(Class<?> type, int numValues, String completion, CommandArgumentParser parser) {
        this.type = type;
        this.numValues = numValues;
        this.completion = completion;
        this.parser = parser;
    }

    private static Vector interpretVector(String first, String second, String third, CommandSender sender) {
        if (first.equals("~") && second.equals("~") && third.equals("~")) {
            Location location = ((Player) sender).getLocation();
            return new Vector(location.getBlockX() + 0.5f, location.getBlockY(), location.getBlockZ() + 0.5f);
        }
        return new Vector(Float.parseFloat(first), Float.parseFloat(second), Float.parseFloat(third));
    }
}
