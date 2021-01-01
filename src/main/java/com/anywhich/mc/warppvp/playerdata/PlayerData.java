package com.anywhich.mc.warppvp.playerdata;

import com.anywhich.mc.warppvp.abilities.Abilities;
import com.anywhich.mc.warppvp.perks.Perks;

public class PlayerData {
    public Abilities selectedAbility;
    public Perks selectedPerk;

    public boolean isEligibleToPlay() {
        return selectedAbility != null;
    }
}
