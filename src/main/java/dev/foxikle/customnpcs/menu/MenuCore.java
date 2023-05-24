package dev.foxikle.customnpcs.menu;

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

    public Inventory getMainMenu(){
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
        lore.add(ChatColor.YELLOW  + "Main Hand: " + ChatColor.AQUA + npc.getHandItem().getType());
        lore.add(ChatColor.YELLOW  + "Offhand: " + ChatColor.AQUA + npc.getItemInOffhand().getType());
        lore.add(ChatColor.YELLOW  + "Helmet: " + ChatColor.AQUA + npc.getHeadItem().getType());
        lore.add(ChatColor.YELLOW  + "Chestplate: " + ChatColor.AQUA + npc.getChestItem().getType());
        lore.add(ChatColor.YELLOW  + "Leggings: " + ChatColor.AQUA + npc.getLegsItem().getType());
        lore.add(ChatColor.YELLOW  + "Boots: " + ChatColor.AQUA + npc.getBootsItem().getType());

        handMeta.setLore(lore);
        equipment.setItemMeta(handMeta);

        ItemStack positionsItem = new ItemStack(Material.COMPASS);
        ItemMeta positionMeta = positionsItem.getItemMeta();
        positionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "direction");
        positionMeta.setDisplayName(ChatColor.DARK_GREEN + "Facing Direction");
        double dir = npc.getFacingDirection();
        lore.clear();
        switch ((int) dir){
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
                lore.add(ChatColor.YELLOW + "Click to change!");            }
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
        if(npc.isClickable()){
            clickableButton = new ItemStack(Material.OAK_SAPLING);

            ItemStack commandButton = new ItemStack(Material.RECOVERY_COMPASS);
            ItemMeta commandButtonMeta = commandButton.getItemMeta();
            commandButtonMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "command");
            commandButtonMeta.setDisplayName(ChatColor.DARK_GREEN + "Change command");
            lore.clear();
            lore.add(ChatColor.YELLOW + "The command run when ");
            lore.add(ChatColor.YELLOW + "interacting with the npc. ");
            lore.add(ChatColor.YELLOW + "The current command is:");
            lore.add(ChatColor.AQUA + npc.getCommand());
            commandButtonMeta.setLore(lore);
            commandButton.setItemMeta(commandButtonMeta);
            inv.setItem(34, commandButton);
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

    public Inventory getArmorMenu(){
        ItemStack helm = npc.getHeadItem();
        ItemStack cp = npc.getChestItem();
        ItemStack legs = npc.getLegsItem();
        ItemStack boots = npc.getBootsItem();
        ItemStack hand = npc.getHandItem();
        ItemStack offhand = npc.getItemInOffhand();
        Inventory inv = MenuUtils.addBorder(Bukkit.createInventory(null, 54, ChatColor.BLACK + "" + ChatColor.BOLD + "     Edit NPC_1_19_1_R1 Equipment"));
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

        if(helm.getType().isAir()){
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
        if(cp.getType().isAir()){
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
        if(legs.getType().isAir()){
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
        if(boots.getType().isAir()){
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
        if(hand.getType().isAir()){
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
        if(offhand.getType().isAir()){
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

    public NPC getNpc(){
        return this.npc;
    }
}
