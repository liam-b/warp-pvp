package com.anywhich.mc.warppvp;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParticleHelper {
    public static void particleExplosion(Location center, int samples, double speed) {
        for (int i = 0; i < samples; i++) {
            center.getWorld().spawnParticle(Particle.CRIT, center, 1, 0, 0, 0, speed);
        }
    }

    public static void particleSphere(Location center, double size, int samples, Color color) {
        Vector[] points = pointsOnSurfaceOfSphere(center.toVector(), size, samples);

        for (Vector point : points) {
            center.getWorld().spawnParticle(Particle.REDSTONE, point.toLocation(center.getWorld()), 1, new Particle.DustOptions(color, 0.8f));
        }
    }

    public static void animatedParticleExplosion(Location center, double size, int samples, double speed) {
        Vector[] points = pointsOnSurfaceOfSphere(center.toVector(), size, samples);
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (Vector point : points) {
            Vector direction = point.clone().subtract(center.toVector()).normalize();
            players.forEach(player -> spawnNmsParticle(player, Particles.FIREWORK, point.toLocation(center.getWorld()), direction, 0, speed, true));
        }
    }

    private static Vector[] pointsOnSurfaceOfSphere(Vector center, double size, int samples) {
        Vector[] points = new Vector[samples];
        double phi = Math.PI * (3.0 - Math.sqrt(5.0));

        for (int i = 0; i < samples; i++) {
            double y = 1 - (i / ((double)samples - 1)) * 2;
            double radius = Math.sqrt(1 - y * y);
            double theta = phi * i;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;

            points[i] = new Vector(x * size, y * size, z * size).add(center);
        }

        return points;
    }

    private static void spawnNmsParticle(Player player, ParticleType particle, Location position, Vector offset, int count, double speed, boolean showWhenFar) {
        PacketPlayOutWorldParticles particlePacket = new PacketPlayOutWorldParticles(particle, showWhenFar, position.getX(), position.getY(), position.getZ(), (float)offset.getX(), (float)offset.getY(), (float)offset.getZ(), (float)speed, count);
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection.sendPacket(particlePacket);
    }
}
