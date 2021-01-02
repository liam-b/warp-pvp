package com.anywhich.mc.warppvp.perks;

import com.anywhich.mc.warppvp.EquipmentManager;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotStreakPerk extends Perk {
    public static final Perks NAME = Perks.HOT_STREAK;
    public static final int KILLS_PER_LEVEL = 2;

    private final Map<Player, PlayerData> playerData;
    private final Map<Player, Integer> killStreaks = new HashMap<>();

    public HotStreakPerk(PerksManager perksManager, Map<Player, PlayerData> playerData) {
        super(perksManager, NAME);
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        killStreaks.remove(killed);

        Player killer = event.getEntity().getKiller();
        if (killer != null && playerData.containsKey(killed) && killed != killer && perksManager.hasPlayerSelectedPerk(killer, NAME)) {
            int killStreak = killStreaks.getOrDefault(killer, 0) + 1;
            killStreaks.put(killer, killStreak);

            int level = killStreak / KILLS_PER_LEVEL;
            ItemStack swordItem = killer.getInventory().getItem(0);
            if (swordItem != null) {
                ItemMeta swordMeta = swordItem.getItemMeta();
                swordMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
                switch (level) {
                    case 0:
                        if (swordMeta.hasEnchant(Enchantment.DAMAGE_ALL)) {
                            swordMeta.removeEnchant(Enchantment.DAMAGE_ALL);
                            setItemAttackDamage(swordMeta, 5);
                        }
                        break;
                    case 1:
                        if (swordMeta.getEnchantLevel(Enchantment.DAMAGE_ALL) != 1) {
                            swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
                            setItemAttackDamage(swordMeta, 5);
                            sendPerkActionbar(killer, "Added level of sharpness");
                        }
                        break;
                    case 2:
                        if (swordMeta.getEnchantLevel(Enchantment.DAMAGE_ALL) != 2) {
                            swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, false);
                            setItemAttackDamage(swordMeta, 4);
                            sendPerkActionbar(killer, "Added level of sharpness");
                        }
                        break;
                }

                swordItem.setItemMeta(swordMeta);
            }
        }
    }

    private static void setItemAttackDamage(ItemMeta meta, double amount) {
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
    }
}
