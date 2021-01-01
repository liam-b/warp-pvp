package com.anywhich.mc.warppvp;

import com.anywhich.mc.warppvp.gear.Gear;
import com.anywhich.mc.warppvp.gear.GearManager;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentManager implements Listener {
    private final HashMap<Integer, ItemStack> permanentItems = new HashMap<>();
    private final HashMap<Integer, ItemStack> gearItems = new HashMap<>();
    private final Map<Player, PlayerData> playerData;
//    private final GearManager gearManager;

    public EquipmentManager(Map<Player, PlayerData> playerData, GearManager gearManager) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.playerData = playerData;
//        this.gearManager = gearManager;

        ItemStack sword = new ItemStack(Material.STONE_SWORD);
        ItemMeta swordMeta = sword.getItemMeta();
        swordMeta.setUnbreakable(true);
        sword.setItemMeta(swordMeta);
        permanentItems.put(0, sword);

//        ItemStack bow = new ItemStack(Material.BOW);
//        ItemMeta bowMeta = sword.getItemMeta();
//        bowMeta.setUnbreakable(true);
//        bow.setItemMeta(bowMeta);
//        items.put(1, bow);

        ItemStack boots = new ItemStack(Material.IRON_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION_FALL, 2);
        ItemMeta bootsMeta = boots.getItemMeta();
        bootsMeta.setUnbreakable(true);
        boots.setItemMeta(bootsMeta);
        permanentItems.put(36, boots);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
        leggings.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
        ItemMeta leggingsMeta = leggings.getItemMeta();
        leggingsMeta.setUnbreakable(true);
        leggings.setItemMeta(leggingsMeta);
        permanentItems.put(37, leggings);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
        chestplate.addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
        ItemMeta chestplateMeta = chestplate.getItemMeta();
        chestplateMeta.setUnbreakable(true);
        chestplate.setItemMeta(chestplateMeta);
        permanentItems.put(38, chestplate);

        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        ItemMeta helmetMeta = helmet.getItemMeta();
        helmetMeta.setUnbreakable(true);
//        helmetMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
        helmet.setItemMeta(helmetMeta);
        permanentItems.put(39, helmet);

        for (Gear gear : gearManager.getGear()) {
            gearItems.put(gear.ITEM_SLOT, gear.ITEM.asQuantity(Math.floorDiv(gear.SUPPLY_MAX, 2)));
        }
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (playerData.containsKey(event.getEntity())) {
            ArrayList<ItemStack> itemsToRemove = new ArrayList<>();
            for (ItemStack item : event.getDrops()) {
                for (ItemStack equipmentItem : permanentItems.values()) {
                    if (item.getType() == equipmentItem.getType()) itemsToRemove.add(item);
                }
            }

            event.getDrops().removeAll(itemsToRemove);
            event.setShouldDropExperience(false);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (playerData.containsKey(event.getPlayer())) supplyEquipment(event.getPlayer());
    }

    public void supplyEquipment(Player player) {
        Inventory inventory = player.getInventory();

        HashMap<Integer, ItemStack> allItems = new HashMap<>();
        allItems.putAll(permanentItems);
        allItems.putAll(gearItems);
        for (int slot : allItems.keySet()) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() != allItems.get(slot).getType()) {
                inventory.setItem(slot, allItems.get(slot));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (playerData.containsKey(player)) {
            event.setCancelled(true);
        }
    }
}
