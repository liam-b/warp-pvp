package com.anywhich.mc.commandutil;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import java.util.*;

public class Command {
    private final String name;
    private boolean isRoot;
    private final List<Command> children = new ArrayList<>();
    private List<CommandUsage> usages = new ArrayList<>();

    public Command(String name) {
        this.isRoot = false;
        this.name = name;
    }

    public Command build(PluginCommand pluginCommand) {
        Objects.requireNonNull(pluginCommand);
        pluginCommand.setExecutor(this::onCommand);
        pluginCommand.setTabCompleter(this::onTabComplete);
        isRoot = true;
        return this;
    }

    public Command addSubCommand(String name) {
        Command subCommand = new Command(name);
        children.add(subCommand);
        return subCommand;
    }

    public Command addUsage(Class<?>[] arguments, CommandExecutor executor) {
        usages.add(new CommandUsage(arguments, executor));
        return this;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        boolean firstArgumentIsName = args.length >= 1 && args[0].equals(name) || isRoot;

        if (!isRoot) args = removeFirstElement(args);
        boolean isOverloadedByChild = false;
        if (args.length != 0 && firstArgumentIsName) {
            for (Command child : children) {
                if (child.onCommand(sender, command, alias, args)) isOverloadedByChild = true;
            }
        }

        if (!isOverloadedByChild && firstArgumentIsName) {
            for (CommandUsage usage : usages) {
                if (usage.tryExecute(sender, new ArrayDeque<>(Arrays.asList(args)))) return true;
            }
        }

        return isRoot;
    }

    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        List<String> tabCompletions = new ArrayList<>();
        if (args.length == 1 && name.startsWith(args[0]) && !isRoot) tabCompletions.add(name);

        if (args.length > 1 && args[0].equals(name) || isRoot) {
            if (!isRoot) args = removeFirstElement(args);
            for (Command child : children) {
                tabCompletions.addAll(child.onTabComplete(sender, command, alias, args));
            }

            for (CommandUsage usage : usages) {
                String completions = usage.getCompletions(args);
                if (completions != null) tabCompletions.add(completions);
            }
        }

        return tabCompletions;
    }

    private String[] removeFirstElement(String[] array) {
        if (array.length > 1) return Arrays.copyOfRange(array, 1, array.length);
        else return new String[0];
    }
}
