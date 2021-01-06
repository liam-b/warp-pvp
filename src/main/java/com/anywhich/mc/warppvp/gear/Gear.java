package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
import com.anywhich.mc.warppvp.perks.HotStreakPerk;
import com.anywhich.mc.warppvp.perks.Perks;
import com.anywhich.mc.warppvp.perks.UtilityExpertPerk;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Gear implements Listener {
    public final int SUPPLY_PERIOD;
    public final int SUPPLY_MAX;
    public final int COUNT_MAX;

    public final ItemStack ITEM;
    public final int ITEM_SLOT;

    protected final WarpEffectManager warpEffectManager;
    protected final Map<Player, PlayerData> playerData;
    private final int onTickTaskId;
    private final int onSupplyTaskId;
    private final int onExtraSupplyTaskId;

    public Gear(WarpEffectManager warpEffectManager, Map<Player, PlayerData> playerData, int countMax, ItemStack item, int itemSlot, int supplyMax, int supplyPeriod) {
        SUPPLY_PERIOD = supplyPeriod;
        COUNT_MAX = countMax;
        SUPPLY_MAX = supplyMax;
        ITEM = item;
        ITEM_SLOT = itemSlot;

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
        onSupplyTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onSupply, 0, SUPPLY_PERIOD);
        onExtraSupplyTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onExtraSupply, 0, Math.round(SUPPLY_PERIOD / UtilityExpertPerk.GEAR_INTERVAL_MULTIPLIER));
        this.warpEffectManager = warpEffectManager;
        this.playerData = playerData;
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onTickTaskId);
        Bukkit.getScheduler().cancelTask(onSupplyTaskId);
        Bukkit.getScheduler().cancelTask(onExtraSupplyTaskId);
        HandlerList.unregisterAll(this);
    }

    protected void onTick() {}

    public void onSupply() {
        playerData.keySet().forEach(player -> {
            mergeItemIntoInventory(player, ITEM, ITEM_SLOT, SUPPLY_MAX);
        });
    }

    public void onExtraSupply() {
        playerData.forEach((player, data) -> {
            if (data.selectedPerk == Perks.UTILITY_EXPERT) mergeItemIntoInventory(player, ITEM, ITEM_SLOT, SUPPLY_MAX);
        });
    }

    void mergeItemIntoInventory(Player player, ItemStack newItem, int slot, int maxAmount) {
        if (playerData.get(player).selectedPerk == Perks.UTILITY_EXPERT) maxAmount *= UtilityExpertPerk.GEAR_LIMIT_MULTIPLIER;

        ItemStack existingItem = player.getInventory().getItem(slot);
        int newAmount = newItem.getAmount();
        if (existingItem != null && existingItem.getType() == newItem.getType()) newAmount = Math.min(existingItem.getAmount() + newAmount, maxAmount);
        if (existingItem == null || existingItem.getAmount() < maxAmount) player.getInventory().setItem(slot, newItem.asQuantity(newAmount));
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            Item itemEntity = event.getItem();
            if (itemEntity.getItemStack().getType() == ITEM.getType() && playerData.containsKey(player)) {
                mergeItemIntoInventory(player, itemEntity.getItemStack(), ITEM_SLOT, COUNT_MAX);
                itemEntity.remove();
                event.setCancelled(true);
            }
        }
    }
}
