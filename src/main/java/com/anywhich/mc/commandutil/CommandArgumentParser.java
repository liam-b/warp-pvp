package com.anywhich.mc.commandutil;

import org.bukkit.command.CommandSender;

import java.util.Queue;

@FunctionalInterface
public interface CommandArgumentParser {
    Object parse(CommandSender sender, Queue<String> args);
}
