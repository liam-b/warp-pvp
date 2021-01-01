package com.anywhich.mc.commandutil;

import org.bukkit.command.CommandSender;

import java.util.List;

@FunctionalInterface
public interface CommandExecutor {
    void execute(CommandSender sender, List<Object> args);
}
