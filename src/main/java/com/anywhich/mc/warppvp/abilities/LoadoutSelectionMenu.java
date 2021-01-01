package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.WarpPvp;
import com.anywhich.mc.warppvp.perks.Perks;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadoutSelectionMenu implements Listener {
    private final List<MenuItem> items = new ArrayList<>();
    private final Inventory inventory;

    public LoadoutSelectionMenu(Map<Player, PlayerData> playerData) {
        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        initializeItems(playerData);
        inventory = Bukkit.createInventory(null, 18, "Loadout Selection");
        items.forEach(item -> item.addToInventory(inventory));
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    private void initializeItems(Map<Player, PlayerData> playerData) {
        items.add(AbilityMenuItems.getMenuItemForAbility(1, Abilities.BLAST, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(3, Abilities.CHARGE, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(5, Abilities.HOP, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(7, Abilities.SLAM, playerData));

        items.add(PerkMenuItems.getMenuItemForPerk(11, Perks.MARTYRDOM, playerData));
    }

    public void openInventory(HumanEntity entity) {
        entity.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == inventory) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            items.forEach(menuItem -> {
                if (menuItem.getSlot() == event.getRawSlot()) {
                    menuItem.getOnClick().accept(player);
                    player.closeInventory();
                }
            });
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryDragEvent event) {
        if (event.getInventory() == inventory) {
            event.setCancelled(true);
        }
    }
}

class MenuItem {
    protected final int slot;
    protected final Material material;
    protected final String name;
    protected final List<String> lore;
    protected final Consumer<Player> onClick;

    public MenuItem(int slot, Material material, String name, List<String> lore, Consumer<Player> onClick) {
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.onClick = onClick;
    }

    public void addToInventory(Inventory inventory) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RESET + name);
        meta.setLore(lore.stream().map(line -> ChatColor.RESET + ChatColor.GRAY.toString() + line).collect(Collectors.toList()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        inventory.setItem(slot, item);
    }

    public int getSlot() {
        return slot;
    }

    public Consumer<Player> getOnClick() {
        return onClick;
    }
}

class AbilityMenuItems {
    public static MenuItem getMenuItemForAbility(int slot, Abilities abilityName, Map<Player, PlayerData> playerData) {
        List<String> lore = new ArrayList<>();
        Material material = Material.AIR;

        switch (abilityName) {
            case HOP:
                material = Material.FEATHER;

                lore.add("Jump straight out of any");
                lore.add("sticky situation and fly away!");
                lore.add("");
                addAttributeToLore(lore, "Cooldown", HopAbility.COOLDOWN / 20 + "s");
                addAttributeToLore(lore, "Damage", round(HopAbility.DAMAGE / 2) + "♡");
                addAttributeToLore(lore, "Warp", Math.round(HopAbility.WARP * 100) + "%");
                addAttributeToLore(lore, "Height", round(HopAbility.VERTICAL_VELOCITY * 7.14));
                break;
            case CHARGE:
                material = Material.DIAMOND_SWORD;

                lore.add("Leave a path of death");
                lore.add("and destruction!");
                lore.add("");
                addAttributeToLore(lore, "Cooldown", ChargeAbility.COOLDOWN / 20 + "s");
                addAttributeToLore(lore, "Damage", round(ChargeAbility.DAMAGE / 2) + "♡");
                addAttributeToLore(lore, "Warp", Math.round(ChargeAbility.WARP * 100) + "%");
                addAttributeToLore(lore, "Distance", round(ChargeAbility.SELECTION_RANGE));
                break;
            case BLAST:
                material = Material.FIRE_CHARGE;

                lore.add("Throw your enemies away with");
                lore.add("with a powerful radial blast!");
                lore.add("");
                addAttributeToLore(lore, "Cooldown", BlastAbility.COOLDOWN / 20 + "s");
                addAttributeToLore(lore, "Damage", round(BlastAbility.DAMAGE / 2) + "♡");
                addAttributeToLore(lore, "Warp", Math.round(BlastAbility.WARP * 100) + "%");
                addAttributeToLore(lore, "Radius", round(BlastAbility.RANGE));
                break;
            case SLAM:
                material = Material.ANVIL;

                lore.add("Catch your enemies off guard");
                lore.add("with a short range body-slam!");
                lore.add("");
                addAttributeToLore(lore, "Cooldown", SlamAbility.COOLDOWN / 20 + "s");
                addAttributeToLore(lore, "Damage", round(SlamAbility.DAMAGE / 2) + "♡");
                addAttributeToLore(lore, "Warp", Math.round(SlamAbility.WARP * 100) + "%");
                addAttributeToLore(lore, "Radius", round(SlamAbility.RANGE));
                break;
        }

        return new MenuItem(slot, material, ChatColor.GOLD + capitalize(abilityName.toString()), lore, player -> {
            playerData.putIfAbsent(player, new PlayerData());
            playerData.get(player).selectedAbility = abilityName;
            player.sendMessage("Selected " + abilityName.toString().toLowerCase() + " ability");
        });
    }

    private static void addAttributeToLore(List<String> lore, String name, Object value) {
        lore.add(ChatColor.ITALIC + name + ": " + ChatColor.RED + ChatColor.ITALIC + value);
    }

    private static double round(double number) {
        DecimalFormat newFormat = new DecimalFormat("0.#");
        return Double.parseDouble(newFormat.format(number));
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}

class PerkMenuItems {
    public static MenuItem getMenuItemForPerk(int slot, Perks perkName, Map<Player, PlayerData> playerData) {
        List<String> lore = new ArrayList<>();
        Material material = Material.AIR;

        switch (perkName) {
            case MARTYRDOM:
                material = Material.TNT;
                lore.add("Drop a live TNT when killed");
                break;
        }

        return new MenuItem(slot, material, ChatColor.GOLD + capitalize(perkName.toString()), lore, player -> {
            playerData.putIfAbsent(player, new PlayerData());
            playerData.get(player).selectedPerk = perkName;
            player.sendMessage("Selected " + perkName.toString().toLowerCase() + " perk");
        });
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}