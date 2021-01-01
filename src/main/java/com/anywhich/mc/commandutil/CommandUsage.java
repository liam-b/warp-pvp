package com.anywhich.mc.commandutil;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CommandUsage {
    private final Class<?>[] arguments;
    private final CommandExecutor executor;

    public CommandUsage(Class<?>[] arguments, CommandExecutor executor) {
        this.arguments = arguments;
        this.executor = executor;
    }

    public boolean tryExecute(CommandSender sender, Queue<String> rawArguments) {
        List<Object> extractedArguments = new ArrayList<>();
        for (Class<?> argument : arguments) {
            if (rawArguments.isEmpty()) break;
            else {
                for (CommandArgumentTypes argumentType : CommandArgumentTypes.values()) {
                    if (argument == argumentType.type && rawArguments.size() > argumentType.numValues - 1)
                        extractedArguments.add(argumentType.parser.parse(sender, rawArguments));
                }
            }
        }

        if (extractedArguments.size() == arguments.length) {
            executor.execute(sender, extractedArguments);
            return true;
        }
        return false;
    }

    public String getCompletions(String[] rawArguments) {
        List<String> argumentCompletions = new ArrayList<>();
        for (int i = 0; i < arguments.length; i++) {
            if (i + 1 >= rawArguments.length) {
                for (CommandArgumentTypes argumentType : CommandArgumentTypes.values()) {
                    if (arguments[i] == argumentType.type) argumentCompletions.add(argumentType.completion);
                }
            }
        }

        if (!argumentCompletions.isEmpty()) return String.join(" ", argumentCompletions);
        return null;
    }
}
