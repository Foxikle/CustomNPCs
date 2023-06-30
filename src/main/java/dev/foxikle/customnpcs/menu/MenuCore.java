package dev.foxikle.customnpcs.menu;

import dev.foxikle.customnpcs.Action;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MenuCore {

    private NPC npc;

    public MenuCore(NPC npc) {
        this.npc = npc;
    }

    public Inventory getMainMenu() {
        List<String> lore = new ArrayList<>();
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 45, ChatColor.BLACK + "" + ChatColor.BOLD + "     Create a New NPC"));
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "MenuButtonTag");

        ItemStack nametag = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nametag.getItemMeta();
        nameMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "NameTag");
        nameMeta.setDisplayName(ChatColor.AQUA + "Change Name");
        lore.add(ChatColor.YELLOW + "The current name is " + ChatColor.AQUA + npc.getHologramName());
        nameMeta.setLore(lore);
        nametag.setItemMeta(nameMeta);

        ItemStack equipment = new ItemStack(Material.ARMOR_STAND);
        ItemMeta handMeta = equipment.getItemMeta();
        handMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "equipment");
        handMeta.setDisplayName(ChatColor.DARK_GREEN + "Change Item");
        lore.clear();
        lore.add(ChatColor.YELLOW + "The current euipment is ");
        lore.add(ChatColor.YELLOW + "Main Hand: " + ChatColor.AQUA + npc.getHandItem().getType());
        lore.add(ChatColor.YELLOW + "Offhand: " + ChatColor.AQUA + npc.getItemInOffhand().getType());
        lore.add(ChatColor.YELLOW + "Helmet: " + ChatColor.AQUA + npc.getHeadItem().getType());
        lore.add(ChatColor.YELLOW + "Chestplate: " + ChatColor.AQUA + npc.getChestItem().getType());
        lore.add(ChatColor.YELLOW + "Leggings: " + ChatColor.AQUA + npc.getLegsItem().getType());
        lore.add(ChatColor.YELLOW + "Boots: " + ChatColor.AQUA + npc.getBootsItem().getType());

        handMeta.setLore(lore);
        equipment.setItemMeta(handMeta);

        ItemStack positionsItem = new ItemStack(Material.COMPASS);
        ItemMeta positionMeta = positionsItem.getItemMeta();
        positionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "direction");
        positionMeta.setDisplayName(ChatColor.DARK_GREEN + "Facing Direction");
        double dir = npc.getFacingDirection();
        lore.clear();
        switch ((int) dir) {
            case 180 -> {
                lore.add("");
                lore.add(ChatColor.DARK_AQUA + "▸ North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case -135 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.DARK_AQUA + "▸ North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case -90 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.DARK_AQUA + "▸ East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case -45 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.DARK_AQUA + "▸ South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case 0 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.DARK_AQUA + "▸ South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case 45 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.DARK_AQUA + "▸ South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case 90 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.DARK_AQUA + "▸ West");
                lore.add(ChatColor.GREEN + "North West");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            case 135 -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.DARK_AQUA + "▸ North west");
                lore.add(ChatColor.GREEN + "Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
            default -> {
                lore.add("");
                lore.add(ChatColor.GREEN + "North");
                lore.add(ChatColor.GREEN + "North East");
                lore.add(ChatColor.GREEN + "East");
                lore.add(ChatColor.GREEN + "South East");
                lore.add(ChatColor.GREEN + "South");
                lore.add(ChatColor.GREEN + "South West");
                lore.add(ChatColor.GREEN + "West");
                lore.add(ChatColor.GREEN + "North west");
                lore.add(ChatColor.DARK_AQUA + "▸ Player Direction");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to change!");
            }
        }

        positionMeta.setLore(lore);
        positionsItem.setItemMeta(positionMeta);

        ItemStack resilientItem = new ItemStack(Material.BELL);
        ItemMeta resilientMeta = resilientItem.getItemMeta();
        lore.clear();
        lore.add(npc.isResilient() ? ChatColor.GREEN + "" + ChatColor.BOLD + "RESILIENT" : ChatColor.RED + "" + ChatColor.BOLD + "NOT RESILIENT");
        resilientMeta.setLore(lore);
        resilientMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "resilience");
        resilientMeta.setDisplayName(ChatColor.DARK_GREEN + "Change resilience");
        resilientItem.setItemMeta(resilientMeta);

        ItemStack confirmButton = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Confirm");
        confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM");
        confirmButton.setItemMeta(confirmMeta);

        ItemStack clickableButton;
        if (npc.isClickable()) {
            clickableButton = new ItemStack(Material.OAK_SAPLING);

            ItemStack actionsButton = new ItemStack(Material.RECOVERY_COMPASS);
            ItemMeta actionsButtonMeta = actionsButton.getItemMeta();
            actionsButtonMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "actions");
            actionsButtonMeta.setDisplayName(ChatColor.DARK_GREEN + "Change actions");
            lore.clear();
            lore.add(ChatColor.YELLOW + "The actions performed when ");
            lore.add(ChatColor.YELLOW + "interacting with the npc. ");
            actionsButtonMeta.setLore(lore);
            actionsButton.setItemMeta(actionsButtonMeta);
            inv.setItem(34, actionsButton);
        } else {
            clickableButton = new ItemStack(Material.DEAD_BUSH);
        }
        ItemMeta clickableMeta = clickableButton.getItemMeta();
        clickableMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "clickable");
        clickableMeta.setDisplayName(ChatColor.DARK_GREEN + "Change interactability");
        lore.clear();
        lore.add(npc.isClickable() ? ChatColor.GREEN + "" + ChatColor.BOLD + "CLICKABLE" : ChatColor.RED + "" + ChatColor.BOLD + "NOT CLICKABLE");
        clickableMeta.setLore(lore);
        clickableButton.setItemMeta(clickableMeta);

        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Cancel");
        cancelMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CANCEL");
        cancelButton.setItemMeta(cancelMeta);
        inv.setItem(13, MenuUtils.getSkinIcon(key, "changeSkin", "Change Skin", ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, "Changes the NPC's skin", "The current skin is " + npc.getSkinName(), "Click to change!", "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9"));
        inv.setItem(16, nametag);
        inv.setItem(10, positionsItem);
        inv.setItem(22, resilientItem);
        inv.setItem(25, clickableButton);
        inv.setItem(31, confirmButton);
        inv.setItem(36, cancelButton);
        inv.setItem(19, equipment);
        return inv;
    }

    public Inventory getArmorMenu() {
        ItemStack helm = npc.getHeadItem();
        ItemStack cp = npc.getChestItem();
        ItemStack legs = npc.getLegsItem();
        ItemStack boots = npc.getBootsItem();
        ItemStack hand = npc.getHandItem();
        ItemStack offhand = npc.getItemInOffhand();
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 54, ChatColor.BLACK + "" + ChatColor.BOLD + "     Edit NPC Equipment"));
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "EquipmentInv");

        ItemStack item1 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta1 = item1.getItemMeta();
        List<String> lore1 = new ArrayList<>();
        NamespacedKey key1 = new NamespacedKey(CustomNPCs.getInstance(), "NoClickey");
        meta1.getCustomTagContainer().setCustomTag(key1, ItemTagType.STRING, "PANE");
        meta1.setDisplayName(" ");
        lore1.add("");
        meta1.setLore(lore1);
        item1.setItemMeta(meta1);

        inv.setItem(10, item1);
        inv.setItem(11, item1);
        inv.setItem(12, item1);

        inv.setItem(14, item1);
        inv.setItem(15, item1);
        inv.setItem(16, item1);

        inv.setItem(19, item1);
        inv.setItem(20, item1);

        inv.setItem(24, item1);
        inv.setItem(25, item1);

        inv.setItem(28, item1);
        inv.setItem(29, item1);
        inv.setItem(30, item1);

        inv.setItem(32, item1);
        inv.setItem(33, item1);
        inv.setItem(34, item1);

        inv.setItem(37, item1);
        inv.setItem(38, item1);
        inv.setItem(39, item1);

        inv.setItem(41, item1);
        inv.setItem(42, item1);
        inv.setItem(43, item1);

        if (helm.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "helm");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Helmet Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a helmet to change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(13, item);
        } else {
            ItemMeta meta = helm.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "helm");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + helm.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a helmet to change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            helm.setItemMeta(meta);
            inv.setItem(13, helm);
        }
        if (cp.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "cp");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Chestplate Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a chestplate to change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(22, item);
        } else {
            ItemMeta meta = cp.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "cp");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + cp.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a chestplate to change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            cp.setItemMeta(meta);
            inv.setItem(22, cp);
        }
        if (legs.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "legs");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Leggings Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a pair of leggings");
            lore.add(ChatColor.YELLOW + "to change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(31, item);
        } else {
            ItemMeta meta = legs.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "legs");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + legs.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a pair of leggings");
            lore.add(ChatColor.YELLOW + "to change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            legs.setItemMeta(meta);
            inv.setItem(31, legs);
        }
        if (boots.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "boots");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Boots Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a pair of boots to ");
            lore.add(ChatColor.YELLOW + "change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(40, item);
        } else {
            ItemMeta meta = boots.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "boots");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + boots.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "a pair of boots to ");
            lore.add(ChatColor.YELLOW + "change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            boots.setItemMeta(meta);
            inv.setItem(40, boots);
        }
        if (hand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "hand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Hand Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "an item to change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(23, item);
        } else {
            ItemMeta meta = hand.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "hand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + hand.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "an item to change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            hand.setItemMeta(meta);
            inv.setItem(23, hand);
        }
        if (offhand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "offhand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + "Empty Offhand Slot");
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "an item to change.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(21, item);
        } else {
            ItemMeta meta = offhand.getItemMeta();
            List<String> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "offhand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.setDisplayName(ChatColor.GREEN + offhand.getType().toString());
            lore.add(ChatColor.YELLOW + "Click this slot with");
            lore.add(ChatColor.YELLOW + "an item to change.");
            lore.add(ChatColor.RED + "Rick click to remove");
            meta.setLore(lore);
            offhand.setItemMeta(meta);
            inv.setItem(21, offhand);
        }
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "close");
        closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CLOSE");
        closeButton.setItemMeta(closeMeta);
        inv.setItem(49, closeButton);

        return inv;
    }

    public Inventory getActionMenu() {
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 36, ChatColor.BLACK + "" + ChatColor.BOLD + "      Edit NPC Actions"));
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "ActionInv");

        List<Action> actions = npc.getActions();

        for (Action action : actions) {
            ItemStack item = new ItemStack(Material.BEDROCK);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            switch (action.getSubCommand()) {
                case "DISPLAY_TITLE" -> {
                    item.setType(Material.OAK_SIGN);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bDisplay Title"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "DISPLAY_TITLE");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe current title is: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "SEND_MESSAGE" -> {
                    item.setType(Material.PAPER);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend Message"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_MESSAGE");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe current message is: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "PLAY_SOUND" -> {
                    item.setType(Material.BELL);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&ePlay Sound"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "PLAY_SOUND");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe current sound is: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "RUN_COMMAND" -> {
                    item.setType(Material.ANVIL);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bRun Command"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "RUN_COMMAND");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe command is: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "ACTION_BAR" -> {
                    item.setType(Material.IRON_INGOT);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend Actionbar"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ACTION_BAR");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe current actionbar is: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "TELEPORT" -> {
                    item.setType(Material.ENDER_PEARL);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bTeleport Player"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "TELEPORT");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe current Teleport location is: '" + String.join(", ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
                case "SEND_TO_SERVER" -> {
                    item.setType(Material.GRASS_BLOCK);
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend To Bungeecord Server"));
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_TO_SERVER");
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eThe server is called: '" + String.join(" ", action.getArgs()) + "&r&e'"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&cRight Click to remove."));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eLeft Click to edit."));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        List<String> lore = new ArrayList<>();

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lGO BACK"));
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        closeMeta.setLore(lore);
        close.setItemMeta(closeMeta);
        lore.clear();
        inv.setItem(31, close);

        // Add New
        ItemStack newAction = new ItemStack(Material.LILY_PAD);
        ItemMeta actionMeta = newAction.getItemMeta();
        actionMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lNew Action"));
        actionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "new_action");
        newAction.setItemMeta(actionMeta);
        lore.clear();
        inv.addItem(newAction);

        return inv;
    }

    public Inventory getActionCustomizerMenu(Action action) {
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 45, ChatColor.BLACK + "" + ChatColor.BOLD + "      Edit NPC Action"));
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "CustomizeActionButton");

        // Go back to actions menu
        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Go Back"));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(36, goBack);

        // Confirm the action creation
        ItemStack confirm = new ItemStack(Material.LILY_PAD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "confirm");
        confirmMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aConfirm"));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(40, confirm);

        List<String> args = action.getArgsCopy();

        switch (action.getSubCommand()) {
            case "RUN_COMMAND" -> {
                ItemStack selectCommand = new ItemStack(Material.ANVIL);
                ItemMeta meta = selectCommand.getItemMeta();
                List<String> lore = new ArrayList<>();
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_command");
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eClick To Edit Command"));
                lore.add(ChatColor.translateAlternateColorCodes('&', "&b" + String.join(" ", args)));
                lore.add(ChatColor.translateAlternateColorCodes('&', "\n&eClick to change!"));
                meta.setLore(lore);
                selectCommand.setItemMeta(meta);
                inv.setItem(22, selectCommand);
            }
            case "DISPLAY_TITLE" -> {

                /* 6 buttons. 3 "displays" Fade in, stay, fade out. 3 buttons increment, 3 decrement.  1 button to edit title

                 # # # # # # # # #
                 # I # I # I # # #
                 # O # O # O # E #
                 # D # D # D # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - I = increment
                 - D = decrement
                 - O = display
                 - E = title displayed
                 - # = empty space
                */
                
                // Increments

                List<String> incLore = new ArrayList<>();
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Left CLick to add 1"));
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Right Click to add 5"));
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Shift + Right Click to add 20"));

                ItemStack incIn = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncIn = incIn.getItemMeta();
                metaIncIn.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease fade in duration"));
                metaIncIn.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_in");
                metaIncIn.setLore(incLore);
                incIn.setItemMeta(metaIncIn);
                inv.setItem(10, incIn);

                ItemStack incStay = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncStay = incStay.getItemMeta();
                metaIncStay.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease display duration"));
                metaIncStay.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_stay");
                metaIncStay.setLore(incLore);
                incStay.setItemMeta(metaIncStay);
                inv.setItem(12, incStay);

                ItemStack incOut = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncOut = incOut.getItemMeta();
                metaIncOut.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease fade out duration"));
                metaIncOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_out");
                metaIncOut.setLore(incLore);
                incOut.setItemMeta(metaIncOut);
                inv.setItem(14, incOut);

    
                
                //decrements

                List<String> decLore = new ArrayList<>();
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Left CLick to remove 1"));
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Right Click to remove 5"));
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Shift + Click to remove 20"));

                ItemStack decIn = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecIn = decIn.getItemMeta();
                metaDecIn.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease fade in duration"));
                metaDecIn.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_in");
                metaDecIn.setLore(decLore);
                decIn.setItemMeta(metaDecIn);
                inv.setItem(28, decIn);

                ItemStack decStay = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecStay = decStay.getItemMeta();
                metaDecStay.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease display duration"));
                metaDecStay.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_stay");
                metaDecStay.setLore(decLore);
                decStay.setItemMeta(metaDecStay);
                inv.setItem(30, decStay);

                ItemStack decOut = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecOut = decOut.getItemMeta();
                metaDecOut.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease fade out duration"));
                metaDecOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_out");
                metaDecOut.setLore(decLore);
                decOut.setItemMeta(metaDecOut);
                inv.setItem(32, decOut);

                // Displays

                List<String> displayLore = new ArrayList<>();
                displayLore.add(ChatColor.translateAlternateColorCodes('&', "&8In ticks"));


                ItemStack displayIn = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayIn = displayIn.getItemMeta();
                metaDisplayIn.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eFade in: " + args.get(0)));
                args.remove(0);
                metaDisplayIn.setLore(displayLore);
                displayIn.setItemMeta(metaDisplayIn);
                inv.setItem(19, displayIn);

                ItemStack displayStay = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayStay = displayStay.getItemMeta();
                metaDisplayStay.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDisplay time: " + args.get(0)));
                args.remove(0);
                metaDisplayStay.setLore(displayLore);
                displayStay.setItemMeta(metaDisplayStay);
                inv.setItem(21, displayStay);

                ItemStack displayOut = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayOut = displayOut.getItemMeta();
                metaDisplayOut.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eFade out: " + args.get(0)));
                args.remove(0);
                metaDisplayOut.setLore(displayLore);
                displayOut.setItemMeta(metaDisplayOut);
                inv.setItem(23, displayOut);

                ItemStack title = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta titleMeta = title.getItemMeta();
                titleMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_title");
                title.setItemMeta(titleMeta);
                inv.setItem(25, title);

            }
            case "SEND_MESSAGE" -> {
                /* 1 button to edit message

                 # # # # # # # # #
                 # # # # # # # # #
                 # # # # E # # # #
                 # # # # # # # # #
                 # # # # # # # # #

                 - E = Message sent
                 - # = empty space
                */

                ItemStack message = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta titleMeta = message.getItemMeta();
                titleMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_message");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
            case "PLAY_SOUND" -> {
                /* 4 buttons. 2 "displays" pitch, volume, 2 buttons increment, 2 decrement.  1 button to edit sound Enter it with a command for auto complete.

                 # # # # # # # # #
                 # I # I # # # # #
                 # O # O # # E # #
                 # D # D # # # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - I = increment
                 - D = decrement
                 - O = display
                 - E = Sound played
                 - # = empty space
                */
            }
            case "ACTION_BAR" -> {
                /* 1 button to edit message

                 # # # # # # # # #
                 # # # # # # # # #
                 # # # # E # # # #
                 # # # # # # # # #
                 # # # # # # # # #

                 - E = Message sent
                 - # = empty space
                */

                ItemStack message = new ItemStack(Material.IRON_INGOT);
                ItemMeta titleMeta = message.getItemMeta();
                titleMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_actionbar");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
            case "TELEPORT" -> {

                 /* 6 buttons. 3 "displays" Fade in, stay, fade out. 3 buttons increment, 3 decrement.  1 button to edit YAW

                 # # # # # # # # #
                 # I I I # I # I #
                 # x y z # P # Y #
                 # D D D # D # D #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - I = increment
                 - D = decrement
                 - O = display
                 - Y = Yaw
                 - # = empty space
                */


                List<String> incLore = new ArrayList<>();
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Left CLick to add 1"));
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Right Click to add 5"));
                incLore.add(ChatColor.translateAlternateColorCodes('&', "&8Shift + Right Click to add 20"));

                ItemStack incX = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncX = incX.getItemMeta();
                metaIncX.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease X coordinate"));
                metaIncX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_x");
                metaIncX.setLore(incLore);
                incX.setItemMeta(metaIncX);
                inv.setItem(10, incX);

                ItemStack incY = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncY = incY.getItemMeta();
                metaIncY.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease Y coordinate"));
                metaIncY.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_y");
                metaIncY.setLore(incLore);
                incY.setItemMeta(metaIncY);
                inv.setItem(11, incY);

                ItemStack incZ = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncZ = incZ.getItemMeta();
                metaIncZ.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease Z coordinate"));
                metaIncZ.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_z");
                metaIncZ.setLore(incLore);
                incZ.setItemMeta(metaIncZ);
                inv.setItem(12, incZ);

                ItemStack incYaw = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncYaw = incYaw.getItemMeta();
                metaIncYaw.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease yaw"));
                metaIncYaw.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_yaw");
                metaIncYaw.setLore(incLore);
                incYaw.setItemMeta(metaIncYaw);
                inv.setItem(16, incYaw);

                ItemStack incPitch = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncPitch = incPitch.getItemMeta();
                metaIncPitch.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eIncrease pitch"));
                metaIncPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_pitch");
                metaIncPitch.setLore(incLore);
                incPitch.setItemMeta(metaIncPitch);
                inv.setItem(14, incPitch);


                //decrements

                List<String> decLore = new ArrayList<>();
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Left CLick to remove 1"));
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Right Click to remove 5"));
                decLore.add(ChatColor.translateAlternateColorCodes('&', "&8Shift + Right Click to remove 20"));

                ItemStack decX = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecX = decX.getItemMeta();
                metaDecX.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease X coordinate"));
                metaDecX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_z");
                metaDecX.setLore(decLore);
                decX.setItemMeta(metaDecX);
                inv.setItem(28, decX);

                ItemStack decY = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecY = decY.getItemMeta();
                metaDecY.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease Y coordinate"));
                metaDecY.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_y");
                metaDecY.setLore(decLore);
                decY.setItemMeta(metaDecY);
                inv.setItem(29, decY);

                ItemStack decZ = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecOut = decZ.getItemMeta();
                metaDecOut.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease Z coordinate"));
                metaDecOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_z");
                metaDecOut.setLore(decLore);
                decZ.setItemMeta(metaDecOut);
                inv.setItem(30, decZ);

                ItemStack decYaw = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecYaw = decYaw.getItemMeta();
                metaDecYaw.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease yaw"));
                metaDecYaw.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_yaw");
                metaDecYaw.setLore(decLore);
                decYaw.setItemMeta(metaDecYaw);
                inv.setItem(34, decYaw);

                ItemStack decPitch = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecPitch = decPitch.getItemMeta();
                metaDecPitch.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eDecrease pitch"));
                metaDecPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_pitch");
                metaDecPitch.setLore(decLore);
                decPitch.setItemMeta(metaDecPitch);
                inv.setItem(32, decPitch);

                // Displays

                List<String> displayLore = new ArrayList<>();
                displayLore.add(ChatColor.translateAlternateColorCodes('&', "&8In blocks"));


                ItemStack displayIn = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayIn = displayIn.getItemMeta();
                metaDisplayIn.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eX: " + args.get(0)));
                args.remove(0);
                metaDisplayIn.setLore(displayLore);
                displayIn.setItemMeta(metaDisplayIn);
                inv.setItem(19, displayIn);

                ItemStack displayStay = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayStay = displayStay.getItemMeta();
                metaDisplayStay.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eY: " + args.get(0)));
                args.remove(0);
                metaDisplayStay.setLore(displayLore);
                displayStay.setItemMeta(metaDisplayStay);
                inv.setItem(20, displayStay);

                ItemStack displayOut = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayOut = displayOut.getItemMeta();
                metaDisplayOut.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eZ: " + args.get(0)));
                args.remove(0);
                metaDisplayOut.setLore(displayLore);
                displayOut.setItemMeta(metaDisplayOut);
                inv.setItem(21, displayOut);

                ItemStack displayYaw = new ItemStack(Material.COMPASS);
                ItemMeta displayYawMeta = displayYaw.getItemMeta();
                displayYawMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&eYaw: " + args.get(0)));
                args.remove(0);
                displayYaw.setItemMeta(displayYawMeta);
                inv.setItem(25, displayYaw);

                ItemStack displayPitch = new ItemStack(Material.COMPASS);
                ItemMeta displayPitchMeta = displayPitch.getItemMeta();
                displayPitchMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&ePitch: " + args.get(0)));
                args.remove(0);
                displayPitch.setItemMeta(displayPitchMeta);
                inv.setItem(23, displayPitch);



            }
            case "SEND_TO_SERVER" -> {
                /* 1 button to edit message

                 # # # # # # # # #
                 # # # # # # # # #
                 # # # # S # # # #
                 # # # # # # # # #
                 # # # # # # # # #

                 - S = Server Name
                 - # = empty space
                */

                ItemStack message = new ItemStack(Material.GRASS_BLOCK);
                ItemMeta titleMeta = message.getItemMeta();
                titleMeta.setDisplayName(ChatColor.YELLOW + "Selected server: " + String.join(" ", args));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_server");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
        }

        return inv;
    }

    public Inventory getNewActionMenu(){
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 27, ChatColor.BLACK + "" + ChatColor.BOLD + "       New NPC Action"));
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "NewActionButton");

        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Go Back"));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(18, goBack);

        // make and add the npc action types.

        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta  meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();


            item.setType(Material.OAK_SIGN);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bDisplay Title"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "DISPLAY_TITLE");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&eDisplays a title for the player."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.PAPER);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend Message"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_MESSAGE");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&eSends the player a message."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.BELL);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&ePlay Sound"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "PLAY_SOUND");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&ePlays a sound for the player."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.ANVIL);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bRun Command"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "RUN_COMMAND");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&eRuns a command as the player."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.IRON_INGOT);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend Actionbar"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ACTION_BAR");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e Sends the player an actionbar."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.ENDER_PEARL);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bTeleport Player"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "TELEPORT");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&eTeleports a player upon interacting."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);

            item.setType(Material.GRASS_BLOCK);
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bSend To Bungeecord Server"));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_TO_SERVER");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&eSends a player to a bungeecord"));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&eserver upon interacting."));
            meta.setLore(lore);
            item.setItemMeta(meta);
            lore.clear();
            inv.addItem(item);




        return inv;
    }

    public NPC getNpc() {
        return this.npc;
    }
}
