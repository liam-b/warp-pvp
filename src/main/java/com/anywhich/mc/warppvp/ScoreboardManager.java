package com.anywhich.mc.warppvp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Set;

public class ScoreboardManager implements Listener {
    private final Scoreboard scoreboard;
    public final Team defaultTeam;
    public final Objective kills;
    public final Objective deaths;

    private final Set<Player> players;

    public ScoreboardManager(Set<Player> players) {
        this.players = players;

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        players.forEach(player -> player.setScoreboard(scoreboard));

        defaultTeam = scoreboard.registerNewTeam("default");
        defaultTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        players.forEach(player -> defaultTeam.addEntry(player.getName()));

        kills = scoreboard.registerNewObjective("kills", "dummy", "Kills");
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
        players.forEach(player -> kills.getScore(player.getName()).setScore(0));

        deaths = scoreboard.registerNewObjective("deaths", "dummy", "Deaths");
        deaths.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        players.forEach(player -> deaths.getScore(player.getName()).setScore(0));

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        incrementScore(deaths, event.getEntity());

        Player killer = event.getEntity().getKiller();
        if (killer != null && players.contains(event.getEntity()) && players.contains(killer) && event.getEntity() != killer) {
            killer.sendTitle("", "[⚔] " + event.getEntity().getDisplayName(), 4, 42, 4);
            incrementScore(kills, killer);
        }
    }

    private void incrementScore(Objective objective, Player player) {
        Score score = objective.getScore(player.getName());
        score.setScore(score.getScore() + 1);
    }
}