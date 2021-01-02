package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class PowerRingsManager implements Listener {
    public static final int RING_GENERATE_INTERVAL = 120 * 20;
    public static final int RING_GENERATE_VARIANCE = 15 * 20;

    private final WarpPvp plugin;
    private final Map<Player, PlayerData> playerData;
    private final List<Location> ringLocations;
    private final List<PowerRing> rings = new ArrayList<>();
    private final Random random = new Random();
    private final int onUpdateTaskId;
    private int onGenerateTaskId;


    public PowerRingsManager(Map<Player, PlayerData> playerData, World world, WarpPvpConfig config) {
        this.playerData = playerData;

        plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onUpdate, 0, PowerRing.UPDATE_INTERVAL);

        ringLocations = config.ringSpawns.stream().map(vector -> vector.toLocation(world)).collect(Collectors.toList());
        scheduleGenerateTask();
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onUpdateTaskId);
        Bukkit.getScheduler().cancelTask(onGenerateTaskId);
        HandlerList.unregisterAll(this);
    }

    private void scheduleGenerateTask() {
        onGenerateTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::onGenerate, Math.round(RING_GENERATE_INTERVAL + (random.nextDouble() - 0.5) * RING_GENERATE_VARIANCE));
    }

    private void onGenerate() {
        rings.add(new PowerRing(random(ringLocations), playerData));
    }

    private void onUpdate() {
        List<PowerRing> toRemove = new ArrayList<>();
        rings.forEach(ring -> {
            ring.onUpdate();
            if (ring.getAge() > PowerRing.LIFETIME) toRemove.add(ring);
        });

        if (!toRemove.isEmpty()) scheduleGenerateTask();
        rings.removeAll(toRemove);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) event.setCancelled(true);
    }

    public static <T> T random(Collection<T> collection) {
        int num = (int) (Math.random() * collection.size());
        for(T t: collection) if (--num < 0) return t;
        throw new AssertionError();
    }
}

class PowerRing {
    public static final int UPDATE_INTERVAL = 10;
    public static final int LIFETIME = 35 * (20 / UPDATE_INTERVAL);
    public static final int PREPARE_TIME = 15 * (20 / UPDATE_INTERVAL);

    private static final PotionEffect POTION_EFFECT = new PotionEffect(PotionEffectType.SPEED, 100, 0, false, true, true);
    private static final int ABSORPTION_HEARTS_TOTAL = 10;

    private static final double RADIUS = 3.5;
    private static final int NUM_PARTICLES = 60;
    private static final Particle PARTICLE_TYPE = Particle.FLAME;
    private static final double PARTICLE_Y_OFFSET = 0.2;

    private static final double PREPARE_HEIGHT = 64;
    private static final int PREPARE_EXPLOSION_INTERVAL = 2;
    private static final double PREPARE_EXPLOSION_SIZE = 0.5;
    private static final int PREPARE_EXPLOSION_NUM_PARTICLES = 32;
    private static final double PREPARE_EXPLOSION_PARTICLE_SPEED = 0.4;


    private final Random random = new Random();

    private int age = -PREPARE_TIME;
    private final Location center;
    private final Map<Player, PlayerData> playerData;

    public PowerRing(Location center, Map<Player, PlayerData> playerData) {
        this.center = center;
        this.playerData = playerData;
    }

    public void onUpdate() {
        if (age <= 0) {
            if (age % PREPARE_EXPLOSION_INTERVAL == 0) {
                double progress = (-age / (double) PREPARE_TIME);
                double height = progress * PREPARE_HEIGHT;
                ParticleHelper.animatedParticleExplosion(center.clone().add(0, height, 0), PREPARE_EXPLOSION_SIZE, PREPARE_EXPLOSION_NUM_PARTICLES, PREPARE_EXPLOSION_PARTICLE_SPEED);
                playerData.keySet().forEach(player -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1 - (float)progress, 1f);
                });
            }
        }

        if (age == 0) center.getWorld().strikeLightning(center);

        if (age >= 0) {
            drawParticles();
            center.getNearbyPlayers(RADIUS).forEach(player -> {
                if (playerData.containsKey(player)) {
                    player.addPotionEffect(POTION_EFFECT);
                    if (age % (LIFETIME / ABSORPTION_HEARTS_TOTAL) == 0) {
                        player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount() + 1, ABSORPTION_HEARTS_TOTAL));
                    }
                }
            });
        }

        age++;
    }

    private void drawParticles() {
        double angleOffset = random.nextDouble();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            double angle = i / (double)NUM_PARTICLES * Math.PI * 2;
            Location position = center.clone().add(new Vector(RADIUS, 0, 0).rotateAroundY(angle + angleOffset));
            position.getWorld().spawnParticle(PARTICLE_TYPE, position, 1, 0, PARTICLE_Y_OFFSET, 0, 0);
        }
    }

    public int getAge() {
        return age;
    }
}