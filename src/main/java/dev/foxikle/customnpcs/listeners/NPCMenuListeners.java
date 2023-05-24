package dev.foxikle.customnpcs.listeners;

import dev.foxikle.customnpcs.ChatRunnable;
import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import dev.foxikle.customnpcs.menu.MenuCore;
import dev.foxikle.customnpcs.menu.MenuUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static org.bukkit.Material.*;

public class NPCMenuListeners implements Listener {

    Map<Player, MenuCore> map = CustomNPCs.getInstance().menus;

    private void OpenAnvil(Player p) {
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> stateSnapshot.getPlayer().sendMessage("You closed the inventory."))
                .onClick((slot, stateSnapshot) -> {
                    if(slot != AnvilGUI.Slot.OUTPUT) {
                        return new ArrayList<>();
                    }
                    String text = stateSnapshot.getText();
                    map.get(p).getNpc().setName(ChatColor.translateAlternateColorCodes('&', text));
                    Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> p.openInventory(map.get(p).getMainMenu()), 1);
                    p.sendMessage(ChatColor.GREEN + "The NPC's name was set to: " + ChatColor.BOLD + text);
                    return List.of(AnvilGUI.ResponseAction.close());
                })
                .preventClose()
                .text("Type here")
                .title(ChatColor.BLACK + "" + ChatColor.BOLD + "  Enter a Name")
                .plugin(CustomNPCs.getInstance())
                .open(p);
    }


    @EventHandler
    public void OnInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        NamespacedKey key = new NamespacedKey(CustomNPCs.getInstance(), "MenuButtonTag");
        ItemStack item = e.getCurrentItem();
        PersistentDataContainer tagContainer = item.getItemMeta().getPersistentDataContainer();
        Player player = (Player) e.getWhoClicked();
        MenuCore mc = map.get(player);
        NPC npc = mc.getNpc();
        if (tagContainer.get(key, PersistentDataType.STRING) != null) {
            if (tagContainer.get(key, PersistentDataType.STRING).equals("NameTag")) {
                e.setCancelled(true);
                OpenAnvil(player);
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("direction")) {
                double dir = npc.getFacingDirection();
                if(e.getAction() == InventoryAction.PICKUP_ALL) {
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
                } else if(e.getAction() == InventoryAction.PICKUP_HALF){
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
            } else if (tagContainer.get(key, PersistentDataType.STRING).equals("command")) {
                CustomNPCs.getInstance().waiting.add(player);
                player.sendMessage(ChatColor.RED + "Type in chat the command that should be executed. Do not include the slash.");
               new ChatRunnable(player).runTaskTimer(CustomNPCs.getInstance(), 1, 15);
                player.closeInventory();
                e.setCancelled(true);
            }

        } else if (tagContainer.getKeys().contains(new NamespacedKey(CustomNPCs.getInstance(), "SkinButton"))) {
            e.setCancelled(true);
            String name = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomNPCs.getInstance(), "SkinButton"), PersistentDataType.STRING);
            npc.setValue(MenuUtils.getValue(name));
            npc.setSignature(MenuUtils.getSignature(name));
            npc.setSkinName(name);
            player.sendMessage(ChatColor.GREEN + "Skin changed to " + ChatColor.BOLD + name);
            CustomNPCs.getInstance().pages.put(player,0);
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
                case "close" ->
                        player.openInventory(mc.getMainMenu());
            }
            e.setCancelled(true);
        }
    }
}
