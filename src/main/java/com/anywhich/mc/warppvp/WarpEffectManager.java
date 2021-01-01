package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.playerdata.PlayerData;
import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutWorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

public class WarpEffectManager implements Listener {
    private static final double TINT_MAX_MULTIPLIER = 10;
    private static final double TINT_MIN_MULTIPLIER = 0.5;

    private static final double DEFAULT_WALK_SPEED = 0.2;
    private static final double MIN_WALK_SPEED = 0.025;

    private static final double JUMP_RESTRICTION_THRESHOLD = 0.18;
    private static final double JUMP_RESTRICTION_MIN_VELOCITY = -0.3;
    private static final double JUMP_RESTRICTION_MAX_VELOCITY = 0.4;

//    private static final double POTION_THRESHOLD = 0.15;
//    private static final int POTION_MIN_AMPLIFIER = 255;
//    private static final int POTION_MAX_AMPLIFIER = 251;

//    private static final double LEVITATION_INITIAL_THRESHOLD = 0.15;
//    private static final int LEVITATION_INITIAL_LEVEL = 255;
//    private static final double LEVITATION_SIGNIFICANT_THRESHOLD = 0.35;
//    private static final int LEVITATION_SIGNIFICANT_LEVEL = 255;
//    private static final int LEVITATION_MAXIMUM_LEVEL = 251;

    private static final double WARP_DECAY_PER_TICK = 0.0045;

    private final WorldBorder worldBorder;
    private final Map<Player, Double> warpLevels = new HashMap<>();
    private final Map<Player, BossBar> warpBars = new HashMap<>();
    private final Set<Player> hasJumped = new HashSet<>();
    private final int onTickTaskId;

    public WarpEffectManager(Map<Player, PlayerData> playerData, World world) {
        worldBorder = world.getWorldBorder();
        playerData.keySet().forEach(player -> {
            warpLevels.put(player, 0.0);

            BossBar bar = Bukkit.createBossBar("Warp", BarColor.RED, BarStyle.SOLID);
            bar.addPlayer(player);
            warpBars.put(player, bar);
        });

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onTickTaskId);
        warpBars.values().forEach(BossBar::removeAll);
        HandlerList.unregisterAll(this);
    }

    public void increasePlayerWarpLevel(Player player, double amount) {
        warpLevels.put(player, Math.min(warpLevels.getOrDefault(player, 0.0) + amount, 1));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1, 2);
    }

    private void onTick() {
        warpLevels.forEach((player, warpLevel) -> {
//            player.sendActionBar(String.valueOf(Math.round(warpLevel * 100.0) / 100.0));

            setPlayerRedTint(player, warpLevel);
            warpBars.get(player).setVisible(warpLevel > 0);
            warpBars.get(player).setProgress(warpLevel);

            float walkSpeed = (float) map(1 - warpLevel, 0, 1, MIN_WALK_SPEED, DEFAULT_WALK_SPEED);
            player.setWalkSpeed(walkSpeed);

//            int levitationLevel = 0;
//            if (warpLevel > LEVITATION_SIGNIFICANT_THRESHOLD) {
//                levitationLevel = (int)Math.round(map(1 - warpLevel, 0, 1 - LEVITATION_SIGNIFICANT_THRESHOLD, LEVITATION_MAXIMUM_LEVEL, LEVITATION_SIGNIFICANT_LEVEL));
//            } else if (warpLevel > LEVITATION_INITIAL_THRESHOLD) {
//                levitationLevel = LEVITATION_INITIAL_LEVEL;
//            }

//            int effectAmplifier = 0;
//            if (warpLevel > POTION_THRESHOLD) {
//                effectAmplifier = (int)Math.round(map(1 - warpLevel, 0, 1 - POTION_THRESHOLD, POTION_MAX_AMPLIFIER, POTION_MIN_AMPLIFIER));
//            }
//
//            PotionEffect oldPotionEffect = player.getPotionEffect(PotionEffectType.JUMP);
//            if (oldPotionEffect == null || oldPotionEffect.getAmplifier() != effectAmplifier) {
//                player.removePotionEffect(PotionEffectType.JUMP);
//                if (effectAmplifier > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, effectAmplifier, false, false, false));
//            }
//            player.sendActionBar(String.valueOf(effectAmplifier));

            warpLevels.put(player, Math.max(0, warpLevel - WARP_DECAY_PER_TICK));
        });
    }

    @EventHandler
    private void onPlayerJump(PlayerJumpEvent event) { // maybe use hunger?
        Player player = event.getPlayer();
        if (warpLevels.containsKey(player)) {
            double warpLevel = warpLevels.get(player);
            if (warpLevel != 0 && warpLevel >= JUMP_RESTRICTION_THRESHOLD) hasJumped.add(player);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (hasJumped.contains(player)) {
            double jumpVelocity = map(warpLevels.get(player), JUMP_RESTRICTION_THRESHOLD, 1, JUMP_RESTRICTION_MIN_VELOCITY, JUMP_RESTRICTION_MAX_VELOCITY);
            player.setVelocity(player.getVelocity().setY(-jumpVelocity));
            hasJumped.remove(player);
        }
    }

//    @EventHandler
//    private void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Player player = (Player) event.getEntity();
//            player.sendMessage(event.getAcceleration().toString());
//            if (warpLevels.get(player) > 0) {
//                if (Math.abs(player.getVelocity().getY()) > 0.8) {
//                    player.sendMessage("velocity reset");
//                    player.setVelocity(player.getVelocity().setY(0));
//                }
//            }
//        }
//    }

    private void setPlayerRedTint(Player player, double tint) {
        double normalisedTint = tint * tint;
//        double normalisedTint = 1 - Math.sqrt(1 - tint * tint);
        sendWorldBorderPacket(player, (int) map(normalisedTint, 0, 1, worldBorder.getSize() * TINT_MIN_MULTIPLIER, worldBorder.getSize() * TINT_MAX_MULTIPLIER));
    }

    private void sendWorldBorderPacket(Player player, int warningBlocks) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.server.v1_16_R3.WorldBorder playerWorldBorder = nmsPlayer.world.getWorldBorder();
        PacketPlayOutWorldBorder worldBorder = new PacketPlayOutWorldBorder(playerWorldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS);
        try {
            Field field = worldBorder.getClass().getDeclaredField("i");
            field.setAccessible(true);
            field.setInt(worldBorder, warningBlocks);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nmsPlayer.playerConnection.sendPacket(worldBorder);
    }

    private double map(double number, double fromMin, double fromMax, double toMin, double toMax) {
        double value = (number - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
        return Math.max(toMin, Math.min(value, toMax));
    }
}
