package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.gear.Gear;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.Map;

public class PerksManager {
    private final Perk[] perks;
    private final Map<Player, PlayerData> playerData;

    public PerksManager(Map<Player, PlayerData> playerData) {
        this.playerData = playerData;

        perks = new Perk[] {
                new MartyrdomPerk(this),
                new VampirePerk(this, playerData),
                new HotStreakPerk(this, playerData),
        };
    }

    public void destroy() {
        for (Perk perk : perks) {
            perk.destroy();
        }
    }

    public boolean hasPlayerSelectedPerk(Player player, Perks perkName) {
        return playerData.containsKey(player) && playerData.get(player).selectedPerk == perkName;
    }
}
