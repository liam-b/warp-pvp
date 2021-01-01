package com.anywhich.mc.warppvp.gear;

import com.anywhich.mc.warppvp.WarpEffectManager;
import com.anywhich.mc.warppvp.WarpPvp;
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
import java.util.Set;

public abstract class Gear implements Listener {
    public final int SUPPLY_PERIOD;
    public final int SUPPLY_MAX;
    public final int COUNT_MAX;

    public final ItemStack ITEM;
    public final int ITEM_SLOT;

    protected final WarpEffectManager warpEffectManager;
    protected final Set<Player> players;
    private final int onTickTaskId;
    private final int onSupplyTaskId;

    public Gear(WarpEffectManager warpEffectManager, Set<Player> players, int countMax, ItemStack item, int itemSlot, int supplyMax, int supplyPeriod) {
        SUPPLY_PERIOD = supplyPeriod;
        COUNT_MAX = countMax;
        SUPPLY_MAX = supplyMax;
        ITEM = item;
        ITEM_SLOT = itemSlot;

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        onTickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
        onSupplyTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onSupply, 0, SUPPLY_PERIOD);
        this.warpEffectManager = warpEffectManager;
        this.players = players;
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(onTickTaskId);
        Bukkit.getScheduler().cancelTask(onSupplyTaskId);
        HandlerList.unregisterAll(this);
    }

    protected void onTick() {}

    public void onSupply() {
        for (Player player : players) {
            mergeItemIntoInventory(player, ITEM, ITEM_SLOT, SUPPLY_MAX);
        }
    }

    void mergeItemIntoInventory(Player player, ItemStack newItem, int slot, int maxAmount) {
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
            if (itemEntity.getItemStack().getType() == ITEM.getType() && players.contains(player)) {
                mergeItemIntoInventory(player, itemEntity.getItemStack(), ITEM_SLOT, COUNT_MAX);
                itemEntity.remove();
                event.setCancelled(true);
            }
        }
    }
}
