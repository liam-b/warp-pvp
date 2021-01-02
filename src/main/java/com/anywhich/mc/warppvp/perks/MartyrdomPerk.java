package com.anywhich.mc.warppvp.perks;

import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MartyrdomPerk extends Perk {
    public static final Perks NAME = Perks.MARTYRDOM;
    public static final int TNT_FUSE = 30;

    public MartyrdomPerk(PerksManager perksManager) {
        super(perksManager, NAME);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (perksManager.hasPlayerSelectedPerk(player, NAME)) {
            TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);
            tnt.setFuseTicks(TNT_FUSE);
            tnt.setSource(player);

            sendPerkActionbar(player, "Dropped live TNT");
        }
    }
}
