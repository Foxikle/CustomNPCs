package dev.foxikle.customnpcs.internal.listeners;

import dev.foxikle.customnpcs.api.Action;
import dev.foxikle.customnpcs.api.ActionType;
import dev.foxikle.customnpcs.internal.*;
import dev.foxikle.customnpcs.api.conditions.Conditional;
import dev.foxikle.customnpcs.api.conditions.LogicalConditional;
import dev.foxikle.customnpcs.api.conditions.NumericConditional;
import dev.foxikle.customnpcs.internal.menu.MenuCore;
import dev.foxikle.customnpcs.internal.runnables.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.bukkit.Material.*;

/**
 * Handlers for clicks in Menus
 */
public class NPCMenuListeners implements Listener {
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;

    /**
     * The map of MenuCores
     */
    private Map<Player, MenuCore> map;

    /**
     * Creates the handler for NPC menu clicks
     * @param plugin the main class instance
     */
    public NPCMenuListeners(CustomNPCs plugin){
        this.plugin = plugin;
        map = plugin.menuCores;
    }

    /**
     * <p>The generic handler npc menu clicks
     * </p>
     * @param e The callback event object
     * @since 1.3-pre5
     */
    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "MenuButtonTag");
        ItemStack item = e.getCurrentItem();
        PersistentDataContainer persistentDataContainer = item.getItemMeta().getPersistentDataContainer();
        Player player = (Player) e.getWhoClicked();
        MenuCore mc = map.get(player);
        if(mc == null) return;
        InternalNpc npc = mc.getNpc();
        if(npc.getActions() == null) return;
        if (persistentDataContainer.get(key, PersistentDataType.STRING) != null) {
            if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("NameTag")) {
                plugin.nameWaiting.add(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.sendMessage(ChatColor.GREEN + "Type the NPC name the chat.");
                new NameRunnable(player, plugin).runTaskTimer(plugin, 1, 15);
                player.closeInventory();
                e.setCancelled(true);
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("direction")) {
                double dir = npc.getFacingDirection();
                if (e.getAction() == InventoryAction.PICKUP_ALL) {
                    switch ((int) dir) {
                        case 180 -> {
                            npc.setDirection(-135.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -135 -> {
                            npc.setDirection(-90.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -90 -> {
                            npc.setDirection(-45.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -45 -> {
                            npc.setDirection(0.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 0 -> {
                            npc.setDirection(45.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 45 -> {
                            npc.setDirection(90.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 90 -> {
                            npc.setDirection(135.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 135 -> {
                            npc.setDirection(player.getLocation().getYaw());
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        default -> {
                            npc.setDirection(180);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                    }
                } else if (e.getAction() == InventoryAction.PICKUP_HALF) {
                    switch ((int) dir) {
                        case 180 -> {
                            npc.setDirection(player.getLocation().getYaw());
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -135 -> {
                            npc.setDirection(180);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -90 -> {
                            npc.setDirection(-135);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case -45 -> {
                            npc.setDirection(-90);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 0 -> {
                            npc.setDirection(-45.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 45 -> {
                            npc.setDirection(0.0);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 90 -> {
                            npc.setDirection(45);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        case 135 -> {
                            npc.setDirection(90);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                        default -> {
                            npc.setDirection(135);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            player.openInventory(mc.getMainMenu());
                        }
                    }
                }
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("resilience")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                e.setCancelled(true);
                if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.RED + "" + ChatColor.BOLD + "NOT RESILIENT")) {
                    npc.setResilient(true);
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.GREEN + "" + ChatColor.BOLD + "RESILIENT");
                    player.openInventory(mc.getMainMenu());
                } else if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GREEN + "" + ChatColor.BOLD + "RESILIENT")) {
                    npc.setResilient(false);
                    player.openInventory(mc.getMainMenu());
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.RED + "" + ChatColor.BOLD + "NOT RESILIENT");
                }
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("clickable")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                e.setCancelled(true);
                if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.RED + "" + ChatColor.BOLD + "NOT INTERACTABLE")) {
                    npc.setClickable(true);
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.GREEN + "" + ChatColor.BOLD + "INTERACTABLE");
                    player.openInventory(mc.getMainMenu());
                } else if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GREEN + "" + ChatColor.BOLD + "INTERACTABLE")) {
                    npc.setClickable(false);
                    player.openInventory(mc.getMainMenu());
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.RED + "" + ChatColor.BOLD + "NOT INTERACTABLE");
                }
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("changeSkin")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                e.setCancelled(true);
                player.openInventory(plugin.catalogueInventories.get(0));

            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("equipment")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                e.setCancelled(true);
                player.openInventory(mc.getArmorMenu());
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("Confirm")) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), 1);
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 3);
                Bukkit.getScheduler().runTaskLater(plugin, npc::createNPC, 1);
                player.sendMessage(npc.isResilient() ? ChatColor.GREEN + "Reslilient NPC created!" : ChatColor.GREEN + "Temporary NPC created!");
                player.closeInventory();
                e.setCancelled(true);
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("Cancel")) {
                player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
                player.sendMessage(ChatColor.RED + "NPC aborted");
                player.closeInventory();
                e.setCancelled(true);
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("actions")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(mc.getActionMenu());
                e.setCancelled(true);
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SkinButton"))) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            e.setCancelled(true);
            String name = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "SkinButton"), PersistentDataType.STRING);
            npc.setValue(plugin.getMenuUtils().getValue(name));
            npc.setSignature(plugin.getMenuUtils().getSignature(name));
            npc.setSkinName(name);
            player.sendMessage(ChatColor.GREEN + "Skin changed to " + ChatColor.BOLD + name);
            plugin.pages.put(player, 0);
            player.closeInventory();
            player.openInventory(mc.getMainMenu());
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NoClickey"))) {
            e.setCancelled(true);
            String tag = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "NoClickey"), PersistentDataType.STRING);
            switch (tag) {
                case "prev" -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1, 1);
                    player.openInventory(plugin.catalogueInventories.get(plugin.getPage(player) - 1));
                    plugin.setPage(player, plugin.getPage(player) - 1);
                }
                case "next" -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1, 1);
                    player.openInventory(plugin.catalogueInventories.get(plugin.getPage(player) + 1));
                    plugin.setPage(player, plugin.getPage(player) + 1);
                }
                case "close" -> {
                    player.openInventory(mc.getMainMenu());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "EquipmentInv"))) {
            String button = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "EquipmentInv"), PersistentDataType.STRING);
            switch (button) {
                case "helm" -> {
                    if (!e.getCursor().getType().isAir()) {
                        npc.setHeadItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getHeadItem().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setHeadItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset helmet slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "cp" -> {
                    if (!e.getCursor().getType().isAir()) {
                        Material type = e.getCursor().getType();
                        if (type != LEATHER_CHESTPLATE && type != CHAINMAIL_CHESTPLATE && type != IRON_CHESTPLATE && type != GOLDEN_CHESTPLATE && type != DIAMOND_CHESTPLATE && type != NETHERITE_CHESTPLATE) {
                            return;
                        }
                        npc.setChestItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getChestItem().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setChestItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset chestplate slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "legs" -> {
                    if (!e.getCursor().getType().isAir()) {
                        Material type = e.getCursor().getType();
                        if (type != LEATHER_LEGGINGS && type != CHAINMAIL_LEGGINGS && type != IRON_LEGGINGS && type != GOLDEN_LEGGINGS && type != DIAMOND_LEGGINGS && type != NETHERITE_LEGGINGS)
                            return;
                        npc.setLegsItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getLegsItem().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setLegsItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset legs slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "boots" -> {
                    if (!e.getCursor().getType().isAir()) {
                        Material type = e.getCursor().getType();
                        if (type != LEATHER_BOOTS && type != CHAINMAIL_BOOTS && type != IRON_BOOTS && type != GOLDEN_BOOTS && type != DIAMOND_BOOTS && type != NETHERITE_BOOTS)
                            return;
                        npc.setBootsItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getBootsItem().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setBootsItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset boots slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "hand" -> {
                    if (!e.getCursor().getType().isAir()) {
                        npc.setHandItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getHandItem().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setHandItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset hand slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "offhand" -> {
                    if (!e.getCursor().getType().isAir()) {
                        npc.setOffhandItem(e.getCursor().clone());
                        e.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(ChatColor.GREEN + "Successfully set helmet slot to " + npc.getItemInOffhand().getType());
                        player.openInventory(mc.getArmorMenu());
                    } else {
                        if (e.getAction() == InventoryAction.PICKUP_HALF) {
                            npc.setOffhandItem(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(ChatColor.RED + "Successfully reset offhand slot ");
                            player.openInventory(mc.getArmorMenu());
                        }
                    }
                }
                case "close" -> {
                    player.openInventory(mc.getMainMenu());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }
            }
            e.setCancelled(true);
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "ActionInv"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "ActionInv"), PersistentDataType.STRING);
            if (itemData.equals("new_action")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(mc.getNewActionMenu());
            } else if (itemData.equals("go_back")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(mc.getMainMenu());
            } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SerializedAction"))){
                Action action = Action.of(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "SerializedAction"), PersistentDataType.STRING));
                if (e.getAction() == InventoryAction.PICKUP_HALF) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    npc.removeAction(action);
                    player.openInventory(mc.getActionMenu());
                    e.setCancelled(true);
                } else {
                    plugin.editingActions.put(player, action);
                    plugin.originalEditingActions.put(player, action.toJson());
                    player.openInventory(mc.getActionCustomizerMenu(action));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "ConditionInv"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "ConditionInv"), PersistentDataType.STRING);
            if (itemData.equals("new_condition")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(mc.getNewConditionMenu());
            } else if (itemData.equals("change_mode")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                Action action = plugin.editingActions.get(player);
                action.setMode(action.getMode() == Conditional.SelectionMode.ALL ? Conditional.SelectionMode.ONE : Conditional.SelectionMode.ALL);
                player.openInventory(mc.getConditionMenu(action));
            } else if (itemData.equals("go_back")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                player.openInventory(mc.getActionCustomizerMenu(plugin.editingActions.get(player)));
            } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SerializedCondition"))){
                Conditional conditional = Conditional.of(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "SerializedCondition"), PersistentDataType.STRING));
                if (e.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    Action action = plugin.editingActions.get(player);
                    action.removeConditional(conditional);
                    player.openInventory(mc.getConditionMenu(action));
                    e.setCancelled(true);
                } else {
                    plugin.editingConditionals.put(player, conditional);
                    plugin.originalEditingConditionals.put(player, conditional.toJson());
                    player.openInventory(mc.getConditionalCustomizerMenu(conditional));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NewActionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "NewActionButton"), PersistentDataType.STRING);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = null;

            switch (itemData) {
                case "RUN_COMMAND" -> action = new Action(ActionType.RUN_COMMAND, new ArrayList<>(Arrays.asList("command", "to", "be", "run")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "DISPLAY_TITLE" -> action = new Action(ActionType.DISPLAY_TITLE, new ArrayList<>(Arrays.asList("10", "20", "10", "title!")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "SEND_MESSAGE" -> action = new Action(ActionType.SEND_MESSAGE, new ArrayList<>(Arrays.asList("message", "to", "be", "sent")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "PLAY_SOUND" -> action = new Action(ActionType.PLAY_SOUND, new ArrayList<>(Arrays.asList("1", "1", Sound.UI_BUTTON_CLICK.name())), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "ACTION_BAR" -> action = new Action(ActionType.ACTION_BAR, new ArrayList<>(Arrays.asList("actionbar", "to", "be", "sent")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "TELEPORT" -> action = new Action(ActionType.TELEPORT, new ArrayList<>(Arrays.asList("0", "0", "0", "0", "0")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "SEND_TO_SERVER" -> action = new Action(ActionType.SEND_TO_SERVER, new ArrayList<>(Arrays.asList("server", "name")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "TOGGLE_FOLLOWING" -> action = new Action(ActionType.TOGGLE_FOLLOWING, new ArrayList<>(Arrays.asList(npc.getUUID().toString())), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "GIVE_EXP" -> action = new Action(ActionType.GIVE_EXP, new ArrayList<>(Arrays.asList("0", "true")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "REMOVE_EXP" -> action = new Action(ActionType.REMOVE_EXP, new ArrayList<>(Arrays.asList("0", "true")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "ADD_EFFECT" -> action = new Action(ActionType.ADD_EFFECT, new ArrayList<>(Arrays.asList("1", "1", "true", "SPEED")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "REMOVE_EFFECT" -> action = new Action(ActionType.REMOVE_EFFECT, new ArrayList<>(Arrays.asList("SPEED")), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
                case "go_back" -> player.openInventory(mc.getActionMenu());
            }
            if(action != null) {
                plugin.editingActions.put(player, action);
                player.openInventory(mc.getActionCustomizerMenu(action));
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NewConditionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "NewConditionButton"), PersistentDataType.STRING);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = null;

            switch (itemData) {
                case "NUMERIC_CONDITION" -> conditional = new NumericConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.EXP_LEVELS, 0.0);
                case "LOGICAL_CONDITION" -> conditional = new LogicalConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.GAMEMODE, "SURVIVAL");
                case "go_back" -> player.openInventory(mc.getConditionMenu(plugin.editingActions.get(player)));
            }
            if(conditional != null) {
                plugin.editingConditionals.put(player, conditional);
                player.openInventory(mc.getConditionalCustomizerMenu(conditional));
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "CustomizeConditionalButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "CustomizeConditionalButton"), PersistentDataType.STRING);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = plugin.editingConditionals.get(player);
            Action action = plugin.editingActions.get(player);

            switch (itemData) {
                case "select_target_value" -> {
                    player.closeInventory();
                    plugin.targetWaiting.add(player);
                    new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    e.setCancelled(true);
                    conditional = new NumericConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.EXP_LEVELS, 0.0);
                    return;
                }
                case "toggle_comparator" -> {

                        List<Conditional.Comparator> comparators = new ArrayList<>();
                        if(conditional.getType() == Conditional.Type.LOGICAL) {
                            for (Conditional.Comparator value : Conditional.Comparator.values()) {
                                if (value.isStrictlyLogical()) comparators.add(value);
                            }
                        } else {
                            comparators.addAll(Arrays.asList(Conditional.Comparator.values()));
                        }

                        int index = comparators.indexOf(conditional.getComparator());
                        if (e.isLeftClick()) {
                            if (comparators.size() > (index + 1)) {
                                conditional.setComparator(comparators.get(index+1));
                            } else {
                                conditional.setComparator(comparators.get(0));
                            }
                        } else if (e.isRightClick()) {
                            if(index == 0) {
                                conditional.setComparator(comparators.get(comparators.size()-1));
                            } else {
                                conditional.setComparator(comparators.get(index-1));
                            }
                        }
                        player.openInventory(mc.getConditionalCustomizerMenu(conditional));
                }
                case "select_statistic" -> {

                    List<Conditional.Value> statistics = new ArrayList<>();
                    if(conditional.getType() == Conditional.Type.LOGICAL) {
                        for (Conditional.Value value : Conditional.Value.values()) {
                            if (value.isLogical()) statistics.add(value);
                        }
                    } else {
                        for (Conditional.Value value : Conditional.Value.values()) {
                            if (!value.isLogical()) statistics.add(value);
                        }
                    }

                    int index = statistics.indexOf(conditional.getValue());
                    if (e.isLeftClick()) {
                        if (statistics.size() > (index + 1)) {
                            conditional.setValue(statistics.get(index+1));
                        } else {
                            conditional.setValue(statistics.get(0));
                        }
                    } else if (e.isRightClick()) {
                        if(index == 0) {
                            conditional.setValue(statistics.get(statistics.size()-1));
                        } else {
                            conditional.setValue(statistics.get(index-1));
                        }
                    }
                    player.openInventory(mc.getConditionalCustomizerMenu(conditional));
                }
                case "confirm" -> {
                    e.setCancelled(true);
                    if(plugin.originalEditingConditionals.get(player) != null)
                        action.removeConditional(Conditional.of(plugin.originalEditingConditionals.remove(player)));
                    action.addConditional(conditional);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(mc.getConditionMenu(action)), 1);
                }
                case "go_back" -> player.openInventory(mc.getNewConditionMenu());
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "CustomizeActionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "CustomizeActionButton"), PersistentDataType.STRING);
            Action action = plugin.editingActions.get(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            switch (Objects.requireNonNull(itemData)) {
                // RUN_COMMAND
                case "edit_command" -> {
                    player.closeInventory();
                    plugin.commandWaiting.add(player);
                    new CommandRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    e.setCancelled(true);
                    return;
                }
                // DISPLAY_TITLE
                case "increment_in" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                        }
                    }
                }
                case "increment_stay" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                        }
                    }
                }
                case "increment_out" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                        }
                    }
                }
                case "decrement_in" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    }
                }

                case "decrement_stay" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                        }
                    }
                }
                case "decrement_out" -> {
                    if(action.getSubCommand().equalsIgnoreCase("DISPLAY_TITLE")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                        }
                    }
                }
                case "edit_title" -> {
                    player.closeInventory();
                    plugin.titleWaiting.add(player);
                    new TitleRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    e.setCancelled(true);
                    return;
                }

                //SEND_MESSAGE
                case "edit_message" ->{
                    player.closeInventory();
                    plugin.messageWaiting.add(player);
                    new MessageRunnable(player, plugin).runTaskTimer(plugin, 0,10);
                    e.setCancelled(true);
                    return;
                }

                // PLAY_SOUND  (pitch / volume / sound)
                case "edit_sound" -> {
                    player.closeInventory();
                    plugin.soundWaiting.add(player);
                    new SoundRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    e.setCancelled(true);
                    return;
                }

                case "increment_sound_pitch" -> {
                    if (action.getSubCommand().equalsIgnoreCase("PLAY_SOUND")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) {
                            if (Double.parseDouble(action.getArgs().get(0))+.1 > 1) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be greater than 1!");
                            } else {
                                action.getArgs().set(0, String.valueOf(Double.parseDouble(action.getArgs().get(0)) + .1));
                            }
                        }
                    }
                }
                case "increment_volume" -> {
                    if (action.getSubCommand().equalsIgnoreCase("PLAY_SOUND")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) {
                            if (Double.parseDouble(action.getArgs().get(1))+.1 > 1) {
                                player.sendMessage(ChatColor.RED + "The volume cannot be greater than 1!");
                            } else {
                                action.getArgs().set(1, String.valueOf(Double.parseDouble(action.getArgs().get(1)) + .1));
                            }
                        }
                    }
                }
                case "decrement_sound_pitch" -> {
                    if (action.getSubCommand().equalsIgnoreCase("PLAY_SOUND")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) {
                            if (Double.parseDouble(action.getArgs().get(0))-.1 <= .1) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be less than or equal 0!");
                            } else {
                                action.getArgs().set(0, String.valueOf(Double.parseDouble(action.getArgs().get(0)) - .1));
                            }
                        }
                    }
                }
                case "decrement_volume" -> {
                    if (action.getSubCommand().equalsIgnoreCase("PLAY_SOUND")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) {
                            if (Double.parseDouble(action.getArgs().get(1))-.1 <= 0) {
                                player.sendMessage(ChatColor.RED + "The volume cannot be less than or equal 0!");
                            } else {
                                action.getArgs().set(1, String.valueOf(Double.parseDouble(action.getArgs().get(1)) - .1));
                            }
                        }
                    }
                }

                // ACTION_BAR
                case "edit_actionbar" -> {
                    player.closeInventory();
                    plugin.actionbarWaiting.add(player);
                    new ActionbarRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    e.setCancelled(true);
                    return;
                }

                // TELEPORT
                case "increment_x" -> {
                    if(action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                        }
                    }
                }
                case "increment_y" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                        }
                    }
                }
                case "increment_z" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                        }
                    }
                }
                case "increment_yaw" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(3)) == 180) {
                                player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                            } else if ((Integer.parseInt(action.getArgs().get(3)) + 1) > 180) {
                                action.getArgs().set(3, String.valueOf(180));
                            } else {
                                action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(3)) == 180) {
                                player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                            } else if ((Integer.parseInt(action.getArgs().get(3)) + 5) > 180) {
                                action.getArgs().set(3, String.valueOf(180));
                            } else {
                                action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(3)) == 180) {
                                player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                            } else if ((Integer.parseInt(action.getArgs().get(3)) + 20) > 180) {
                                action.getArgs().set(3, String.valueOf(180));
                            } else {
                                action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) + 20));
                            }
                        }
                    }
                }
                case "increment_pitch" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(4)) == 90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be greater than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) + 1) > 90) {
                                action.getArgs().set(4, String.valueOf(90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(4)) == 90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be greater than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) + 5) > 90) {
                                action.getArgs().set(4, String.valueOf(90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(4)) == 90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be greater than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) + 20) > 90) {
                                action.getArgs().set(4, String.valueOf(90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 20));
                            }
                        }
                    }
                }
                case "decrement_x" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    }
                }
                case "decrement_y" -> {
                    if(action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                        }
                    }
                }
                case "decrement_z" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                        }
                    }
                }
                case "decrement_yaw" -> {
                    if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                        if(Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 1) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                        }
                    } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                        if(Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 5) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                        }
                    } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                        if(Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage(ChatColor.RED + "The yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 20) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) - 20));
                        }
                    }
                }
                case "decrement_pitch" -> {
                    if (action.getSubCommand().equalsIgnoreCase("TELEPORT")) {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(4)) == -90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be less than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) - 1) < -90) {
                                action.getArgs().set(4, String.valueOf(-90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(4)) == -90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be less than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) - 5) < -90) {
                                action.getArgs().set(4, String.valueOf(-90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(4)) == -90) {
                                player.sendMessage(ChatColor.RED + "The pitch cannot be less than 90!");
                            } else if ((Integer.parseInt(action.getArgs().get(4)) - 20) < -90) {
                                action.getArgs().set(4, String.valueOf(-90));
                            } else {
                                action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 20));
                            }
                        }
                    }
                }

                // SEND_TO_SERVER
                case "edit_server" -> {
                    player.closeInventory();
                    plugin.serverWaiting.add(player);
                    new ServerRunnable(player, plugin).runTaskTimer(plugin, 0,10);
                    e.setCancelled(true);
                    return;
                }

                // ADD_EFFECT
                case "decrement_duration" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The duration cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The duration cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The duration cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                            }
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "decrement_amplifier" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(1)) == -1) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) - 1) < -1) {
                                action.getArgs().set(1, String.valueOf(-1));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(1)) == -1) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) - 5) < -1) {
                                action.getArgs().set(1, String.valueOf(-1));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(1)) == -1) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) - 20) < -1) {
                                action.getArgs().set(1, String.valueOf(-1));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                            }
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "increment_duration" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                        }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "increment_amplifier" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(1)) == 255) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be greater than 255!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) + 1) > 255) {
                                action.getArgs().set(1, String.valueOf(255));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(1)) == 255) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be greater than 255!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) + 5) > 255) {
                                action.getArgs().set(1, String.valueOf(255));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(1)) == 255) {
                                player.sendMessage(ChatColor.RED + "The amplifier cannot be greater than 255!");
                            } else if ((Integer.parseInt(action.getArgs().get(1)) + 20) > 255) {
                                action.getArgs().set(1, String.valueOf(255));
                            } else {
                                action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                            }
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "edit_add_effect" -> {
                    List<Field> fields = Arrays.stream(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).collect(toList());                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(3));
                    if (e.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(3, effects.get(index+1));
                        } else {
                            action.getArgs().set(3, effects.get(0));
                        }
                    } else if (e.isRightClick()) {
                        if(index == 0) {
                            action.getArgs().set(3, effects.get(effects.size()-1));
                        } else {
                            action.getArgs().set(3, effects.get(index-1));
                        }
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "toggle_hide_particles" -> {
                    boolean bool = Boolean.parseBoolean(action.getArgs().get(2));
                    action.getArgs().set(2, String.valueOf(!bool));
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }

                //REMOVE_EFFECT
                case "edit_remove_effect" -> {
                    List<Field> fields = Arrays.stream(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).collect(toList());                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(0));
                    if (e.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(0, effects.get(index+1));
                        } else {
                            action.getArgs().set(0, effects.get(0));
                        }
                    } else if (e.isRightClick()) {
                        if(index == 0) {
                            action.getArgs().set(0, effects.get(effects.size()-1));
                        } else {
                            action.getArgs().set(0, effects.get(index-1));
                        }
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }

                // GIVE_EXP

                case "increment_give_xp", "increment_remove_xp" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "decrement_give_xp", "decrement_remove_xp" -> {
                        if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The xp cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                            }
                        } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The xp cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                            }
                        } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click
                            if(Integer.parseInt(action.getArgs().get(0)) == -1) {
                                player.sendMessage(ChatColor.RED + "The xp cannot be less than 1!");
                            } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < -1) {
                                action.getArgs().set(0, String.valueOf(-1));
                            } else {
                                action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                            }
                        }
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }
                case "edit_give_levels", "edit_remove_levels" -> {
                    boolean bool = Boolean.parseBoolean(action.getArgs().get(1));
                    action.getArgs().set(1, String.valueOf(!bool));
                    player.openInventory(mc.getActionCustomizerMenu(action));
                }

                // standard controls
                case "decrement_delay" -> {
                    if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click (1)
                        if(!(action.getDelay() - 1 < 0)){
                            action.setDelay(action.getDelay()-1);
                        } else {
                            player.sendMessage(ChatColor.RED + "The delay cannot be negative!");
                        }
                    } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click (5)
                        if(!(action.getDelay() - 5 < 0)){
                            action.setDelay(action.getDelay()-5);
                        } else {
                            player.sendMessage(ChatColor.RED + "The delay cannot be negative!");
                        }
                    } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click (20)
                        if(!(action.getDelay() - 20 < 0)){
                            action.setDelay(action.getDelay()-20);
                        } else {
                            player.sendMessage(ChatColor.RED + "The delay cannot be negative!");
                        }
                    }
                }
                case "increment_delay" -> {
                    if (e.getAction() == InventoryAction.PICKUP_ALL) { // Left click (1)
                        action.setDelay(action.getDelay() + 1);
                    } else if (e.getAction() == InventoryAction.PICKUP_HALF) { // Right Click (5)
                        action.setDelay(action.getDelay() + 5);
                    } else if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) { // Shift Click (20)
                        action.setDelay(action.getDelay() + 20);
                    }
                }
                case "go_back" -> Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(mc.getActionMenu()), 1);
                case "edit_conditionals" -> Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(mc.getConditionMenu(action)), 1);
                case "confirm" -> {
                    if(plugin.originalEditingActions.get(player) != null)
                        npc.removeAction(Action.of(plugin.originalEditingActions.remove(player)));
                    npc.addAction(action);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(mc.getActionMenu()), 1);
                }
            }
            e.setCancelled(true);
            player.openInventory(mc.getActionCustomizerMenu(action));
        }
    }
}

