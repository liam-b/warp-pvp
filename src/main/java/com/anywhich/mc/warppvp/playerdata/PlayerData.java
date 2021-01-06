package com.anywhich.mc.warppvp.playerdata;

import com.anywhich.mc.warppvp.abilities.Abilities;
import com.anywhich.mc.warppvp.perks.Perks;
import org.bukkit.entity.Player;

public class PlayerData {
    public Abilities selectedAbility;
    public Perks selectedPerk;
    public boolean isSpectating = false;

    public boolean isEligibleToPlay() {
        return selectedAbility != null && selectedPerk != null && selectedPerk != null;
    }
}
