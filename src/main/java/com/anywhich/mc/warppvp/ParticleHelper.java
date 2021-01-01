package com.anywhich.mc.warppvp;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleHelper {
    public static void particleExplosion(Location center, int samples, double speed) {
        for (int i = 0; i < samples; i++) {
            center.getWorld().spawnParticle(Particle.CRIT, center, 1, 0, 0, 0, speed);
        }
    }

    public static void particleSphere(Location center, double size, int samples, Color color) {
        Vector[] points = pointsOnSurfaceOfSphere(center.toVector(), size, samples);

        for (Vector point : points) {
//            Vector direction = point.clone().subtract(center.toVector()).normalize();
            center.getWorld().spawnParticle(Particle.REDSTONE, point.toLocation(center.getWorld()), 1, new Particle.DustOptions(color, 0.8f));
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
}
