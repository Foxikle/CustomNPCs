package dev.foxikle.customnpcs.listeners;

import dev.foxikle.customnpcs.Action;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import dev.foxikle.customnpcs.runnables.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.*;

public class NPCMenuListeners implements Listener {

    Map<Player, MenuCore> map = CustomNPCs.getInstance().menuCores;

    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "MenuButtonTag");
        ItemStack item = e.getCurrentItem();
        PersistentDataContainer tagContainer = item.getItemMeta().getPersistentDataContainer();
        Player player = (Player) e.getWhoClicked();
        MenuCore mc = map.get(player);
        if(mc == null) return;
        NPC npc = mc.getNpc();
        if (tagContainer.get(key, PersistentDataType.STRING) != null) {
            if (tagContainer.get(key, PersistentDataType.STRING).equals("NameTag")) {
                CustomNPCs.getInstance().nameWaiting.add(player);
                player.sendMessage(ChatColor.GREEN + "Type the NPC name the chat.");
                new NameRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 1, 15);
                player.closeInventory();
                e.setCancelled(true);
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("direction")) {
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
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("resilience")) {
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
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("clickable")) {
                e.setCancelled(true);
                if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.RED + "" + ChatColor.BOLD + "NOT CLICKABLE")) {
                    npc.setClickable(true);
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.GREEN + "" + ChatColor.BOLD + "CLICKABLE");
                    player.openInventory(mc.getMainMenu());
                } else if (e.getCurrentItem().getItemMeta().getLore().contains(ChatColor.GREEN + "" + ChatColor.BOLD + "CLICKABLE")) {
                    npc.setClickable(false);
                    player.openInventory(mc.getMainMenu());
                    player.sendMessage(ChatColor.AQUA + "The NPC is now " + ChatColor.RED + "" + ChatColor.BOLD + "NOT CLICKABLE");
                }
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("changeSkin")) {
                player.sendMessage("You're changing the NPC's Skin.");
                e.setCancelled(true);
                player.openInventory(CustomNPCs.getInstance().invs.get(0));

            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("equipment")) {
                e.setCancelled(true);
                player.openInventory(mc.getArmorMenu());
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("Confirm")) {
                Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), npc::createNPC, 1);
                player.sendMessage(npc.isResilient() ? ChatColor.GREEN + "Reslilient NPC created!" : ChatColor.GREEN + "Temporary NPC created!");
                player.closeInventory();
                e.setCancelled(true);
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("Cancel")) {
                player.sendMessage(ChatColor.RED + "NPC aborted");
                player.closeInventory();
                e.setCancelled(true);
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("actions")) {
                player.openInventory(mc.getActionMenu());
                e.setCancelled(true);
            }
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "SkinButton"))) {
            e.setCancelled(true);
            String name = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "SkinButton"), PersistentDataType.STRING);
            npc.setValue(MenuUtils.getValue(name));
            npc.setSignature(MenuUtils.getSignature(name));
            npc.setSkinName(name);
            player.sendMessage(ChatColor.GREEN + "Skin changed to " + ChatColor.BOLD + name);
            CustomNPCs.getInstance().pages.put(player, 0);
            player.closeInventory();
            player.openInventory(mc.getMainMenu());
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "NoClickey"))) {
            e.setCancelled(true);
            String tag = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "NoClickey"), PersistentDataType.STRING);
            if (tag.equals("prev")) {
                player.openInventory(CustomNPCs.getInstance().invs.get(CustomNPCs.instance.getPage(player) - 1));
                CustomNPCs.getInstance().setPage(player, CustomNPCs.getInstance().getPage(player) - 1);
            } else if (tag.equals("next")) {
                player.openInventory(CustomNPCs.getInstance().invs.get(CustomNPCs.instance.getPage(player) + 1));
                CustomNPCs.getInstance().setPage(player, CustomNPCs.getInstance().getPage(player) + 1);
            } else if (tag.equals("close")) {
                player.openInventory(mc.getMainMenu());
            }
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "EquipmentInv"))) {
            String button = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "EquipmentInv"), PersistentDataType.STRING);
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
                case "close" -> player.openInventory(mc.getMainMenu());
            }
            e.setCancelled(true);
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "ActionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "ActionButton"), PersistentDataType.STRING);
            if (e.getAction() == InventoryAction.PICKUP_HALF) {
                switch (itemData) {
                    case "RUN_COMMAND" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("RUN_COMMAND")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "DISPLAY_TITLE" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("DISPLAY_TITLE")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "SEND_MESSAGE" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("SEND_MESSAGE")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "PLAY_SOUND" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("PLAY_SOUND")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "ACTION_BAR" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("ACTION_BAR")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "TELEPORT" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("TELEPORT")) {
                            npc.getActions().remove(action);
                        }
                    });
                    case "SEND_TO_SERVER" -> npc.getActions().forEach(action -> {
                        if (action.getSubCommand().equals("SEND_TO_SERVER")) {
                            npc.getActions().remove(action);
                        }
                    });
                }
            }
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "ActionInv"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "ActionInv"), PersistentDataType.STRING);
            List<Action> actionList = new ArrayList<>(npc.getActions());
            if (itemData.equals("new_action")) {
                player.openInventory(mc.getNewActionMenu());
            } else if (itemData.equals("go_back")) {
                player.openInventory(mc.getMainMenu());
            } else {
                if (e.getAction() == InventoryAction.PICKUP_HALF) {
                    switch (itemData) {
                        case "RUN_COMMAND" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("RUN_COMMAND")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "DISPLAY_TITLE" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("DISPLAY_TITLE")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "SEND_MESSAGE" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("SEND_MESSAGE")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "PLAY_SOUND" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("PLAY_SOUND")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "ACTION_BAR" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("ACTION_BAR")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "TELEPORT" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("TELEPORT")) {
                                npc.getActions().remove(action);
                            }
                        });
                        case "SEND_TO_SERVER" -> actionList.forEach(action -> {
                            if (action.getSubCommand().equals("SEND_TO_SERVER")) {
                                npc.getActions().remove(action);
                            }
                        });
                    }
                    e.setCancelled(true);
                    player.openInventory(mc.getActionMenu());
                } else {
                    // Editing
                    switch (itemData) {
                        case "RUN_COMMAND" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("RUN_COMMAND")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "DISPLAY_TITLE" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("DISPLAY_TITLE")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "SEND_MESSAGE" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("SEND_MESSAGE")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "PLAY_SOUND" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("PLAY_SOUND")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "ACTION_BAR" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("ACTION_BAR")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "TELEPORT" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("TELEPORT")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                        case "SEND_TO_SERVER" -> npc.getActions().forEach(action -> {
                            if (action.getSubCommand().equals("SEND_TO_SERVER")) {
                                player.openInventory(mc.getActionCustomizerMenu(action));
                            }
                        });
                    }
                }
            }
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "NewActionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "NewActionButton"), PersistentDataType.STRING);

            Action action = null;

            switch (itemData) {
                case "RUN_COMMAND" ->
                        action = new Action("RUN_COMMAND", new ArrayList<>(Arrays.asList("command", "to", "be", "run")));
                case "DISPLAY_TITLE" ->
                        action = new Action("DISPLAY_TITLE", new ArrayList<>(Arrays.asList("10", "20", "10", "title!")));
                case "SEND_MESSAGE" ->
                        action = new Action("SEND_MESSAGE", new ArrayList<>(Arrays.asList("message", "to", "be", "sent")));
                case "PLAY_SOUND" -> {
                    //action = new Action("PLAY_SOUND", new ArrayList<>(Arrays.asList("1", "1", "sound", "to", "be", "played")));
                    player.sendMessage(ChatColor.RED + "This is currently not implemented yet. Please nudge Foxikle about getting it done sooner.");
                    e.setCancelled(true);
                    return;
                }
                case "ACTION_BAR" ->
                        action = new Action("ACTION_BAR", new ArrayList<>(Arrays.asList("actionbar", "to", "be", "sent")));
                case "TELEPORT" ->
                        action = new Action("TELEPORT", new ArrayList<>(Arrays.asList("0", "0", "0", "0", "0")));
                case "SEND_TO_SERVER" ->
                        action = new Action("SEND_TO_SERVER", new ArrayList<>(Arrays.asList("server", "to", "be", "sent", "to")));
                case "go_back" -> player.openInventory(mc.getActionMenu());
            }
            if(action != null) {
                CustomNPCs.getInstance().editingActions.put(player, action);
                player.openInventory(mc.getActionCustomizerMenu(action));
            }
        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "CustomizeActionButton"))) {
            String itemData = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "CustomizeActionButton"), PersistentDataType.STRING);
            Action action = CustomNPCs.getInstance().editingActions.get(player);
            switch (itemData) {
                // RUN_COMMAND
                case "edit_command" -> {
                    player.closeInventory();
                    CustomNPCs.getInstance().commandWaiting.add(player);
                    new CommandRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 0, 10);
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
                    CustomNPCs.getInstance().titleWaiting.add(player);
                    new TitleRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 0, 10);
                    e.setCancelled(true);
                    return;
                }

                //SEND_MESSAGE
                case "edit_message" ->{
                    player.closeInventory();
                    CustomNPCs.getInstance().messageWaiting.add(player);
                    new MessageRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 0,10);
                    e.setCancelled(true);
                    return;
                }
                    // use a runnable
                case "PLAY_SOUND" -> // not done yet.
                        //player.openInventory(mc.getActionCustomizerMenu(new Action("PLAY_SOUND", new ArrayList<>(Arrays.asList("1", "1", "sound", "to", "be", "played")))));
                        player.sendMessage(ChatColor.RED + "How in the kentucky fried flip flop did you get this menu?!");
                // ACTION_BAR
                case "edit_actionbar" -> {
                    player.closeInventory();
                    CustomNPCs.getInstance().actionbarWaiting.add(player);
                    new ActionbarRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 0, 10);
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
                    CustomNPCs.getInstance().serverWaiting.add(player);
                    new ServerRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 0,10);
                    e.setCancelled(true);
                    return;
                }
                    // runnable things
                case "go_back" -> {
                    Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> player.openInventory(mc.getActionMenu()), 1);
                }
                case "confirm" -> {
                    npc.addAction(action);
                    Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> player.openInventory(mc.getActionMenu()), 1);
                }
            }
            e.setCancelled(true);
            player.openInventory(mc.getActionCustomizerMenu(action));
        }
    }
}

