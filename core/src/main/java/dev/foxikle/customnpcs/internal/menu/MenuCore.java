package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Handles menu creation
 */
public class MenuCore {

    private final InternalNPC npc;
    private final CustomNPCs plugin;

    /**
     * <p> The constructor to make a menu factory
     * </p>
     * @param npc The NPC to edit
     * @param plugin The instance of the Main class
     */
    public MenuCore(InternalNPC npc, CustomNPCs plugin) {
        this.npc = npc;
        this.plugin = plugin;
    }

    /**
     * <p>Gets the main menu
     * </p>
     * @return The Inventory representing the Main NPC menu
     */
    public Inventory getMainMenu() {
        List<Component> lore = new ArrayList<>();
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 45, Component.text("     Create a New NPC", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "MenuButtonTag");

        ItemStack nametag = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nametag.getItemMeta();
        nameMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "NameTag");
        nameMeta.displayName(Component.text("Change Name", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(Component.text("The current name is ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(plugin.getMiniMessage().deserialize(npc.getSettings().getName())));
        nameMeta.lore(lore);
        nametag.setItemMeta(nameMeta);

        ItemStack equipment = new ItemStack(Material.ARMOR_STAND);
        ItemMeta handMeta = equipment.getItemMeta();
        handMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "equipment");
        handMeta.displayName(Component.text("Change Item", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.clear();
        lore.add(Component.text("The current equipment is ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
        lore.add(Component.text("Main Hand: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text((npc.getEquipment().getHand().getType().toString()))));
        lore.add(Component.text("Offhand: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text(npc.getEquipment().getOffhand().getType().toString())));
        lore.add(Component.text("Helmet: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text((npc.getEquipment().getHead().getType().toString()))));
        lore.add(Component.text("Chestplate: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text((npc.getEquipment().getChest().getType().toString()))));
        lore.add(Component.text("Leggings: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text((npc.getEquipment().getLegs().getType().toString()))));
        lore.add(Component.text("Boots: ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW).append(Component.text((npc.getEquipment().getBoots().getType().toString()))));

        handMeta.lore(lore);
        equipment.setItemMeta(handMeta);

        ItemStack positionsItem = new ItemStack(Material.COMPASS);
        ItemMeta positionMeta = positionsItem.getItemMeta();
        positionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "direction");
        positionMeta.displayName(Component.text("Facing Direction", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        double dir = npc.getSettings().getDirection();
        lore.clear();
        switch ((int) dir) {
            case 180 -> {
                lore.add(Component.empty());
                lore.add(Component.text("▸ North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case -135 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case -90 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case -45 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case 0 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case 45 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case 90 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            case 135 -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ North west").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.text("Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
            default -> {
                lore.add(Component.empty());
                lore.add(Component.text("North").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South East").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("South West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("North West").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
                lore.add(Component.text("▸ Player Direction").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_AQUA));
                lore.add(Component.empty());
                lore.add(Component.text("Click to change!").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            }
        }

        positionMeta.lore(lore);
        positionsItem.setItemMeta(positionMeta);

        ItemStack resilientItem = new ItemStack(Material.BELL);
        ItemMeta resilientMeta = resilientItem.getItemMeta();
        lore.clear();
        lore.add(npc.getSettings().isResilient() ? Component.text("RESILIENT").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD) : Component.text("NOT RESILIENT").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        resilientMeta.lore(lore);
        resilientMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "resilience");
        resilientMeta.displayName(Component.text("Change resilience", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        resilientItem.setItemMeta(resilientMeta);

        ItemStack confirmButton = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Confirm");
        confirmMeta.displayName(Component.text("CONFIRM", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        confirmButton.setItemMeta(confirmMeta);

        ItemStack interactableButton;
        if (npc.getSettings().isInteractable()) {
            interactableButton = new ItemStack(Material.OAK_SAPLING);

            ItemStack actionsButton = new ItemStack(Material.RECOVERY_COMPASS);
            ItemMeta actionsButtonMeta = actionsButton.getItemMeta();
            actionsButtonMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "actions");
            actionsButtonMeta.displayName(Component.text("Change actions", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.clear();
            lore.add(Component.text("The actions performed when ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            lore.add(Component.text("interacting with the npc. ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            actionsButtonMeta.lore(lore);
            actionsButtonMeta.lore();
            actionsButton.setItemMeta(actionsButtonMeta);
            inv.setItem(34, actionsButton);
        } else {
            interactableButton = new ItemStack(Material.DEAD_BUSH);
        }
        ItemMeta clickableMeta = interactableButton.getItemMeta();
        clickableMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "clickable");
        clickableMeta.displayName(Component.text("Change interactability", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.clear();
        lore.add(npc.getSettings().isInteractable() ? Component.text("INTERACTABLE").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD) : Component.text("NOT INTERACTABLE").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        clickableMeta.lore(lore);
        interactableButton.setItemMeta(clickableMeta);

        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Cancel");
        cancelMeta.displayName(Component.text("CANCEL", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        cancelButton.setItemMeta(cancelMeta);
        inv.setItem(13, plugin.getMenuUtils().getSkinIcon(key, "changeSkin", "Change Skin", ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, "Changes the NPC's skin", "The current skin is " + npc.getSettings().getSkinName(), "Click to change!", "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9"));
        inv.setItem(16, nametag);
        inv.setItem(10, positionsItem);
        inv.setItem(22, resilientItem);
        inv.setItem(25, interactableButton);
        inv.setItem(31, confirmButton);
        inv.setItem(36, cancelButton);
        inv.setItem(19, equipment);
        return inv;
    }

    /**
     * <p>Gets the menu displaying the NPC's current armor
     * </p>
     * @return The Inventory representing the Armor menu
     */
    public Inventory getArmorMenu() {
        ItemStack helm = npc.getEquipment().getHead();
        ItemStack cp = npc.getEquipment().getChest();
        ItemStack legs = npc.getEquipment().getLegs();
        ItemStack boots = npc.getEquipment().getBoots();
        ItemStack hand = npc.getEquipment().getHand();
        ItemStack offhand = npc.getEquipment().getOffhand();
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 54, Component.text("     Edit NPC Equipment", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "EquipmentInv");

        ItemStack item1 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta1 = item1.getItemMeta();
        NamespacedKey key1 = new NamespacedKey(plugin, "NoClickey");
        meta1.getCustomTagContainer().setCustomTag(key1, ItemTagType.STRING, "PANE");
        meta1.displayName(Component.empty());
        meta1.lore(Collections.emptyList());
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
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "helm");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Helmet Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a helmet to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(13, item);
        } else {
            ItemMeta meta = helm.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "helm");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(helm.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a helmet to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            helm.setItemMeta(meta);
            inv.setItem(13, helm);
        }
        if (cp.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "cp");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Chestplate Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a chestplate to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(22, item);
        } else {
            ItemMeta meta = cp.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "cp");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(cp.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a chestplate to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            cp.setItemMeta(meta);
            inv.setItem(22, cp);
        }
        if (legs.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "legs");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Leggings Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of leggings", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(31, item);
        } else {
            ItemMeta meta = legs.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "legs");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(legs.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of leggings", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            legs.setItemMeta(meta);
            inv.setItem(31, legs);
        }
        if (boots.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "boots");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Boots Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of boots to ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(40, item);
        } else {
            ItemMeta meta = boots.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "boots");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(boots.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of boots to ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            boots.setItemMeta(meta);
            inv.setItem(40, boots);
        }
        if (hand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "hand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Hand Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(23, item);
        } else {
            ItemMeta meta = hand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "hand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(hand.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            hand.setItemMeta(meta);
            inv.setItem(23, hand);
        }
        if (offhand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "offhand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Offhand Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            inv.setItem(21, item);
        } else {
            ItemMeta meta = offhand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "offhand");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(offhand.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            offhand.setItemMeta(meta);
            inv.setItem(21, offhand);
        }
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "close");
        closeMeta.displayName(Component.text("CLOSE", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorate(TextDecoration.BOLD));
        closeButton.setItemMeta(closeMeta);
        inv.setItem(49, closeButton);

        return inv;
    }

    /**
     * <p>Gets the menu displaying all curent actions
     * </p>
     * @return The Inventory representing the Actions menu
     */
    public Inventory getActionMenu() {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 54, Component.text("      Edit NPC Actions", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "ActionInv");

        List<Action> actions = npc.getActions();

        for (Action action : actions) {
            ItemStack item = new ItemStack(Material.BEDROCK);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            List<String> args = action.getArgsCopy();
            if(action.getActionType() != ActionType.TOGGLE_FOLLOWING)
                lore.add(Component.text("Delay (ticks): " + action.getDelay()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
            lore.add(Component.empty());
            switch (action.getSubCommand()) {
                case "DISPLAY_TITLE" -> {
                    item.setType(Material.OAK_SIGN);
                    int fIn = Integer.parseInt(args.get(0));
                    int stay = Integer.parseInt(args.get(1));
                    int fOut = Integer.parseInt(args.get(1));
                    args.remove(0);
                    args.remove(0);
                    args.remove(0);
                    meta.displayName(Component.text("Display Title").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.AQUA));
                    lore.add(Component.text("The current title is: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(String.join(" ", args))).append(Component.text("'", NamedTextColor.YELLOW)));
                    lore.add(Component.text("Fade in: " + fIn).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.AQUA));
                    lore.add(Component.text("Stay: " + stay).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.AQUA));
                    lore.add(Component.text("Fade out: " + fOut).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.AQUA));
                }
                case "SEND_MESSAGE" -> {
                    item.setType(Material.PAPER);
                    meta.displayName(Component.text("Send Message", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current message is: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(String.join(" ", args))).append(Component.text("'", NamedTextColor.YELLOW)));
                }
                case "PLAY_SOUND" -> {
                    item.setType(Material.BELL);
                    meta.displayName(Component.text("Play Sound", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    int pitch = Integer.parseInt(args.get(0));
                    args.remove(0);
                    int volume = Integer.parseInt(args.get(0));
                    args.remove(0);
                    lore.add(Component.text("The current sound is: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Pitch: " + pitch, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Volume: " + volume, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "RUN_COMMAND" -> {
                    item.setType(Material.ANVIL);
                    meta.displayName(Component.text("Run Command", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The command is: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "ACTION_BAR" -> {
                    item.setType(Material.IRON_INGOT);
                    meta.displayName(Component.text("Send Actionbar", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current actionbar is: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(plugin.getMiniMessage().deserialize(String.join(" ", args))).append(Component.text("'", NamedTextColor.YELLOW)));
                }
                case "TELEPORT" -> {
                    item.setType(Material.ENDER_PEARL);
                    int x = Integer.parseInt(args.get(0));
                    int y = Integer.parseInt(args.get(1));
                    int z = Integer.parseInt(args.get(2));
                    int pitch = Integer.parseInt(args.get(3));
                    int yaw = Integer.parseInt(args.get(4));
                    meta.displayName(Component.text("Teleport Player", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current Teleport location is:", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("X: " + x, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Y: " + z, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Z: " + y, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Pitch: " + pitch, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Yaw: " + yaw, NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "GIVE_EXP" -> {
                    item.setType(Material.EXPERIENCE_BOTTLE);
                    meta.displayName(Component.text("Give Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current xp to give is: " + args.get(0) + " " + (args.get(1).equalsIgnoreCase("true") ? "levels" : "points"), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "REMOVE_EXP" -> {
                    item.setType(Material.GLASS_BOTTLE);
                    meta.displayName(Component.text("Remove Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current xp to remove is: " + args.get(0) + " " + (args.get(1).equalsIgnoreCase("true") ? "levels" : "points"), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "ADD_EFFECT" -> {
                    item.setType(Material.BREWING_STAND);
                    meta.displayName(Component.text("Give Effect", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Effect: '" + args.get(3) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Duration: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Amplifier: " + args.get(1), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Hide particles: " + args.get(2), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "REMOVE_EFFECT" -> {
                    item.setType(Material.MILK_BUCKET);
                    meta.displayName(Component.text("Remove Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Effect: '" + args.get(0) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "SEND_TO_SERVER" -> {
                    item.setType(Material.GRASS_BLOCK);
                    meta.displayName(Component.text("Send To Bungeecord/Velocity Server", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Server: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case "TOGGLE_FOLLOWING" -> {
                    item.setType(Material.LEAD);
                    meta.displayName(Component.text("[WIP]", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(" Start / Stop Following", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                }
            }
            lore.add(Component.empty());
            lore.add(Component.text("Right Click to remove.", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Left Click to edit.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            NamespacedKey actionKey = new NamespacedKey(plugin, "SerializedAction");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "actionDisplay");
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.toJson());
            meta.lore(lore);
            item.setItemMeta(meta);
            if (!inv.contains(item))
                inv.addItem(item);
        }

        // Close Button
        ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("GO BACK", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        close.setItemMeta(closeMeta);
        inv.setItem(45, close);

        // Add New
        ItemStack newAction = new ItemStack(Material.LILY_PAD);
        ItemMeta actionMeta = newAction.getItemMeta();
        actionMeta.displayName(Component.text("New Action", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        actionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "new_action");
        newAction.setItemMeta(actionMeta);
        inv.addItem(newAction);

        return inv;
    }

    public Inventory getConditionMenu(Action action) {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 36, Component.text("  Edit Action Conditionals", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "ConditionInv");
        NamespacedKey dataKey = new NamespacedKey(plugin, "SerializedCondition");
        if(action.getConditionals() != null) {
            for (Conditional c : action.getConditionals()) {
                ItemStack item = new ItemStack(Material.BEDROCK);
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = new ArrayList<>();
                if (c.getType() == Conditional.Type.NUMERIC) {
                    item.setType(Material.POPPED_CHORUS_FRUIT);
                    meta.displayName(Component.text("Numeric Condition", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                } else if (c.getType() == Conditional.Type.LOGICAL) {
                    item.setType(Material.COMPARATOR);
                    meta.displayName(Component.text("Logical Condition", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                lore.add(Component.empty());
                lore.add(Component.text("Comparator: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(c.getComparator().name(), NamedTextColor.LIGHT_PURPLE)).append(Component.text("'", NamedTextColor.YELLOW)));
                lore.add(Component.text("Value: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(c.getValue().name(), NamedTextColor.LIGHT_PURPLE)).append(Component.text("'", NamedTextColor.YELLOW)));
                lore.add(Component.text("Target Value: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(c.getTarget(), NamedTextColor.LIGHT_PURPLE)).append(Component.text("'", NamedTextColor.YELLOW)));
                lore.add(Component.empty());
                lore.add(Component.text("Right Click to remove.", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Left Click to edit.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "actionDisplay");
                meta.getPersistentDataContainer().set(dataKey, PersistentDataType.STRING, c.toJson());
                meta.lore(lore);
                item.setItemMeta(meta);
                if (!inv.contains(item))
                    inv.addItem(item);
            }
        }
        List<Component> lore = new ArrayList<>();

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("GO BACK", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        closeMeta.lore(lore);
        close.setItemMeta(closeMeta);
        lore.clear();
        inv.setItem(31, close);

        // Add New
        ItemStack newCondition = new ItemStack(Material.LILY_PAD);
        ItemMeta conditionMeta = newCondition.getItemMeta();
        conditionMeta.displayName(Component.text("New Condition", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        conditionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "new_condition");
        newCondition.setItemMeta(conditionMeta);
        lore.clear();
        inv.addItem(newCondition);

        // Change Mode
        ItemStack changeMode = new ItemStack(action.getMode() == Conditional.SelectionMode.ALL ? Material.GREEN_CANDLE : Material.RED_CANDLE);
        ItemMeta changeModeMeta = changeMode.getItemMeta();
        changeModeMeta.displayName(Component.text("Change Mode", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(action.getMode() == Conditional.SelectionMode.ALL ? Component.text("Match ALL Conditions", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) : Component.text("Match ONE Condition", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        changeModeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "change_mode");
        changeModeMeta.lore(lore);
        changeMode.setItemMeta(changeModeMeta);
        lore.clear();
        inv.setItem(35, changeMode);

        return inv;
    }

    /**
     * <p>Gets the menu to customize an action
     * </p>
     * @param action The Action to customize
     * @return The Inventory representing the action to customize
     */
    public Inventory getActionCustomizerMenu(Action action) {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 45, Component.text("       Edit NPC Action", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "CustomizeActionButton");

        // lores
        List<Component> incLore = new ArrayList<>();
        incLore.add(Component.text("Left CLick to add 1", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        incLore.add(Component.text("Right Click to add 5", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        incLore.add(Component.text("Shift + Right Click to add 20", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        List<Component> decLore = new ArrayList<>();
        decLore.add(Component.text("Left CLick to remove 1", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        decLore.add(Component.text("Right Click to remove 5", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        decLore.add(Component.text("Shift + Click to remove 20", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        // Go back to actions menu
        ItemStack decDelay = new ItemStack(Material.RED_DYE);
        ItemMeta decDelayItemMeta = decDelay.getItemMeta();
        decDelayItemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_delay");
        decDelayItemMeta.displayName(Component.text("Decrement Delay", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        decDelay.setItemMeta(decDelayItemMeta);
        inv.setItem(3, decDelay);// Go back to actions menu

        ItemStack displayDelay = new ItemStack(Material.CLOCK);
        ItemMeta delayMeta = displayDelay.getItemMeta();
        delayMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
        delayMeta.displayName(Component.text("Delay ticks: " + action.getDelay(), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        displayDelay.setItemMeta(delayMeta);
        inv.setItem(4, displayDelay);// Go back to actions menu

        ItemStack incDelay = new ItemStack(Material.LIME_DYE);
        ItemMeta incDelayItemMeta = incDelay.getItemMeta();
        incDelayItemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_delay");
        incDelayItemMeta.displayName(Component.text("Increment Delay", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        incDelay.setItemMeta(incDelayItemMeta);
        inv.setItem(5, incDelay);

        // Go back to actions menu
        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.displayName(Component.text("Go Back", NamedTextColor.GOLD));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(36, goBack);

        //Edit conditionals
        ItemStack editConditionals = new ItemStack(Material.COMPARATOR);
        ItemMeta editConditionalsMeta = editConditionals.getItemMeta();
        editConditionalsMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_conditionals");
        editConditionalsMeta.displayName(Component.text("Edit Conditionals", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        editConditionals.setItemMeta(editConditionalsMeta);
        inv.setItem(44, editConditionals);

        // Confirm the action creation
        ItemStack confirm = new ItemStack(Material.LILY_PAD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "confirm");
        confirmMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(40, confirm);

        List<String> args = action.getArgsCopy();

        switch (action.getSubCommand()) {
            case "RUN_COMMAND" -> {
                ItemStack selectCommand = new ItemStack(Material.ANVIL);
                ItemMeta meta = selectCommand.getItemMeta();
                List<Component> lore = new ArrayList<>();
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_command");
                meta.displayName(Component.text("Click To Edit Command", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text(String.join(" ", args), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
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

                ItemStack incIn = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncIn = incIn.getItemMeta();
                metaIncIn.displayName(Component.text("Increase fade in duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncIn.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_in");
                metaIncIn.lore(incLore);
                incIn.setItemMeta(metaIncIn);
                inv.setItem(10, incIn);

                ItemStack incStay = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncStay = incStay.getItemMeta();
                metaIncStay.displayName(Component.text("Increase display duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncStay.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_stay");
                metaIncStay.lore(incLore);
                incStay.setItemMeta(metaIncStay);
                inv.setItem(12, incStay);

                ItemStack incOut = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncOut = incOut.getItemMeta();
                metaIncOut.displayName(Component.text("Increase fade out duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_out");
                metaIncOut.lore(incLore);
                incOut.setItemMeta(metaIncOut);
                inv.setItem(14, incOut);


                //decrements

                ItemStack decIn = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecIn = decIn.getItemMeta();
                metaDecIn.displayName(Component.text("Decrease fade in duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecIn.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_in");
                metaDecIn.lore(decLore);
                decIn.setItemMeta(metaDecIn);
                inv.setItem(28, decIn);

                ItemStack decStay = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecStay = decStay.getItemMeta();
                metaDecStay.displayName(Component.text("Decrease display duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecStay.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_stay");
                metaDecStay.lore(decLore);
                decStay.setItemMeta(metaDecStay);
                inv.setItem(30, decStay);

                ItemStack decOut = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecOut = decOut.getItemMeta();
                metaDecOut.displayName(Component.text("Decrease fade out duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_out");
                metaDecOut.lore(decLore);
                decOut.setItemMeta(metaDecOut);
                inv.setItem(32, decOut);

                // Displays

                List<Component> displayLore = new ArrayList<>();
                displayLore.add(Component.text("In ticks", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));


                ItemStack displayIn = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayIn = displayIn.getItemMeta();
                metaDisplayIn.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayIn.displayName(Component.text("Fade in: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayIn.lore(displayLore);
                displayIn.setItemMeta(metaDisplayIn);
                inv.setItem(19, displayIn);

                ItemStack displayStay = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayStay = displayStay.getItemMeta();
                metaDisplayStay.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayStay.displayName(Component.text("Display time: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayStay.lore(displayLore);
                displayStay.setItemMeta(metaDisplayStay);
                inv.setItem(21, displayStay);

                ItemStack displayOut = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayOut = displayOut.getItemMeta();
                metaDisplayOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayOut.displayName(Component.text("Fade out: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayOut.lore(displayLore);
                displayOut.setItemMeta(metaDisplayOut);
                inv.setItem(23, displayOut);

                ItemStack title = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta titleMeta = title.getItemMeta();
                titleMeta.displayName(plugin.getMiniMessage().deserialize(String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_title");
                title.setItemMeta(titleMeta);
                inv.setItem(25, title);

            }
            case "ADD_EFFECT" -> {

                /*
                 # # # # # # # # #
                 # I # I # # # # #
                 # O # O # O # O #
                 # D # D # # # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - I = increment
                 - D = decrement
                 - O = display
                 - # = empty space
                */

                // Increments

                ItemStack incDur = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncDur = incDur.getItemMeta();
                metaIncDur.displayName(Component.text("Increase effect duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncDur.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_duration");
                metaIncDur.lore(incLore);
                incDur.setItemMeta(metaIncDur);
                inv.setItem(10, incDur);

                ItemStack incAmp = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncAmp = incAmp.getItemMeta();
                metaIncAmp.displayName(Component.text("Increase effect aplifier", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncAmp.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_amplifier");
                metaIncAmp.lore(incLore);
                incAmp.setItemMeta(metaIncAmp);
                inv.setItem(12, incAmp);

                //decrements

                ItemStack decDur = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecDur = decDur.getItemMeta();
                metaDecDur.displayName(Component.text("Decrease effect duration", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecDur.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_duration");
                metaDecDur.lore(decLore);
                decDur.setItemMeta(metaDecDur);
                inv.setItem(28, decDur);

                ItemStack decAmp = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecAmp = decAmp.getItemMeta();
                metaDecAmp.displayName(Component.text("Decrease effect aplifier", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecAmp.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_amplifier");
                metaDecAmp.lore(decLore);
                decAmp.setItemMeta(metaDecAmp);
                inv.setItem(30, decAmp);

                // Displays

                List<Component> displayLore = new ArrayList<>();
                displayLore.add(Component.text("In ticks", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));


                ItemStack displayDuration = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayDuration = displayDuration.getItemMeta();
                metaDisplayDuration.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayDuration.displayName(Component.text("Duration: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayDuration.lore(displayLore);
                displayDuration.setItemMeta(metaDisplayDuration);
                inv.setItem(19, displayDuration);

                ItemStack displayAmplifier = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayAmplifier = displayAmplifier.getItemMeta();
                metaDisplayAmplifier.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayAmplifier.displayName(Component.text("Amplifier: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayAmplifier.setItemMeta(metaDisplayAmplifier);
                inv.setItem(21, displayAmplifier);

                ItemStack hideParticlesItem = new ItemStack(action.getArgs().get(2).equalsIgnoreCase("true") ? Material.GREEN_CANDLE : Material.RED_CANDLE);
                ItemMeta metaHideParticles = hideParticlesItem.getItemMeta();
                metaHideParticles.getPersistentDataContainer().set(key, PersistentDataType.STRING, "toggle_hide_particles");
                metaHideParticles.displayName(Component.text("Hide Particles: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                hideParticlesItem.setItemMeta(metaHideParticles);
                inv.setItem(23, hideParticlesItem);

                ItemStack potion = new ItemStack(Material.POTION);
                ItemMeta potionMeta = potion.getItemMeta();
                potionMeta.displayName(Component.text("Effect to give", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                potionMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                potionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_add_effect");
                List<Field> fields = Arrays.stream(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();
                List<Component> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if(!Objects.equals(action.getArgs().get(3), field.getName()))
                        lore.add(Component.text(field.getName(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    else
                        lore.add(Component.text("▸ " + field.getName(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                });
                potionMeta.lore(lore);
                potion.setItemMeta(potionMeta);
                inv.setItem(25, potion);

            }
            case "REMOVE_EFFECT" -> {

                /*
                 # # # # # # # # #
                 # # # # # # # # #
                 # # # # O # # # #
                 # # # # # # # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - O = display
                 - # = empty space
                */


                ItemStack potion = new ItemStack(Material.POTION);
                ItemMeta potionMeta = potion.getItemMeta();
                potionMeta.displayName(Component.text("Effect to remove", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                potionMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_remove_effect");
                List<Field> fields = Arrays.stream(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();
                List<Component> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if(!Objects.equals(action.getArgs().get(3), field.getName()))
                        lore.add(Component.text(field.getName(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    else
                        lore.add(Component.text("▸ " + field.getName(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                });
                potionMeta.lore(lore);
                potion.setItemMeta(potionMeta);
                inv.setItem(22, potion);

            }

            case "GIVE_EXP" -> {

                /*
                 # # # # # # # # #
                 # # # I # # # # #
                 # # # O # O # # #
                 # # # D # # # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - O = display
                 - # = empty space
                */

                ItemStack incAmount = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncAmount = incAmount.getItemMeta();
                metaIncAmount.displayName(Component.text("Increase xp", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_give_xp");
                metaIncAmount.lore(incLore);
                incAmount.setItemMeta(metaIncAmount);
                inv.setItem(11, incAmount);

                ItemStack displayAmount = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayAmount = displayAmount.getItemMeta();
                metaDisplayAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayAmount.displayName(Component.text("Xp to give: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayAmount.setItemMeta(metaDisplayAmount);
                inv.setItem(20, displayAmount);

                ItemStack decAmount = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecAmount = decAmount.getItemMeta();
                metaDecAmount.displayName(Component.text("Decrease xp", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_give_xp");
                metaDecAmount.lore(decLore);
                decAmount.setItemMeta(metaDecAmount);
                inv.setItem(29, decAmount);

                ItemStack levels = new ItemStack(action.getArgs().get(1).equalsIgnoreCase("true") ? Material.GREEN_CANDLE : Material.RED_CANDLE);
                ItemMeta levelsMeta = levels.getItemMeta();
                levelsMeta.displayName(action.getArgs().get(1).equalsIgnoreCase("true") ? Component.text("Levels", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) : Component.text("Points", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                levelsMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_give_levels");
                levelsMeta.lore(List.of(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

                levels.setItemMeta(levelsMeta);
                inv.setItem(24, levels);

            }
            case "REMOVE_EXP" -> {

                /*
                 # # # # # # # # #
                 # # # I # # # # #
                 # # # O # O # # #
                 # # # D # # # # #
                 # # # # # # # # #

                 ^^ Example inventory layout.
                 - O = display
                 - # = empty space
                */

                ItemStack incAmount = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncAmount = incAmount.getItemMeta();
                metaIncAmount.displayName(Component.text("Increase xp", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_remove_xp");
                metaIncAmount.lore(incLore);
                incAmount.setItemMeta(metaIncAmount);
                inv.setItem(12, incAmount);

                ItemStack displayAmount = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayAmount = displayAmount.getItemMeta();
                metaDisplayAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayAmount.displayName(Component.text("Xp to remove: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayAmount.setItemMeta(metaDisplayAmount);
                inv.setItem(21, displayAmount);

                ItemStack decAmount = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecAmount = decAmount.getItemMeta();
                metaDecAmount.displayName(Component.text("Decrease xp", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecAmount.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_remove_xp");
                metaDecAmount.lore(decLore);
                decAmount.setItemMeta(metaDecAmount);
                inv.setItem(30, decAmount);

                ItemStack levels = new ItemStack(action.getArgs().get(1).equalsIgnoreCase("true") ? Material.GREEN_CANDLE : Material.RED_CANDLE);
                ItemMeta levelsMeta = levels.getItemMeta();
                levelsMeta.displayName(action.getArgs().get(1).equalsIgnoreCase("true") ? Component.text("Levels", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) : Component.text("Points", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                levelsMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_remove_levels");
                levelsMeta.lore(List.of(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));


                levels.setItemMeta(levelsMeta);
                inv.setItem(23, levels);

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
                titleMeta.displayName(plugin.getMiniMessage().deserialize(String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_message");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
            case "PLAY_SOUND" -> {
                /* 4 buttons.
                2 "displays" pitch, volume,
                2 buttons increment,
                2 decrement.
                1 button to edit sound Enter it with a command for auto complete.

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


                List<Component> smallIncLore = new ArrayList<>();
                smallIncLore.add(Component.text("CLick to add .1", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                ItemStack incPitch = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncPitch = incPitch.getItemMeta();
                metaIncPitch.displayName(Component.text("Increase pitch.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_sound_pitch");
                metaIncPitch.lore(smallIncLore);
                incPitch.setItemMeta(metaIncPitch);
                inv.setItem(10, incPitch);

                ItemStack incVolume = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncVolume = incVolume.getItemMeta();
                metaIncVolume.displayName(Component.text("Increase volume", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncVolume.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_volume");
                metaIncVolume.lore(smallIncLore);
                incVolume.setItemMeta(metaIncVolume);
                inv.setItem(12, incVolume);

                //decrements

                List<Component> smallDecLore = new ArrayList<>();
                smallDecLore.add(Component.text("Left CLick to remove .1", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                ItemStack decPitch = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecPitch = decPitch.getItemMeta();
                metaDecPitch.displayName(Component.text("Decrease pitch", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_sound_pitch");
                metaDecPitch.lore(smallDecLore);
                decPitch.setItemMeta(metaDecPitch);
                inv.setItem(28, decPitch);

                ItemStack decVolume = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecVolume = decVolume.getItemMeta();
                metaDecVolume.displayName(Component.text("Decrease volume", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecVolume.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_volume");
                metaDecVolume.lore(smallDecLore);
                decVolume.setItemMeta(metaDecVolume);
                inv.setItem(30, decVolume);


                // Displays



                ItemStack displayPitch = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayPitch = displayPitch.getItemMeta();
                metaDisplayPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayPitch.displayName(Component.text("Pitch: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayPitch.setItemMeta(metaDisplayPitch);
                inv.setItem(19, displayPitch);

                ItemStack displayVolume = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayVolume = displayVolume.getItemMeta();
                metaDisplayVolume.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayVolume.displayName(Component.text("Volume: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayVolume.setItemMeta(metaDisplayVolume);
                inv.setItem(21, displayVolume);

                ItemStack sound = new ItemStack(Material.BELL);
                ItemMeta metaDisplaySound = sound.getItemMeta();
                metaDisplaySound.displayName(Component.text("Sound: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDisplaySound.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_sound");
                sound.setItemMeta(metaDisplaySound);
                inv.setItem(24, sound);

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
                titleMeta.displayName(plugin.getMiniMessage().deserialize(String.join(" ", args)));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_actionbar");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
            case "TELEPORT" -> {

                 /* 6 buttons.
                 3 "displays" F
                 ade in,
                 stay,
                 fade out.

                 3 buttons increment,
                 3 decrement.
                 1 button to edit YAW

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

                ItemStack incX = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncX = incX.getItemMeta();
                metaIncX.displayName(Component.text("Increase X coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_x");
                metaIncX.lore(incLore);
                incX.setItemMeta(metaIncX);
                inv.setItem(10, incX);

                ItemStack incY = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncY = incY.getItemMeta();
                metaIncY.displayName(Component.text("Increase Y coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncY.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_y");
                metaIncY.lore(incLore);
                incY.setItemMeta(metaIncY);
                inv.setItem(11, incY);

                ItemStack incZ = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncZ = incZ.getItemMeta();
                metaIncZ.displayName(Component.text("Increase Z coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncZ.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_z");
                metaIncZ.lore(incLore);
                incZ.setItemMeta(metaIncZ);
                inv.setItem(12, incZ);

                ItemStack incYaw = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncYaw = incYaw.getItemMeta();
                metaIncYaw.displayName(Component.text("Increase yaw", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncYaw.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_yaw");
                metaIncYaw.lore(incLore);
                incYaw.setItemMeta(metaIncYaw);
                inv.setItem(16, incYaw);

                ItemStack incPitch = new ItemStack(Material.LIME_DYE);
                ItemMeta metaIncPitch = incPitch.getItemMeta();
                metaIncPitch.displayName(Component.text("Increase pitch", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaIncPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "increment_pitch");
                metaIncPitch.lore(incLore);
                incPitch.setItemMeta(metaIncPitch);
                inv.setItem(14, incPitch);


                //decrements

                ItemStack decX = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecX = decX.getItemMeta();
                metaDecX.displayName(Component.text("Decrease X coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_z");
                metaDecX.lore(decLore);
                decX.setItemMeta(metaDecX);
                inv.setItem(28, decX);

                ItemStack decY = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecY = decY.getItemMeta();
                metaDecY.displayName(Component.text("Decrease Y coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecY.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_y");
                metaDecY.lore(decLore);
                decY.setItemMeta(metaDecY);
                inv.setItem(29, decY);

                ItemStack decZ = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecOut = decZ.getItemMeta();
                metaDecOut.displayName(Component.text("Decrease Z coordinate", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecOut.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_z");
                metaDecOut.lore(decLore);
                decZ.setItemMeta(metaDecOut);
                inv.setItem(30, decZ);

                ItemStack decYaw = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecYaw = decYaw.getItemMeta();
                metaDecYaw.displayName(Component.text("Decrease yaw", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecYaw.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_yaw");
                metaDecYaw.lore(decLore);
                decYaw.setItemMeta(metaDecYaw);
                inv.setItem(34, decYaw);

                ItemStack decPitch = new ItemStack(Material.RED_DYE);
                ItemMeta metaDecPitch = decPitch.getItemMeta();
                metaDecPitch.displayName(Component.text("Decrease pitch", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                metaDecPitch.getPersistentDataContainer().set(key, PersistentDataType.STRING, "decrement_pitch");
                metaDecPitch.lore(decLore);
                decPitch.setItemMeta(metaDecPitch);
                inv.setItem(32, decPitch);

                // Displays

                List<Component> displayLore = new ArrayList<>();
                displayLore.add(Component.text("In blocks", NamedTextColor.DARK_GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));


                ItemStack displayX = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayX = displayX.getItemMeta();
                metaDisplayX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayX.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayX.displayName(Component.text("X: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayX.lore(displayLore);
                displayX.setItemMeta(metaDisplayX);
                inv.setItem(19, displayX);

                ItemStack displayY = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayY = displayY.getItemMeta();
                metaDisplayY.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayY.displayName(Component.text("Y: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayY.lore(displayLore);
                displayY.setItemMeta(metaDisplayY);
                inv.setItem(20, displayY);

                ItemStack displayZ = new ItemStack(Material.CLOCK);
                ItemMeta metaDisplayZ = displayZ.getItemMeta();
                metaDisplayZ.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                metaDisplayZ.displayName(Component.text("Z: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                metaDisplayZ.lore(displayLore);
                displayZ.setItemMeta(metaDisplayZ);
                inv.setItem(21, displayZ);

                ItemStack displayYaw = new ItemStack(Material.COMPASS);
                ItemMeta displayYawMeta = displayYaw.getItemMeta();
                displayYawMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                displayYawMeta.displayName(Component.text("Yaw: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                args.remove(0);
                displayYaw.setItemMeta(displayYawMeta);
                inv.setItem(25, displayYaw);

                ItemStack displayPitch = new ItemStack(Material.COMPASS);
                ItemMeta displayPitchMeta = displayPitch.getItemMeta();
                displayPitchMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "display");
                displayPitchMeta.displayName(Component.text("Pitch: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
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
                titleMeta.displayName(Component.text("Selected server: " + String.join(" ", args), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                titleMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "edit_server");
                message.setItemMeta(titleMeta);
                inv.setItem(22, message);
            }
            case "TOGGLE_FOLLOWING" -> {
                npc.addAction(action);
                return getActionMenu();
            }
        }

        return inv;
    }

    /**
     * <p> Gets the menu to customize an action
     * </p>
     * @param conditional The Conditional to customize
     * @return The Inventory representing the conditional to customize
     */
    public Inventory getConditionalCustomizerMenu(Conditional conditional) {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 27, Component.text("  Edit Action Conditional", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "CustomizeConditionalButton");


        // Go back to actions menu
        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.displayName(Component.text("Go Back", NamedTextColor.GOLD));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(18, goBack);

        // Confirm the action creation
        ItemStack confirm = new ItemStack(Material.LILY_PAD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "confirm");
        confirmMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        confirm.setItemMeta(confirmMeta);
        inv.setItem(22, confirm);
        
        switch (conditional.getType()) {
            case NUMERIC -> {
                 /* 1 button to edit message

                 # # # # # # # # #
                 # # C # T # S # #
                 # # # # # # # # #

                 - T = target value
                 - C = comparator
                 - S = Select Statistic
                 - # = empty space
                */
                ItemStack selectComparator = new ItemStack(Material.COMPARATOR);
                ItemMeta meta = selectComparator.getItemMeta();
                List<Component> lore = new ArrayList<>();
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "toggle_comparator");
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if(conditional.getComparator() != c)
                        lore.add(Component.text(c.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    else
                        lore.add(Component.text("▸ " + c.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                meta.displayName(Component.text("Comparator", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                inv.setItem(11, selectComparator);
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "select_target_value");
                targetMeta.displayName(Component.text("Select Target Value", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("The target value is '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(conditional.getTarget(), NamedTextColor.AQUA).append(Component.text("'", NamedTextColor.YELLOW))));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                inv.setItem(13, targetValue);
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
                statisticMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "select_statistic");
                for (Conditional.Value v : Conditional.Value.values()) {
                    if (!v.isLogical()) {
                        if (conditional.getValue() != v)
                            lore.add(Component.text(v.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        else
                            lore.add(Component.text("▸ " + v.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    }
                }
                statisticMeta.displayName(Component.text("Statistic", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                statisticMeta.lore(lore);
                statistic.setItemMeta(statisticMeta);
                inv.setItem(15, statistic);
            }
            case LOGICAL -> {
                 /* 1 button to edit message

                 # # # # # # # # #
                 # # C # T # S # #
                 # # # # # # # # #

                 - T = target value
                 - C = comparator
                 - S = Select Statistic
                 - # = empty space
                */
                ItemStack selectComparator = new ItemStack(Material.COMPARATOR);
                ItemMeta meta = selectComparator.getItemMeta();
                List<Component> lore = new ArrayList<>();
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "toggle_comparator");
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if (c.isStrictlyLogical()) {
                        if(conditional.getComparator() != c)
                            lore.add(Component.text(c.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        else
                            lore.add(Component.text("▸ " + c.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    }
                }
                meta.displayName(Component.text("Comparator", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                inv.setItem(11, selectComparator);
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "select_target_value");
                targetMeta.displayName(Component.text("Select Target Value", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("The target value is '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(conditional.getTarget(), NamedTextColor.AQUA).append(Component.text("'", NamedTextColor.YELLOW))));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                inv.setItem(13, targetValue);
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
                statisticMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "select_statistic");
                for (Conditional.Value v : Conditional.Value.values()) {
                    if (v.isLogical()) {
                        if (conditional.getValue() != v)
                            lore.add(Component.text(v.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        else
                            lore.add(Component.text("▸ " + v.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    }
                }
                statisticMeta.displayName(Component.text("Statistic", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                statisticMeta.lore(lore);
                statistic.setItemMeta(statisticMeta);
                inv.setItem(15, statistic);
            }
        }
        return inv;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     * @return The Inventory representing the new Action menu
     */
    public Inventory getNewActionMenu() {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 36, Component.text("       New NPC Action", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "NewActionButton");

        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.displayName(Component.text("Go Back", NamedTextColor.GOLD));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(27, goBack);

        // make and add the npc action types.

        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();


        item.setType(Material.OAK_SIGN);
        meta.displayName(Component.text("Display Title", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "DISPLAY_TITLE");
        lore.add(Component.text("Displays a title for the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.PAPER);
        meta.displayName(Component.text("Send Message", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_MESSAGE");
        lore.add(Component.text("Sends the player a message.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.BELL);
        meta.displayName(Component.text("Play Sound", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "PLAY_SOUND");
        lore.add(Component.text("Plays a sound for the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.ANVIL);
        meta.displayName(Component.text("Run Command", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "RUN_COMMAND");
        lore.add(Component.text("Runs a command as the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.IRON_INGOT);
        meta.displayName(Component.text("Send Actionbar", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ACTION_BAR");
        lore.add(Component.text("Sends the player an actionbar.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.ENDER_PEARL);
        meta.displayName(Component.text("Teleport Player", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "TELEPORT");
        lore.add(Component.text("Teleports a player upon interacting.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.GRASS_BLOCK);
        meta.displayName(Component.text("Send To Bungeecord/Velocity Server", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "SEND_TO_SERVER");
        lore.add(Component.text("Sends a player to a bungeecord/velocity", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(Component.text("server upon interacting.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.LEAD);
        meta.displayName(Component.text("Start/Stop Following", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "TOGGLE_FOLLOWING");
        lore.add(Component.text("Toggles whether or not the ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(Component.text("NPC follows this player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.EXPERIENCE_BOTTLE);
        meta.displayName(Component.text("Give Exp", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "GIVE_EXP");
        lore.add(Component.text("Gives the player exp.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.GLASS_BOTTLE);
        meta.displayName(Component.text("Remove Exp", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "REMOVE_EXP");
        lore.add(Component.text("Removes exp from the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.BREWING_STAND);
        meta.displayName(Component.text("Give Effect", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "ADD_EFFECT");
        lore.add(Component.text("Gives an effect to the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.MILK_BUCKET);
        meta.displayName(Component.text("Remove Effect", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "REMOVE_EFFECT");
        lore.add(Component.text("Removes an effect from the player.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        return inv;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     * @return The Inventory representing the new Action menu
     */
    public Inventory getNewConditionMenu() {
        Inventory inv = plugin.getMenuUtils().addBorder(Bukkit.createInventory(null, 27, Component.text("   New Action Condition", NamedTextColor.BLACK, TextDecoration.BOLD)));
        NamespacedKey key = new NamespacedKey(plugin, "NewConditionButton");

        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "go_back");
        goBackMeta.displayName(Component.text("Go Back", NamedTextColor.GOLD));
        goBack.setItemMeta(goBackMeta);
        inv.setItem(18, goBack);

        // make and add the npc action types.

        ItemStack item = new ItemStack(Material.BEDROCK);
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = new ArrayList<>();


        item.setType(Material.POPPED_CHORUS_FRUIT);
        meta.displayName(Component.text("Numeric Condition", NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "NUMERIC_CONDITION");
        lore.add(Component.text("Compares numbers.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        item.setType(Material.COMPARATOR);
        meta.displayName(Component.text("Logical Condition", NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "LOGICAL_CONDITION");
        lore.add(Component.text("Compares things with numbered options", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        meta.lore(lore);
        item.setItemMeta(meta);
        lore.clear();
        inv.addItem(item);

        return inv;
    }
    /**
     * <p> Gets the NPC object associated with the Menus
     * </p>
     * @return The npc
     */
    public InternalNPC getNpc() {
        return this.npc;
    }
}
