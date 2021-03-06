package com.anywhich.mc.warppvp.abilities;

import com.anywhich.mc.warppvp.Lobby;
import com.anywhich.mc.warppvp.WarpPvp;
import com.anywhich.mc.warppvp.WarpPvpConfig;
import com.anywhich.mc.warppvp.perks.*;
import com.anywhich.mc.warppvp.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadoutSelectionMenu implements Listener {
    private final List<MenuItem> items = new ArrayList<>();
    private final Inventory inventory;
    private final Lobby lobby;

    public LoadoutSelectionMenu(Lobby lobby, Map<Player, PlayerData> playerData) {
        this.lobby = lobby;

        initializeItems(playerData);
        inventory = Bukkit.createInventory(null, 27, "Loadout Selection");
        items.forEach(item -> item.addToInventory(inventory));

        WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
    }

    private void initializeItems(Map<Player, PlayerData> playerData) {
        items.add(AbilityMenuItems.getMenuItemForAbility(1, Abilities.BLAST, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(3, Abilities.CHARGE, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(5, Abilities.HOP, playerData));
        items.add(AbilityMenuItems.getMenuItemForAbility(7, Abilities.SLAM, playerData));

        items.add(PerkMenuItems.getMenuItemForPerk(18, Perks.MARTYRDOM, playerData));
        items.add(PerkMenuItems.getMenuItemForPerk(20, Perks.VAMPIRE, playerData));
        items.add(PerkMenuItems.getMenuItemForPerk(22, Perks.HOT_STREAK, playerData));
        items.add(PerkMenuItems.getMenuItemForPerk(24, Perks.UTILITY_EXPERT, playerData));
        items.add(PerkMenuItems.getMenuItemForPerk(26, Perks.WARP_SPECIALIST, playerData));
    }

    public void openInventory(HumanEntity entity) {
        if (lobby.isGamePlying()) entity.sendMessage("Cannot change loadout during the game!");
        else entity.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == inventory) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            Player player = (Player) event.getWhoClicked();
            items.forEach(menuItem -> {
                if (menuItem.getSlot() == event.getRawSlot()) menuItem.getOnClick().accept(player);
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
                addAttributeToLore(lore, "Height", round(HopAbility.VERTICAL_VELOCITY * HopAbility.VERTICAL_VELOCITY / 2 * 10.98));
                break;
            case CHARGE:
                material = Material.DIAMOND_SWORD;

                lore.add("Plow through enemies with");
                lore.add("a short range charge!");
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
                lore.add("with a medium range body-slam!");
                lore.add("");
                addAttributeToLore(lore, "Cooldown", SlamAbility.COOLDOWN / 20 + "s");
                addAttributeToLore(lore, "Damage", round(SlamAbility.DAMAGE / 2) + "♡");
                addAttributeToLore(lore, "Warp", Math.round(SlamAbility.WARP * 100) + "%");
                addAttributeToLore(lore, "Radius", round(SlamAbility.RANGE));
                break;
        }

        return new MenuItem(slot, material, Abilities.COLOR + abilityName.toTitle(), lore, player -> {
            WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
            int stat = plugin.getConfig().getInt("stats.abilities." + abilityName.toString());
            plugin.getConfig().set("stats.abilities." + abilityName.toString(), stat + 1);
            plugin.saveConfig();

            playerData.putIfAbsent(player, new PlayerData());
            playerData.get(player).selectedAbility = abilityName;
            player.sendMessage(Abilities.COLOR + abilityName.toCapital() + ChatColor.WHITE + " ability selected");
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 0.8f, 1);
        });
    }

    private static void addAttributeToLore(List<String> lore, String name, Object value) {
        lore.add(ChatColor.ITALIC + name + ": " + ChatColor.RED + ChatColor.ITALIC + value);
    }

    private static double round(double number) {
        DecimalFormat newFormat = new DecimalFormat("0.#");
        return Double.parseDouble(newFormat.format(number));
    }
}

class PerkMenuItems {
    public static MenuItem getMenuItemForPerk(int slot, Perks perkName, Map<Player, PlayerData> playerData) {
        List<String> lore = new ArrayList<>();
        Material material = Material.AIR;

        switch (perkName) {
            case MARTYRDOM:
                material = Material.TNT;
                lore.add("Drop a live TNT when killed.");
                break;
            case VAMPIRE:
                material = Material.MUTTON;
                lore.add("Gain " + Math.round(VampirePerk.EXTRA_HEARTS + 6) / 2.0 + " hearts after a kill instead of 3.");
                break;
            case HOT_STREAK:
                material = Material.LAVA_BUCKET;
                lore.add("Gain sharpness 1 after " + HotStreakPerk.KILLS_PER_LEVEL);
                lore.add("kills in a row.");
                break;
            case UTILITY_EXPERT:
                material = Material.IRON_AXE;
                lore.add("Collect gear items " + Math.round(UtilityExpertPerk.GEAR_INTERVAL_MULTIPLIER * 100) + "% faster");
                lore.add("with " + Math.round((UtilityExpertPerk.GEAR_LIMIT_MULTIPLIER - 1) * 100) + "% larger stacks.");
                break;
            case WARP_SPECIALIST:
                material = Material.END_CRYSTAL;
                lore.add("Warp decays " + Math.round((WarpSpecialistPerk.WARP_DECAY_MULTIPLIER - 1) * 100) + "% faster than usual.");
                break;
        }

        return new MenuItem(slot, material, Perks.COLOR + perkName.toTitle(), lore, player -> {
            WarpPvp plugin = JavaPlugin.getPlugin(WarpPvp.class);
            int stat = plugin.getConfig().getInt("stats.perks." + perkName.toString());
            plugin.getConfig().set("stats.perks." + perkName.toString(), stat + 1);
            plugin.saveConfig();

            playerData.putIfAbsent(player, new PlayerData());
            playerData.get(player).selectedPerk = perkName;
            player.sendMessage(Perks.COLOR + perkName.toCapital() + ChatColor.WHITE + " perk selected");
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 0.8f, 1);
        });
    }
}