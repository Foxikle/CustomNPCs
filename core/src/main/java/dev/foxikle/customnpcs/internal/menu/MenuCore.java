package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.actions.conditions.LogicalConditional;
import dev.foxikle.customnpcs.actions.conditions.NumericConditional;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Utils;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.runnables.*;
import me.flame.menus.builders.items.ItemBuilder;
import me.flame.menus.menu.Menu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bukkit.Material.*;

/**
 * Handles menu creation
 */
public class MenuCore {

    private static final List<Field> fields = Arrays.stream(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();
    private final InternalNpc npc;
    private final CustomNPCs plugin;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    /**
     * <p> The constructor to make a menu factory
     * </p>
     *
     * @param npc    The NPC to edit
     * @param plugin The instance of the Main class
     */
    public MenuCore(InternalNpc npc, CustomNPCs plugin) {
        this.npc = npc;
        this.plugin = plugin;
    }

    /**
     * <p> Gets the main menu
     * </p>
     *
     * @return The Menu representing the Main NPC menu
     */
    public Menu getMainMenu() {
        List<Component> lore = new ArrayList<>();
        Menu menu = Menu.builder().title("       Create a New NPC").rows(5).addAllModifiers().normal();

        ItemStack nametag = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = nametag.getItemMeta();
        nameMeta.displayName(Component.text("Change Name", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(Component.text("The current name is ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(plugin.getMiniMessage().deserialize(npc.getSettings().getName())));
        nameMeta.lore(lore);
        nametag.setItemMeta(nameMeta);

        ItemStack equipment = new ItemStack(Material.ARMOR_STAND);
        ItemMeta handMeta = equipment.getItemMeta();
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

        ItemStack directionItem = new ItemStack(Material.COMPASS);
        ItemMeta positionMeta = directionItem.getItemMeta();
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
        directionItem.setItemMeta(positionMeta);

        ItemStack resilientItem = new ItemStack(Material.BELL);
        ItemMeta resilientMeta = resilientItem.getItemMeta();
        lore.clear();
        lore.add(npc.getSettings().isResilient() ? Component.text("RESILIENT").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD) : Component.text("NOT RESILIENT").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        resilientMeta.lore(lore);
        resilientMeta.displayName(Component.text("Change resilience", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        resilientItem.setItemMeta(resilientMeta);

        ItemStack confirmButton = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("CONFIRM", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        confirmButton.setItemMeta(confirmMeta);

        ItemStack interactableButton;
        if (npc.getSettings().isInteractable()) {
            interactableButton = new ItemStack(Material.OAK_SAPLING);

            ItemStack actionsButton = new ItemStack(Material.RECOVERY_COMPASS);
            ItemMeta actionsButtonMeta = actionsButton.getItemMeta();
            actionsButtonMeta.displayName(Component.text("Change actions", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.clear();
            lore.add(Component.text("The actions performed when ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            lore.add(Component.text("interacting with the npc. ").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW));
            actionsButtonMeta.lore(lore);
            actionsButtonMeta.lore();
            actionsButton.setItemMeta(actionsButtonMeta);
            menu.setItem(34, ItemBuilder.of(actionsButton).buildItem((player, event) -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                event.setCancelled(true);
                getActionMenu().open(player);

            }));
        } else {
            interactableButton = new ItemStack(Material.DEAD_BUSH);
        }
        ItemMeta clickableMeta = interactableButton.getItemMeta();
        clickableMeta.displayName(Component.text("Change interactability", NamedTextColor.DARK_GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.clear();
        lore.add(npc.getSettings().isInteractable() ? Component.text("INTERACTABLE").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD) : Component.text("NOT INTERACTABLE").decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        clickableMeta.lore(lore);
        interactableButton.setItemMeta(clickableMeta);

        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("CANCEL", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        cancelButton.setItemMeta(cancelMeta);
        menu.setItem(13, ItemBuilder.of(plugin.getMenuUtils().getSkinIcon(new NamespacedKey(plugin, "nothing_lol_this_should_be_changed_tbh"), "", "Change Skin", ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, "Changes the NPC's skin", "The current skin is " + npc.getSettings().getSkinName(), "Click to change!", "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9").getItemStack()).buildItem((player, event) -> {
            getSkinMenu().open(player);

        }));
        menu.setItem(16, ItemBuilder.of(nametag).buildItem((player, event) -> {

            plugin.nameWaiting.add(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.sendMessage("§aType the NPC name the chat.");
            new NameRunnable(player, plugin).runTaskTimer(plugin, 1, 15);
            player.closeInventory();
            event.setCancelled(true);

        }));
        menu.setItem(10, ItemBuilder.of(directionItem).buildItem((player, event) -> {

            if (event.isLeftClick()) {
                switch ((int) dir) {
                    case 180 -> npc.getSettings().setDirection(-135.0);
                    case -135 -> npc.getSettings().setDirection(-90.0);
                    case -90 -> npc.getSettings().setDirection(-45.0);
                    case -45 -> npc.getSettings().setDirection(0.0);
                    case 0 -> npc.getSettings().setDirection(45.0);
                    case 45 -> npc.getSettings().setDirection(90.0);
                    case 90 -> npc.getSettings().setDirection(135.0);
                    case 135 -> npc.getSettings().setDirection(player.getLocation().getYaw());
                    default -> npc.getSettings().setDirection(180);
                }
            } else if (event.isRightClick()) {
                switch ((int) dir) {
                    case 180 -> npc.getSettings().setDirection(player.getLocation().getYaw());
                    case -135 -> npc.getSettings().setDirection(180);
                    case -90 -> npc.getSettings().setDirection(-135);
                    case -45 -> npc.getSettings().setDirection(-90);
                    case 0 -> npc.getSettings().setDirection(-45.0);
                    case 45 -> npc.getSettings().setDirection(0.0);
                    case 90 -> npc.getSettings().setDirection(45);
                    case 135 -> npc.getSettings().setDirection(90);
                    default -> npc.getSettings().setDirection(135);
                }
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getMainMenu().open(player);

        }));
        menu.setItem(22, ItemBuilder.of(resilientItem).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            player.sendMessage("§bThe NPC is now " + (npc.getSettings().isResilient() ? "§c§lNOT RESILIENT" : "§a§lRESILIENT"));
            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            getMainMenu().open(player);

        }));
        menu.setItem(25, ItemBuilder.of(interactableButton).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            player.sendMessage("§bThe NPC is now " + (npc.getSettings().isInteractable() ? "§c§lNOT INTERACTABLE" : "§a§lINTERACTABLE"));
            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            getMainMenu().open(player);

        }));
        menu.setItem(31, ItemBuilder.of(confirmButton).buildItem((player, event) -> {

            Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 3);
            Bukkit.getScheduler().runTaskLater(plugin, npc::createNPC, 1);
            player.sendMessage(npc.getSettings().isResilient() ? "§aReslilient NPC created!" : "§aTemporary NPC created!");
            player.closeInventory();
            event.setCancelled(true);

        }));
        menu.setItem(36, ItemBuilder.of(cancelButton).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
            player.sendMessage("§cNPC aborted.");
            player.closeInventory();
            event.setCancelled(true);

        }));
        menu.setItem(28, ItemBuilder.of(SPYGLASS).setName("§bToggle Vision Mode").setLore(npc.getSettings().isTunnelvision() ? "§a§lTUNNEL Visioned" : "§c§lNORMAL Visioned").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            getMainMenu().open(player);

        }));
        menu.setItem(19, ItemBuilder.of(equipment).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            getEquipmentMenu().open(player);

        }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu displaying the NPC's current armor
     * </p>
     *
     * @return The Menu representing the Armor menu
     */
    public Menu getEquipmentMenu() {
        ItemStack helm = npc.getEquipment().getHead();
        ItemStack cp = npc.getEquipment().getChest();
        ItemStack legs = npc.getEquipment().getLegs();
        ItemStack boots = npc.getEquipment().getBoots();
        ItemStack hand = npc.getEquipment().getHand();
        ItemStack offhand = npc.getEquipment().getOffhand();
        Menu menu = Menu.builder().rows(6).addModifier(me.flame.menus.modifiers.Modifier.DISABLE_ITEM_REMOVAL).title("      Edit NPC Equipment").normal();
        menu.getFiller().fill(MenuItems.MENU_GLASS);

        if (helm.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Helmet Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a helmet to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(13, ItemBuilder.of(item).buildItem((player, event) -> {
                if (event.getCursor().getType() == AIR)

                    npc.getEquipment().setHead(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set helmet slot to " + npc.getEquipment().getHead().getType());
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = helm.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(helm.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a helmet to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            helm.setItemMeta(meta);
            menu.setItem(13, ItemBuilder.of(helm).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setHead(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset helmet slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) npc.getEquipment().setHead(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set helmet slot to " + npc.getEquipment().getHead().getType());
                    getEquipmentMenu().open(player);
                }

            }));
        }
        if (cp.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Chestplate Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a chestplate to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(22, ItemBuilder.of(item).buildItem((player, event) -> {
                if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set chestplate slot to " + npc.getEquipment().getChest().getType());
                } else {
                    if (event.getCursor().getType() == AIR) event.setCancelled(true);
                    player.sendMessage("§cThat is not a chestplate!");
                }
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = cp.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(cp.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a chestplate to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            cp.setItemMeta(meta);
            menu.setItem(22, ItemBuilder.of(cp).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setChest(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset chestplate slot ");
                    getEquipmentMenu().open(player);
                } else if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set chestplate slot to " + npc.getEquipment().getChest().getType());
                } else {
                    if (event.getCursor().getType() == AIR) return;
                }
                event.setCancelled(true);
                player.sendMessage("§cThat is not a chestplate!");
                getEquipmentMenu().open(player);

            }));
        }
        if (legs.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Leggings Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of leggings", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(31, ItemBuilder.of(item).buildItem((player, event) -> {

                if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set leggings slot to " + npc.getEquipment().getLegs().getType());

                } else {
                    if (event.getCursor().getType() == AIR) event.setCancelled(true);
                    player.sendMessage("§cThat is not a pair of leggings!");
                }
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = legs.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(legs.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of leggings", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            legs.setItemMeta(meta);
            menu.setItem(31, ItemBuilder.of(legs).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setLegs(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset leggings slot ");
                    getEquipmentMenu().open(player);
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set leggings slot to " + npc.getEquipment().getLegs().getType());
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) event.setCancelled(true);
                    player.sendMessage("§cThat is not a pair of leggings!");
                }

            }));
        }
        if (boots.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.values());
            meta.displayName(Component.text("Empty Boots Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of boots to ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(40, ItemBuilder.of(item).buildItem((player, event) -> {

                if (event.getCursor().getType().name().contains("BOOTS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set boots slot to " + npc.getEquipment().getBoots().getType());
                } else {
                    if (event.getCursor().getType() == AIR) event.setCancelled(true);
                    player.sendMessage("§cThat is not a pair of boots!");
                }
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = boots.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(boots.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("a pair of boots to ", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            boots.setItemMeta(meta);
            menu.setItem(40, ItemBuilder.of(boots).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setBoots(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset boots slot ");
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set boots slot to " + npc.getEquipment().getBoots().getType());
                } else {
                    if (event.getCursor().getType() == AIR)

                        event.setCancelled(true);
                    player.sendMessage("§cThat is not a pair of boots!");
                }
                getEquipmentMenu().open(player);

            }));
        }
        if (hand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Hand Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(23, ItemBuilder.of(item).buildItem((player, event) -> {

                if (event.getCursor().getType() == AIR) npc.getEquipment().setHand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set hand slot to " + npc.getEquipment().getHand().getType());
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = hand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(hand.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            hand.setItemMeta(meta);
            menu.setItem(23, ItemBuilder.of(hand).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setHand(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset hand slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) npc.getEquipment().setHand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getHand().getType());
                    getEquipmentMenu().open(player);
                }

            }));
        }
        if (offhand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text("Empty Offhand Slot", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(21, ItemBuilder.of(item).buildItem((player, event) -> {

                if (event.getCursor().getType() == AIR) npc.getEquipment().setOffhand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getOffhand().getType());
                getEquipmentMenu().open(player);

            }));
        } else {
            ItemMeta meta = offhand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(offhand.getType().toString(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Click this slot with", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("an item to change.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            lore.add(Component.text("Rick click to remove", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            offhand.setItemMeta(meta);

            menu.setItem(21, ItemBuilder.of(offhand).buildItem((player, event) -> {

                if (event.isRightClick()) {
                    npc.getEquipment().setOffhand(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset offhand slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) npc.getEquipment().setOffhand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getOffhand().getType());
                    getEquipmentMenu().open(player);
                }

            }));
        }

        menu.setItem(8, ItemBuilder.of(ARMOR_STAND)
                .setName("§bImport Player Armor")
                .setLore("§eImports your current armor to the NPC")
                .buildItem((player, event) -> {
            npc.getEquipment().importFromEntityEquipment(player.getEquipment());
            getEquipmentMenu().open(player);
        }));

        menu.setItem(49, ItemBuilder.of(BARRIER).setName("§cClose").buildItem((player, event) -> {
            getMainMenu().open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }));
        return menu;
    }

    /**
     * <p>Gets the menu displaying all curent actions
     * </p>
     *
     * @return The Inventory representing the Actions menu
     */
    public Menu getActionMenu() {
        Menu menu = Menu.builder().rows(6).title("          Edit NPC Actions").addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);
        List<Action> actions = npc.getActions();

        for (Action action : actions) {
            ItemStack item = new ItemStack(Material.BEDROCK);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            List<String> args = action.getArgsCopy();
            if (action.getActionType() != ActionType.TOGGLE_FOLLOWING)
                lore.add(Component.text("Delay (ticks): " + action.getDelay()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN));
            lore.add(Component.empty());
            switch (action.getActionType()) {
                case DISPLAY_TITLE -> {
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
                case SEND_MESSAGE -> {
                    item.setType(Material.PAPER);
                    meta.displayName(Component.text("Send Message", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current message is: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(String.join(" ", args))).append(Component.text("'", NamedTextColor.YELLOW)));
                }
                case PLAY_SOUND -> {
                    item.setType(Material.BELL);
                    meta.displayName(Component.text("Play Sound", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    double pitch = Double.parseDouble(args.get(0));
                    args.remove(0);
                    double volume = Double.parseDouble(args.get(0));
                    args.remove(0);
                    lore.add(Component.text("The current sound is: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Pitch: " + DECIMAL_FORMAT.format(pitch), NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Volume: " + DECIMAL_FORMAT.format(volume), NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case RUN_COMMAND -> {
                    item.setType(Material.ANVIL);
                    meta.displayName(Component.text("Run Command", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The command is: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case ACTION_BAR -> {
                    item.setType(Material.IRON_INGOT);
                    meta.displayName(Component.text("Send Actionbar", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current actionbar is: '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(plugin.getMiniMessage().deserialize(String.join(" ", args))).append(Component.text("'", NamedTextColor.YELLOW)));
                }
                case TELEPORT -> {
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
                case GIVE_EXP -> {
                    item.setType(Material.EXPERIENCE_BOTTLE);
                    meta.displayName(Component.text("Give Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current xp to give is: " + args.get(0) + " " + (args.get(1).equalsIgnoreCase("true") ? "levels" : "points"), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case REMOVE_EXP -> {
                    item.setType(Material.GLASS_BOTTLE);
                    meta.displayName(Component.text("Remove Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("The current xp to remove is: " + args.get(0) + " " + (args.get(1).equalsIgnoreCase("true") ? "levels" : "points"), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case ADD_EFFECT -> {
                    item.setType(Material.BREWING_STAND);
                    meta.displayName(Component.text("Give Effect", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Effect: '" + args.get(3) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Duration: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Amplifier: " + args.get(1), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Hide particles: " + args.get(2), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case REMOVE_EFFECT -> {
                    item.setType(Material.MILK_BUCKET);
                    meta.displayName(Component.text("Remove Experience", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Effect: '" + args.get(0) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case SEND_TO_SERVER -> {
                    item.setType(Material.GRASS_BLOCK);
                    meta.displayName(Component.text("Send To Bungeecord/Velocity Server", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    lore.add(Component.text("Server: '" + String.join(" ", args) + "'", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                case TOGGLE_FOLLOWING -> {
                    item.setType(Material.LEAD);
                    meta.displayName(Component.text("[WIP]", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(" Start / Stop Following", NamedTextColor.AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));
                }
            }
            lore.add(Component.empty());
            lore.add(Component.text("Right Click to remove.", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            if (action.getActionType().isEditable())
                lore.add(Component.text("Left Click to edit.", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.addItem(ItemBuilder.of(item).buildItem((player, event) -> {

                event.setCancelled(true);
                if (event.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    npc.removeAction(action);
                } else if (event.isLeftClick()) {
                    if (action.getActionType().isEditable()) {
                        plugin.editingActions.put(player, action.clone());
                        plugin.originalEditingActions.put(player, action);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        getActionCustomizerMenu(action).open(player);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        player.sendMessage("§cThis action cannot be edited!");
                    }
                }

            }));
        }

        // Close Button
        ItemStack close = new ItemStack(Material.ARROW);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("GO BACK", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        close.setItemMeta(closeMeta);
        menu.setItem(45, ItemBuilder.of(close).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getMainMenu().open(player);

        }));

        // Add New
        ItemStack newAction = new ItemStack(Material.LILY_PAD);
        ItemMeta actionMeta = newAction.getItemMeta();
        actionMeta.displayName(Component.text("New Action", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        newAction.setItemMeta(actionMeta);
        menu.addItem(ItemBuilder.of(newAction).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getNewActionMenu().open(player);

        }));
        return menu;
    }

    /**
     * Gets the menu to customize an action's conditions
     *
     * @param action the action whose conditions are to be customized
     * @return the inventory to be displayed
     */
    public Menu getConditionMenu(Action action) {
        Menu menu = Menu.builder().title("   Edit Action Conditionals").rows(4).addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);
        if (action.getConditionals() != null) {
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

                meta.lore(lore);
                item.setItemMeta(meta);
                menu.addItem(ItemBuilder.of(item).buildItem((player, event) -> {

                    if (event.isRightClick()) {
                        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                        action.removeConditional(c);
                        getConditionMenu(action).open(player);
                        event.setCancelled(true);
                    } else {
                        plugin.editingConditionals.put(player, c.clone());
                        plugin.originalEditingConditionals.put(player, c);
                        getConditionalCustomizerMenu(c).open(player);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }

                }));
            }
        }
        List<Component> lore = new ArrayList<>();

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("GO BACK", NamedTextColor.RED, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        closeMeta.lore(lore);
        close.setItemMeta(closeMeta);
        lore.clear();
        menu.setItem(31, ItemBuilder.of(close).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getActionCustomizerMenu(action).open(player);

        }));

        // Add New
        ItemStack newCondition = new ItemStack(Material.LILY_PAD);
        ItemMeta conditionMeta = newCondition.getItemMeta();
        conditionMeta.displayName(Component.text("New Condition", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        newCondition.setItemMeta(conditionMeta);
        lore.clear();
        menu.addItem(ItemBuilder.of(newCondition).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getNewConditionMenu().open(player);

        }));

        // Change Mode
        ItemStack changeMode = new ItemStack(action.getMode() == Conditional.SelectionMode.ALL ? Material.GREEN_CANDLE : Material.RED_CANDLE);
        ItemMeta changeModeMeta = changeMode.getItemMeta();
        changeModeMeta.displayName(Component.text("Change Mode", NamedTextColor.GREEN, TextDecoration.BOLD).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        lore.add(action.getMode() == Conditional.SelectionMode.ALL ? Component.text("Match ALL Conditions", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) : Component.text("Match ONE Condition", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        changeModeMeta.lore(lore);
        changeMode.setItemMeta(changeModeMeta);
        lore.clear();
        menu.setItem(35, ItemBuilder.of(changeMode).buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            action.setMode(action.getMode() == Conditional.SelectionMode.ALL ? Conditional.SelectionMode.ONE : Conditional.SelectionMode.ALL);
            getConditionMenu(action).open(player);

        }));
        return menu;
    }

    /**
     * <p>Gets the menu to customize an action
     * </p>
     *
     * @param action The Action to customize
     * @return The Inventory representing the action to customize
     */
    public Menu getActionCustomizerMenu(Action action) {
        Menu menu = Menu.builder().addAllModifiers().rows(5).title("       Edit NPC Action").normal();
        List<String> incLore = List.of("§8Left Click to add 1", "§8Right Click to add 5", "§8Shift + Right Click to add 20");
        List<String> decLore = List.of("§8Left Click to remove 1", "§8Right Click to remove 5", "§8Shift + Click to remove 20");

        menu.setItem(3, ItemBuilder.of(RED_DYE).setName("§eDecrement Delay").setLore(decLore).buildItem((player, event) -> {

            if (event.isLeftClick()) {
                if (!(action.getDelay() - 1 < 0)) {
                    action.setDelay(action.getDelay() - 1);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            } else if (event.isRightClick()) {
                if (!(action.getDelay() - 5 < 0)) {
                    action.setDelay(action.getDelay() - 5);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            } else if (event.isShiftClick()) {
                if (!(action.getDelay() - 20 < 0)) {
                    action.setDelay(action.getDelay() - 20);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            }
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getActionCustomizerMenu(action).open(player);

        }));
        menu.setItem(4, ItemBuilder.of(CLOCK).setName("§eDelay Ticks: " + action.getDelay()).buildItem());
        menu.setItem(5, ItemBuilder.of(LIME_DYE).setName("§eIncrement Delay").setLore(incLore).buildItem((player, event) -> {
            if (event.isLeftClick()) {
                action.setDelay(action.getDelay() + 1);
            } else if (event.isRightClick()) {
                action.setDelay(action.getDelay() + 5);
            } else if (event.isShiftClick()) {
                action.setDelay(action.getDelay() + 20);
            }
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getActionCustomizerMenu(action).open(player);

        }));
        menu.setItem(36, ItemBuilder.of(ARROW).setName("§6Go Back").buildItem((player, event) -> {
            getActionMenu().open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);

        }));
        menu.setItem(44, ItemBuilder.of(COMPARATOR).setName("§cEdit Conditions").buildItem((player, event) -> {
            getConditionMenu(action).open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);

        }));
        menu.setItem(40, ItemBuilder.of(LILY_PAD).setName("§aConfirm").buildItem((player, event) -> {

            if (plugin.originalEditingActions.get(player) != null)
                npc.removeAction(plugin.originalEditingActions.remove(player));
            npc.addAction(action);
            getActionMenu().open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);

        }));

        List<String> args = action.getArgsCopy();
        switch (action.getActionType()) {
            case RUN_COMMAND ->
                    menu.setItem(22, ItemBuilder.of(ANVIL).setName("§eClick to Edit Command").setLore("§e" + String.join(" ", args), "§eClick to change!").buildItem((player, event) -> {

                        player.closeInventory();
                        plugin.commandWaiting.add(player);
                        new CommandRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);

                    }));
            case DISPLAY_TITLE -> {
                // Increments
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease fade in duration").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease display duration").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(14, ItemBuilder.of(LIME_DYE).setName("§eIncrease fade out duration").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease fade in duration").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease display duration").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(32, ItemBuilder.of(RED_DYE).setName("§eDecrease fade out duration").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                // Displays
                String displayLore = "§8In ticks";
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eFade in: " + args.get(0)).setLore(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eDisplay time: " + args.get(1)).setLore(displayLore).buildItem());
                menu.setItem(23, ItemBuilder.of(CLOCK).setName("§eFade out: " + args.get(2)).setLore(displayLore).buildItem());
                menu.setItem(25, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).buildItem((player, event) -> {

                    player.closeInventory();
                    plugin.titleWaiting.add(player);
                    new TitleRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);

                }));
            }
            case ADD_EFFECT -> {
                // Increments
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease effect duration").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease effect amplifier").setLore(incLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 1) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 5) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 20) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease effect duration").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease effect amplifier").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == -1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 1) < -1) {
                            action.getArgs().set(1, String.valueOf(-1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == -1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 5) < -1) {
                            action.getArgs().set(1, String.valueOf(-1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == -1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 20) < -1) {
                            action.getArgs().set(1, String.valueOf(-1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                // Displays
                String displayLore = "§8In ticks";
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eDuration: " + args.get(0)).setLore(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eAmplifier: " + args.get(1)).setName(displayLore).buildItem());

                boolean particles = Boolean.parseBoolean(args.get(2));
                menu.setItem(23, ItemBuilder.of(particles ? GREEN_CANDLE : RED_CANDLE).setName("§eHide Particles: " + particles).buildItem((player, event) -> {
                    action.getArgs().set(2, String.valueOf(!particles));
                    getActionCustomizerMenu(action).open(player);

                }));


                List<String> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if (!Objects.equals(action.getArgs().get(3), field.getName())) lore.add("§a" + field.getName());
                    else lore.add("§3▸ " + field.getName());
                });
                menu.setItem(25, ItemBuilder.of(POTION).setName("§eEffect to give").setLore(lore).addAllItemFlags().buildItem((player, event) -> {
                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(3));
                    if (event.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(3, effects.get(index + 1));
                        } else {
                            action.getArgs().set(3, effects.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            action.getArgs().set(3, effects.get(effects.size() - 1));
                        } else {
                            action.getArgs().set(3, effects.get(index - 1));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));
            }
            case REMOVE_EFFECT -> {
                List<String> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if (!Objects.equals(action.getArgs().get(3), field.getName())) lore.add("§a" + field.getName());
                    else lore.add("§3▸ " + field.getName());
                });
                menu.setItem(25, ItemBuilder.of(POTION).setName("§eEffect to give").setLore(lore).addAllItemFlags().buildItem((player, event) -> {
                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(0));

                    if (event.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(0, effects.get(index + 1));
                        } else {
                            action.getArgs().set(0, effects.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            action.getArgs().set(0, effects.get(effects.size() - 1));
                        } else {
                            action.getArgs().set(0, effects.get(index - 1));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));
            }
            case GIVE_EXP -> {
                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§eIncrease xp").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eXp to give: " + args.get(0)).buildItem());
                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease xp").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                boolean levels = Boolean.parseBoolean(args.get(1));
                menu.setItem(24, ItemBuilder.of(levels ? GREEN_CANDLE : RED_CANDLE).setName("§eAwarding EXP " + (levels ? "Levels" : "Points")).setLore("§eClick to change!").buildItem((player, event) -> {
                    action.getArgs().set(1, String.valueOf(!levels));
                    getActionCustomizerMenu(action).open(player);

                }));
            }
            case REMOVE_EXP -> {
                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§eIncrease xp").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eXp to remove: " + args.get(0)).buildItem());
                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease xp").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == -1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < -1) {
                            action.getArgs().set(0, String.valueOf(-1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                boolean levels = Boolean.parseBoolean(args.get(1));
                menu.setItem(24, ItemBuilder.of(levels ? GREEN_CANDLE : RED_CANDLE).setName("§eRemoving EXP " + (levels ? "Levels" : "Points")).setLore("§eClick to change!").buildItem((player, event) -> {
                    action.getArgs().set(1, String.valueOf(!levels));
                    getActionCustomizerMenu(action).open(player);

                }));

            }
            case SEND_MESSAGE ->
                    menu.setItem(22, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).setLore("§eClick to change!").buildItem((player, event) -> {

                        player.closeInventory();
                        plugin.messageWaiting.add(player);
                        new MessageRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);

                    }));
            case PLAY_SOUND -> {

                String smallIncLore = "§eClick to add .1";
                String smallDecLore = "§eClick to remove .1";

                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease pitch.").setLore(smallIncLore).buildItem((player, event) -> {

                    action.getArgs().set(0, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(0)) + .1)));
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease Volume").setLore(smallIncLore).buildItem((player, event) -> {

                    action.getArgs().set(1, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(1)) + .1)));
                    getActionCustomizerMenu(action).open(player);

                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease pitch").setLore(smallDecLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Double.parseDouble(action.getArgs().get(0)) - .1 <= 0) {
                            player.sendMessage("§cThe pitch cannot be less than or equal 0!");
                        } else {
                            action.getArgs().set(0, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(0)) - .1)));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease volume").setLore(smallDecLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Double.parseDouble(action.getArgs().get(1)) - .1 <= 0) {
                            player.sendMessage("§cThe volume cannot be less than or equal 0!");
                        } else {
                            action.getArgs().set(1, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(1)) - .1)));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));


                // Displays
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§ePitch: " + args.get(0)).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eVolume: " + args.get(1)).buildItem());

                ItemStack sound = new ItemStack(Material.BELL);
                ItemMeta metaDisplaySound = sound.getItemMeta();
                metaDisplaySound.displayName(Component.text("Sound: " + args.get(0), NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                sound.setItemMeta(metaDisplaySound);
                menu.setItem(24, ItemBuilder.of(OAK_HANGING_SIGN).setName("§eSound: " + args.get(2)).setLore("", "§eClick to change!").buildItem((player, event) -> {

                    player.closeInventory();
                    plugin.soundWaiting.add(player);
                    new SoundRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);

                }));
            }
            case ACTION_BAR ->
                    menu.setItem(22, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).buildItem((player, event) -> {

                        player.closeInventory();
                        plugin.actionbarWaiting.add(player);
                        new ActionbarRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);

                    }));
            case TELEPORT -> {
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease X Coordinate").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§e Increase Y Coordinate").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease Z Coordinate").setLore(incLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(16, ItemBuilder.of(LIME_DYE).setName("§eIncrease Yaw").setLore(incLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 1) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 5) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 20) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) + 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(14, ItemBuilder.of(LIME_DYE).setName("§eIncrease Pitch").setLore(incLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 1) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 5) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 20) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease X Coordinate").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease Y Coordinate").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(20, ItemBuilder.of(RED_DYE).setName("§eDecrease Z Coordinate").setLore(decLore).buildItem((player, event) -> {
                    if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                    } else if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(34, ItemBuilder.of(RED_DYE).setName("§eDecrease Yaw").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 1) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 5) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 20) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                menu.setItem(32, ItemBuilder.of(RED_DYE).setName("§eDecrease Pitch").setLore(decLore).buildItem((player, event) -> {

                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 1) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 5) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 20) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);

                }));

                // Displays
                String displayLore = "In blocks";

                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eX: §b" + args.get(0)).setName(displayLore).buildItem());
                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eY: §b" + args.get(1)).setName(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eZ: §b" + args.get(2)).setName(displayLore).buildItem());

                menu.setItem(25, ItemBuilder.of(COMPASS).setName("§eYaw: §b" + args.get(3)).setName(displayLore).buildItem());
                menu.setItem(23, ItemBuilder.of(COMPASS).setName("§ePitch: §b" + args.get(4)).setName(displayLore).buildItem());
            }
            case SEND_TO_SERVER ->
                    menu.setItem(22, ItemBuilder.of(GRASS_BLOCK).setName("§Server: " + String.join(" ", args)).buildItem((player, event) -> {

                        player.closeInventory();
                        plugin.serverWaiting.add(player);
                        new ServerRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);

                    }));
            case TOGGLE_FOLLOWING -> {
                npc.addAction(action);
                return getActionMenu();
            }
        }
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p> Gets the menu to customize an action
     * </p>
     *
     * @param conditional The Conditional to customize
     * @return The Inventory representing the conditional to customize
     */
    public Menu getConditionalCustomizerMenu(Conditional conditional) {
        Menu menu = Menu.builder().rows(3).title("   Edit Action Conditional").addAllModifiers().normal();

        // Go back to actions menu
        ItemStack goBack = new ItemStack(Material.ARROW);
        ItemMeta goBackMeta = goBack.getItemMeta();
        goBackMeta.displayName(Component.text("Go Back", NamedTextColor.GOLD));
        goBack.setItemMeta(goBackMeta);
        menu.setItem(18, ItemBuilder.of(goBack).buildItem((player, event) -> {
            getNewConditionMenu().open(player);

        }));

        ItemStack confirm = new ItemStack(Material.LILY_PAD);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm", NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        confirm.setItemMeta(confirmMeta);
        menu.setItem(22, ItemBuilder.of(confirm).buildItem((player, event) -> {

            Action action = plugin.editingActions.get(player);
            event.setCancelled(true);
            if (plugin.originalEditingConditionals.get(player) != null)
                action.removeConditional(plugin.originalEditingConditionals.remove(player));
            action.addConditional(conditional);
            getConditionMenu(action).open(player);

        }));

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
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if (conditional.getComparator() != c)
                        lore.add(Component.text(c.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    else
                        lore.add(Component.text("▸ " + c.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                meta.displayName(Component.text("Comparator", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                menu.setItem(11, ItemBuilder.of(selectComparator).buildItem((player, event) -> {
                    List<Conditional.Comparator> comparators = List.of(Conditional.Comparator.values());
                    int index = comparators.indexOf(conditional.getComparator());
                    if (event.isLeftClick()) {
                        if (comparators.size() > (index + 1)) {
                            conditional.setComparator(comparators.get(index + 1));
                        } else {
                            conditional.setComparator(comparators.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setComparator(comparators.get(comparators.size() - 1));
                        } else {
                            conditional.setComparator(comparators.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);

                }));
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.displayName(Component.text("Select Target Value", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("The target value is '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(conditional.getTarget(), NamedTextColor.AQUA).append(Component.text("'", NamedTextColor.YELLOW))));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                menu.setItem(13, ItemBuilder.of(targetValue).buildItem((player, event) -> {

                    player.closeInventory();
                    plugin.targetWaiting.add(player);
                    new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);

                }));
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
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
                menu.setItem(15, ItemBuilder.of(statistic).buildItem((player, event) -> {
                    List<Conditional.Value> statistics = new ArrayList<>();
                    for (Conditional.Value value : Conditional.Value.values()) {
                        if (!value.isLogical()) statistics.add(value);
                    }

                    int index = statistics.indexOf(conditional.getValue());
                    if (event.isLeftClick()) {
                        if (statistics.size() > (index + 1)) {
                            conditional.setValue(statistics.get(index + 1));
                        } else {
                            conditional.setValue(statistics.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setValue(statistics.get(statistics.size() - 1));
                        } else {
                            conditional.setValue(statistics.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);

                }));
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
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if (c.isStrictlyLogical()) {
                        if (conditional.getComparator() != c)
                            lore.add(Component.text(c.name(), NamedTextColor.GREEN).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        else
                            lore.add(Component.text("▸ " + c.name(), NamedTextColor.DARK_AQUA).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    }
                }
                meta.displayName(Component.text("Comparator", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                menu.setItem(11, ItemBuilder.of(selectComparator).buildItem((player, event) -> {
                    List<Conditional.Comparator> comparators = new ArrayList<>();
                    for (Conditional.Comparator value : Conditional.Comparator.values()) {
                        if (value.isStrictlyLogical()) comparators.add(value);
                    }


                    int index = comparators.indexOf(conditional.getComparator());
                    if (event.isLeftClick()) {
                        if (comparators.size() > (index + 1)) {
                            conditional.setComparator(comparators.get(index + 1));
                        } else {
                            conditional.setComparator(comparators.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setComparator(comparators.get(comparators.size() - 1));
                        } else {
                            conditional.setComparator(comparators.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);

                }));
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.displayName(Component.text("Select Target Value", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                lore.add(Component.text("The target value is '", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE).append(Component.text(conditional.getTarget(), NamedTextColor.AQUA).append(Component.text("'", NamedTextColor.YELLOW))));
                lore.add(Component.text("Click to change!", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                menu.setItem(13, ItemBuilder.of(targetValue).buildItem((player, event) -> {

                    player.closeInventory();
                    plugin.targetWaiting.add(player);
                    new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);

                }));
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
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
                menu.setItem(15, ItemBuilder.of(statistic).buildItem((player, event) -> {
                    List<Conditional.Value> statistics = new ArrayList<>();

                    for (Conditional.Value value : Conditional.Value.values()) {
                        if (value.isLogical()) statistics.add(value);
                    }

                    int index = statistics.indexOf(conditional.getValue());
                    if (event.isLeftClick()) {
                        if (statistics.size() > (index + 1)) {
                            conditional.setValue(statistics.get(index + 1));
                        } else {
                            conditional.setValue(statistics.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setValue(statistics.get(statistics.size() - 1));
                        } else {
                            conditional.setValue(statistics.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);

                }));
            }
        }
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     *
     * @return The Inventory representing the new Action menu
     */
    public Menu getNewActionMenu() {
        Menu menu = Menu.builder().rows(4).addAllModifiers().title("          New NPC Action").normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);

        menu.setItem(27, ItemBuilder.of(ARROW).setName("§6Go Back").buildItem((player, event) -> {
            getActionMenu().open(player);

        }));

        menu.addItem(ItemBuilder.of(OAK_SIGN).setName("§bDisplay Title").setLore("§eDisplays a title for the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.DISPLAY_TITLE, Utils.list("10", "20", "10", "title!"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(PAPER).setName("§bSend Message").setLore("§eSends the player a message.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.SEND_MESSAGE, Utils.list("message", "to", "be", "sent"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(BELL).setName("§bPlay Sound").setLore("§ePlays a sound for the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.PLAY_SOUND, Utils.list("1", "1", Sound.UI_BUTTON_CLICK.name()), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(ANVIL).setName("§bRun Command").setLore("§eRuns a command as the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.RUN_COMMAND, Utils.list("command", "to", "be", "run"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(IRON_INGOT).setName("§bSend Actionbar").setLore("§eSends the player an actionbar.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.ACTION_BAR, Utils.list("actionbar", "to", "be", "sent"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(ENDER_PEARL).setName("§bTeleport Player").setLore("§eTeleports a player upon interacting.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.TELEPORT, Utils.list("0", "0", "0", "0", "0"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(GRASS_BLOCK).setName("§bSend To Bungeecord/Velocity Server").setLore("§eSends a player to a Bungeecord/Velocity", "§eserver upon interacting.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.SEND_TO_SERVER, Utils.list("server", "name"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(LEAD).setName("§bStart/Stop Following").setLore("§eToggles whether or not the", "§eNPC follows this player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.TOGGLE_FOLLOWING, Collections.singletonList(npc.getUniqueID().toString()), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(EXPERIENCE_BOTTLE).setName("§bGive Exp").setLore("§eGives the player exp.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.GIVE_EXP, Utils.list("0", "true"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(GLASS_BOTTLE).setName("§bRemove Exp").setLore("§eRemoves exp from the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.REMOVE_EXP, Utils.list("0", "true"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(BREWING_STAND).setName("§bGive Effect").setLore("§eGives an effect to the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.ADD_EFFECT, Utils.list("1", "1", "true", "SPEED"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(MILK_BUCKET).setName("§bRemove Effect").setLore("§eRemoves an effect from the player.").buildItem((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.REMOVE_EFFECT, Utils.list("SPEED"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().canDubplicate()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Component.text("This NPC already has this action!", NamedTextColor.RED));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     *
     * @return The Inventory representing the new Action menu
     */
    public Menu getNewConditionMenu() {
        Menu menu = Menu.builder().title("       New Action Condition").rows(3).addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);

        menu.setItem(18, ItemBuilder.of(ARROW).setName("§6Go Back").buildItem((player, event) -> {
            getConditionMenu(plugin.editingActions.get(player));

        }));

        menu.addItem(ItemBuilder.of(POPPED_CHORUS_FRUIT).setName("§3Numeric Condition").setLore("§eCompares numbers.").buildItem((player, event) -> {


            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = new NumericConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.EXP_LEVELS, 0.0);
            plugin.editingConditionals.put(player, conditional);
            getConditionalCustomizerMenu(conditional).open(player);


        }));

        menu.addItem(ItemBuilder.of(COMPARATOR).setName("§3Logical Condition").setLore("§eCompares things with", "§enumbered options.").buildItem((player, event) -> {


            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = new LogicalConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.GAMEMODE, "SURVIVAL");
            plugin.editingConditionals.put(player, conditional);
            getConditionalCustomizerMenu(conditional).open(player);


        }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    public Menu getSkinMenu() {
        Menu menu = Menu.builder().title("     Edit NPC Skin").rows(3).addAllModifiers().normal();

        menu.setItem(18, ItemBuilder.of(ARROW).setName("§6Go Back").setLore("§eGo back to the main menu").buildItem((player, event) -> {

            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getMainMenu().open(player);

        }));

        menu.setItem(11, ItemBuilder.of(ANVIL).setName("§bImport from Player").setLore("§eFetches a player's skin by name").buildItem((player, event) -> {

            player.closeInventory();
            plugin.playernameWating.add(player);
            new PlayerNameRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);

        }));

        menu.setItem(13, ItemBuilder.of(ARMOR_STAND).setName("§bBrowse Skin Catalogue").setLore("§eUse a preset skin").buildItem((player, event) -> {
            plugin.skinCatalogue.open(player);

        }));

        menu.setItem(15, ItemBuilder.of(WRITABLE_BOOK).setName("§bImport from URL").setLore("§eFetches a skin from a URL").buildItem((player, event) -> {

            player.closeInventory();
            plugin.urlWaiting.add(player);
            new UrlRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);

        }));

        menu.getFiller().fill(MenuItems.MENU_GLASS);

        return menu;
    }

    /**
     * <p> Gets the NPC object associated with the Menus
     * </p>
     *
     * @return The npc
     */
    public InternalNpc getNpc() {
        return this.npc;
    }
}
