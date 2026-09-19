/*
 * Copyright (c) 2024-2026. Foxikle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.foxikle.customnpcs.internal.menu;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.*;
import dev.foxikle.customnpcs.conditions.Comparator;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.OpenButtonAction;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import io.github.mqzen.menus.base.pagination.exception.InvalidPageException;
import io.github.mqzen.menus.misc.button.Button;
import io.github.mqzen.menus.misc.button.actions.ButtonClickAction;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

import static org.bukkit.Material.*;


public class MenuItems {
    public static final Button MENU_GLASS;
    private static final CustomNPCs plugin = CustomNPCs.getInstance();

    static {
        MENU_GLASS = Button.clickable(ItemBuilder.modern(Material.BLACK_STAINED_GLASS_PANE).setDisplay(Component.text(" ")).build(), ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button changeLines(InternalNpc npc, Player player) {

        Component lines = Component.empty();

        for (int i = 0; i < npc.getSettings().getHolograms().size(); i++) {
            String raw = npc.getSettings().getRawHolograms().get(i);
            Component holo = npc.getSettings().getHolograms().get(i);
            if (raw.isEmpty()) {
                holo = Msg.get(player, "messages.empty_string");
            }
            lines = lines.append(Msg.format("   <dark_gray>" + (i + 1) + ". ").append(holo)).append(Component.newline());
        }

        Component[] lore = Msg.vlore(player.locale(), "menus.main.items.name.current_name", 100, Arg.arg(lines));

        return Button.clickable(ItemBuilder.modern(Material.NAME_TAG).setDisplay(Msg.get(player, "menus.main.items.name.name")).setLore(lore).build(), new OpenButtonAction(MenuUtils.NPC_HOLOGRAMS));
    }

    public static Button resilient(InternalNpc npc, Player player) {
        ItemStack i = ItemBuilder.modern(Material.BELL).setLore(npc.getSettings().isResilient() ? Msg.get(player, "menus.main.items.resilient.true") : Msg.get(player, "menus.main.items.resilient.false")).setDisplay(Msg.get(player, "menus.main.items.resilient.change")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            if (npc.getSettings().isResilient()) p.sendMessage(Msg.get(p, "menus.main.resilient.message.now_false"));
            else p.sendMessage(Msg.get(p, "menus.main.resilient.message.now_true"));

            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            menuView.replaceButton(22, MenuItems.resilient(npc, p));
        }));
    }

    public static ItemStack skinSelection(InternalNpc npc, Player player) {
        Object currentSkin = npc.getSettings().getSkinName();
        if (npc.getSettings().getSkinName().isBlank()) {
            currentSkin = Msg.get(player, "menus.main.items.skin.default");
        }
        return ItemBuilder.modern(Material.PLAYER_HEAD).modifyMeta(SkullMeta.class, skullMeta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9";
            profile.setProperty(new ProfileProperty("textures", texture));
            skullMeta.setPlayerProfile(profile);
        }).setLore(Msg.lore(player.locale(), "menus.main.items.skin.lore", Arg.arg(currentSkin)))
                .setDisplay(Msg.get(player, "menus.main.items.skin.name")).build();
    }

    public static ItemStack extraSettings(Player player) {
        return ItemBuilder.modern(Material.COMPARATOR).setDisplay(Msg.get(player, "menus.main.settings.name")).build();
    }

    public static ItemStack deleteNpc(Player player) {
        return ItemBuilder.modern(Material.LAVA_BUCKET).setDisplay(Msg.get(player, "menus.main.delete.name")).build();
    }

    public static ItemStack looking(Player player) {
        return ItemBuilder.modern(Material.ENDER_EYE).setDisplay(Msg.get(player, "menus.main.facing.name")).setLore(Msg.lore(player.locale(), "menus.main.facing.description")).build();
    }

    public static Button interactable(InternalNpc npc, Player player) {
        boolean interactable = npc.getSettings().isInteractable();

        return Button.clickable(ItemBuilder.modern(interactable ? Material.OAK_SAPLING : Material.DEAD_BUSH).setLore(interactable ? Msg.get(player, "menus.main.interactable.true") : Msg.get(player, "menus.main.interactable.false")).setDisplay(Msg.get(player, "menus.main.interactable.name.toggle")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (npc.getSettings().isInteractable())
                p.sendMessage(Msg.get(player, "menus.main.interactable.message.now_false"));
            else p.sendMessage(Msg.get(player, "menus.main.interactable.message.now_true"));

            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            menuView.replaceButton(25, MenuItems.interactable(npc, p));
            menuView.replaceButton(34, MenuItems.showActions(npc, p));
        }));
    }

    public static Button showActions(InternalNpc npc, Player player) {
        if (!npc.getSettings().isInteractable()) return MENU_GLASS;
        return Button.clickable(ItemBuilder.modern(Material.RECOVERY_COMPASS).setDisplay(Msg.get(player, "menus.main.interactable.name")).setLore(Msg.lore(player.locale(), "menus.main.interactable.description")).build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static ItemStack editEquipment(InternalNpc npc, Player p) {
        Equipment equip = npc.getEquipment();
        return ItemBuilder.modern(Material.ARMOR_STAND).setDisplay(Msg.get(p, "menus.main.items.equipment.name")).setLore(Msg.get(p, "menus.main.items.equipment.main_hand", Arg.arg(equipmentName(p, equip.getHand()))), Msg.get(p, "menus.main.items.equipment.off_hand", Arg.arg(equipmentName(p, equip.getOffhand()))), Msg.get(p, "menus.main.items.equipment.helmet", Arg.arg(equipmentName(p, equip.getHead()))), Msg.get(p, "menus.main.items.equipment.chestplate", Arg.arg(equipmentName(p, equip.getChest()))), Msg.get(p, "menus.main.items.equipment.leggings", Arg.arg(equipmentName(p, equip.getLegs()))), Msg.get(p, "menus.main.items.equipment.boots", Arg.arg(equipmentName(p, equip.getBoots())))).build();
    }

    private static Component equipmentName(Player p, @Nullable ItemStack item) {
        if (item == null) {
            return Msg.get(p, "menus.main.items.equipment.empty");
        }
        return Component.translatable(item.translationKey());
    }

    public static Button tunnelVision(InternalNpc npc, Player player) {
        return Button.clickable(ItemBuilder.modern(Material.SPYGLASS).setDisplay(Msg.get(player, "menus.main.vision.name")).setLore(npc.getSettings().isTunnelvision() ? Msg.get(player, "menus.main.vision.tunnel") : Msg.get(player, "menus.main.vision.normal")).build(), ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            menuView.replaceButton(28, MenuItems.tunnelVision(npc, p));
        }));
    }

    public static ItemStack confirmCreation(Player player) {
        return ItemBuilder.modern(Material.LIME_DYE).setDisplay(Msg.get(player, "menus.main.create.name")).build();
    }

    public static ItemStack cancelCreation(Player player) {
        return ItemBuilder.modern(BARRIER).setDisplay(Msg.get(player, "menus.main.cancel.name")).build();
    }

    public static ItemStack importArmor(Player player) {
        return ItemBuilder.modern(Material.ARMOR_STAND).setDisplay(Msg.get(player, "menus.equipment.import")).setLore(Msg.get(player, "menus.equipment.import.description")).build();
    }

    public static Button helmetSlot(InternalNpc npc, Player player) {
        ItemStack helm = npc.getEquipment().getHead();
        if (helm == null || helm.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE).setDisplay(Msg.get(player, "menus.equipment.helmet.empty")).setLore(Msg.lore(player.locale(), "menus.equipment.helmet.change")).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == Material.AIR || event.isRightClick()) return;
                p.playSound(p, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                npc.getEquipment().setHead(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHead()))));
                menuView.replaceButton(13, helmetSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.helmet.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(helm).setDisplay(Component.text(helm.getType().name().toLowerCase(), NamedTextColor.GREEN)).addFlags(ItemFlag.values()).setLore(lore.toArray(new Component[]{})).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setHead(new ItemStack(Material.AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.reset"));
                    menuView.replaceButton(13, helmetSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == Material.AIR) return;
                    npc.getEquipment().setHead(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHead()))));
                    menuView.replaceButton(13, helmetSlot(npc, p));
                }
            }));

        }
    }

    public static Button chestplateSlot(InternalNpc npc, Player player) {
        ItemStack cp = npc.getEquipment().getChest();
        if (cp == null || cp.getType().isAir()) {

            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).setLore(Msg.lore(player.locale(), "menus.equipment.chestplate.change")).setDisplay(Msg.get(player, "menus.equipment.chestplate.empty")).build(), ButtonClickAction.plain((menuView, event) -> {
                Player p = (Player) event.getWhoClicked();
                event.setCancelled(true);
                //todo: look into item components
                if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getChest()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.error"));
                }

                menuView.replaceButton(22, chestplateSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.chestplate.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(cp).addFlags(ItemFlag.values()).setLore(lore.toArray(new Component[]{})).setDisplay(Component.text(cp.getType().toString(), NamedTextColor.GREEN)).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setChest(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.reset"));
                    menuView.replaceButton(22, chestplateSlot(npc, p));
                    return;
                } else if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getChest()))));
                    return;
                } else {
                    if (event.getCursor().getType() == AIR) return;
                }
                event.setCancelled(true);
                p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.error"));
                menuView.replaceButton(22, chestplateSlot(npc, p));
            }));
        }
    }

    public static Button leggingsSlot(InternalNpc npc, Player player) {
        ItemStack legs = npc.getEquipment().getLegs();
        if (legs == null || legs.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).setLore(Msg.lore(player.locale(), "menus.equipment.legs.change")).setDisplay(Msg.get(player, "menus.equipment.legs.empty")).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getLegs()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.error"));
                }
                menuView.replaceButton(31, leggingsSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.legs.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(legs).addFlags(ItemFlag.values()).setDisplay(Component.text(legs.getType().toString(), NamedTextColor.GREEN)).setLore(lore.toArray(new Component[]{})).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setLegs(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.reset"));
                    menuView.replaceButton(31, leggingsSlot(npc, player));
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getLegs()))));
                    menuView.replaceButton(31, leggingsSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.error"));
                }
            }));
        }
    }

    public static Button bootsSlot(InternalNpc npc, Player player) {
        ItemStack boots = npc.getEquipment().getBoots();
        if (boots == null || boots.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(LIME_STAINED_GLASS_PANE).setDisplay(Msg.get(player, "menus.equipment.boots.empty")).addFlags(ItemFlag.values()).setLore(Msg.lore(player.locale(), "menus.equipment.boots.change")).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType().name().contains("BOOTS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getBoots()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.error"));
                    return;
                }
                menuView.replaceButton(40, bootsSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.boots.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(boots).setLore(lore.toArray(new Component[]{})).setDisplay(Component.text(boots.getType().toString(), NamedTextColor.GREEN)).addFlags(ItemFlag.values()).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setBoots(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.reset"));
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getBoots()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.error"));
                    return;
                }
                menuView.replaceButton(40, bootsSlot(npc, p));
            }));
        }
    }

    public static Button handSlot(InternalNpc npc, Player player) {
        ItemStack hand = npc.getEquipment().getHand();
        if (hand == null || hand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).setLore(Msg.lore(player.locale(), "menus.equipment.hand.change")).setDisplay(Msg.get(player, "menus.equipment.hand.empty")).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setHand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHand()))));
                menuView.replaceButton(23, handSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(hand).setLore(lore.toArray(new Component[]{})).setDisplay(Component.text(hand.getType().toString(), NamedTextColor.GREEN)).addFlags(ItemFlag.values()).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setHand(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.reset"));
                    menuView.replaceButton(23, handSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setHand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHand()))));
                    menuView.replaceButton(23, handSlot(npc, p));
                }
            }));
        }
    }

    public static Button offhandSlot(InternalNpc npc, Player player) {
        ItemStack offhand = npc.getEquipment().getOffhand();
        if (offhand == null || offhand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE).setDisplay(Msg.get(player, "menus.equipment.offhand.empty")).setLore(Msg.lore(player.locale(), "menus.equipment.offhand.change")).addFlags(ItemFlag.values()).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setOffhand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                p.sendMessage(Msg.get(p, "menus.equipment.offhand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getOffhand()))));
                menuView.replaceButton(21, offhandSlot(npc, p));
            }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.modern(offhand).setLore(lore.toArray(new Component[]{})).setDisplay(Component.text(offhand.getType().toString(), NamedTextColor.GREEN)).addFlags(ItemFlag.values()).build(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setOffhand(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.offhand.reset"));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setOffhand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.offhand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getOffhand()))));
                }
                menuView.replaceButton(21, offhandSlot(npc, p));
            }));
        }
    }

    public static Button toPose(Player player) {
        return Button.clickable(ItemBuilder.modern(SNIFFER_EGG).setDisplay(Msg.get(player, "pose.pose_editor")).build(), new OpenButtonAction(MenuUtils.NPC_POSE));
    }

    public static Button toMain(Player player) {
        return Button.clickable(ItemBuilder.modern(BARRIER).setDisplay(Msg.get(player, "items.go_back")).build(), new OpenButtonAction(MenuUtils.NPC_MAIN));
    }

    public static Button toAction(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.get(player, "items.go_back")).build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static Button toActionSaveConditions(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.get(player, "items.go_back")).build(), ButtonClickAction.plain((menu, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            plugin.getLotus().openMenu(p, MenuUtils.NPC_ACTION_CUSTOMIZER);
        }));
    }

    public static Button toNewCondition(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.get(player, "items.go_back")).build(), new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toConditionCustomizer(Player player) {
        return Button.clickable(ItemBuilder.modern(COMPARATOR).setDisplay(Msg.get(player, "menus.action_customizer.conditions")).build(), new OpenButtonAction(MenuUtils.NPC_CONDITION_CUSTOMIZER));
    }

    public static List<Button> currentActions(InternalNpc npc, Player player) {
        List<Button> buttons = new ArrayList<>();

        for (Action action : npc.getActions()) {
            buttons.add(Button.clickable(action.getFavicon(player), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    npc.removeAction(action);
                    menuView.getAPI().openMenu(p, MenuUtils.NPC_ACTIONS);
                } else if (event.isLeftClick()) {
                    if (action.canEdit()) {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                        plugin.editingActions.put(p.getUniqueId(), action.clone());
                        plugin.originalEditingActions.put(p.getUniqueId(), action);
                        plugin.getLotus().openMenu(p, MenuUtils.NPC_ACTION_CUSTOMIZER);
                    } else {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        p.sendMessage(Msg.get(p, "edit.fail"));
                    }
                }
            })));
        }

        buttons.add(Button.clickable(ItemBuilder.modern(LILY_PAD).setDisplay(Msg.get(player, "menus.actions.new")).build(), new OpenButtonAction(MenuUtils.NPC_NEW_ACTION)));

        return buttons;
    }

    public static List<Button> currentLines(InternalNpc npc, Player player) {
        List<Button> buttons = new ArrayList<>();

        List<String> raw = npc.getSettings().getRawHolograms();
        List<String> mutable = new ArrayList<>(raw);
        for (int i = 0; i < raw.size(); i++) {
            String line = raw.get(i);
            List<Component> lore = Utils.list(Msg.format(line), Component.empty(), Msg.get(player, "menus.holograms.edit"), Msg.get(player, "menus.holograms.delete"));
            boolean canMoveDown = i < raw.size() - 1;
            boolean canMoveUp = i > 0;

            if (canMoveDown) lore.add(Msg.get(player, "menus.holograms.move_down"));
            if (canMoveUp) lore.add(Msg.get(player, "menus.holograms.move_up"));

            int finalI = i;
            buttons.add(Button.clickable(ItemBuilder.modern(PAPER).setDisplay(Msg.get(player, "menus.holograms.line", Arg.arg(i + 1))).setLore(lore).build(), ButtonClickAction.plain((_, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                // DROP is delete
                // DROP_STACK is delete without confirmation
                // SWAP TO OFFHAND is edit
                // LEFT is up
                // RIGHT is down

                if (event.getClick() == ClickType.DROP) {
                    if (npc.getSettings().getRawHolograms().size() == 1) {
                        player.sendMessage(Msg.get(player, "menus.holograms.min_one"));
                        return;
                    }
                    HologramMenu.editingIndicies.put(p.getUniqueId(), finalI);
                    plugin.getLotus().openMenu(p, MenuUtils.NPC_DELETE_LINE);
                    return;
                }
                if (event.getClick() == ClickType.CONTROL_DROP) {
                    if (npc.getSettings().getRawHolograms().size() == 1) {
                        player.sendMessage(Msg.get(player, "menus.holograms.min_one"));
                        return;
                    }
                    mutable.remove(finalI);
                    player.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1F, 1F);
                    npc.getSettings().getRawHolograms().remove(finalI);
                    npc.getSettings().setHolograms(mutable);
                    plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                    return;
                }
                if (event.getClick() == ClickType.SWAP_OFFHAND) {
                    p.sendMessage(Msg.get(p, "data.name.title"));

                    plugin.wait(p, WaitingType.NAME);

                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    HologramMenu.editingIndicies.put(p.getUniqueId(), finalI);
                    p.closeInventory();

                    if (plugin.getConfig().getBoolean("NameReferenceMessages")) {
                        p.sendMessage(Msg.get(p, "name.reference"));
                        p.sendMessage(line);
                        p.sendMessage(Msg.get(p, "name.toggle_reference_message"));
                    }
                    return;
                }

                if (event.isLeftClick()) {
                    if (!canMoveUp) {
                        p.sendMessage(Msg.get(p, "menus.holograms.move_up_fail"));
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        return;
                    }

                    Collections.swap(mutable, finalI, finalI - 1);
                    npc.getSettings().setHolograms(mutable);
                    plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                    p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, .7F, .9F);
                    return;
                }

                if (!canMoveDown) {
                    p.sendMessage(Msg.get(p, "menus.holograms.move_down_fail"));
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                    return;
                }

                Collections.swap(mutable, finalI, finalI + 1);
                npc.getSettings().setHolograms(mutable);
                plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                p.playSound(p.getLocation(), Sound.BLOCK_PISTON_CONTRACT, .7F, .9F);
            })));
        }

        buttons.add(Button.clickable(ItemBuilder.modern(LILY_PAD).setDisplay(Msg.get(player, "menus.holograms.new_line")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();

            plugin.wait(p, WaitingType.NAME);

            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            HologramMenu.editingIndicies.put(p.getUniqueId(), raw.size());
            p.closeInventory();
        })));

        return buttons;
    }

    public static Button delayDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(CLOCK).setDisplay(Msg.get(player, "menus.action_customizer.delay.name", Arg.arg(action.getDelay()))).build(), ButtonClickAction.plain((_, event) -> event.setCancelled(true)));
    }

    public static Button decrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action_customizer.delay.decrement")).setLore(Msg.lore(player.locale(), "menus.action_customizer.delay.decrement.description")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (action.getDelay() == 0) {
                p.sendMessage(Msg.get(p, "menus.action_customizer.delay.error"));
                return;
            }
            action.setDelay(Math.max(0, Utils.decrement(event).apply(action.getDelay())));
            menuView.updateButton(4, button -> button.setItem(delayDisplay(action, p).getItem()));
        }));
    }

    public static Button incrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action_customizer.delay.increment")).setLore(Msg.lore(player.locale(), "menus.action_customizer.delay.increment.description")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            action.setDelay(Utils.increment(event).apply(action.getDelay()));

            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.updateButton(4, button -> button.setItem(delayDisplay(action, p).getItem()));
        }));
    }

    public static Button cooldownDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(CLOCK).setDisplay(Msg.get(player, "menus.action_customizer.cooldown.name", Arg.arg(action.getCooldown()))).build(), ButtonClickAction.plain((_, event) -> event.setCancelled(true)));
    }

    public static Button decrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action_customizer.cooldown.decrement")).setLore(Msg.lore(player.locale(), "menus.action_customizer.cooldown.decrement.description")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (action.getCooldown() == 0) {
                p.sendMessage(Msg.get(p, "menus.action_customizer.cooldown.error"));
                return;
            }
            action.setCooldown(Math.max(0, Utils.decrement(event).apply(action.getCooldown())));

            menuView.updateButton(7, button -> button.setItem(cooldownDisplay(action, p).getItem()));
        }));
    }

    public static Button incrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action_customizer.cooldown.increment")).setLore(Msg.lore(player.locale(), "menus.action_customizer.cooldown.increment.description")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            action.setCooldown(Math.max(0, Utils.increment(event).apply(action.getCooldown())));

            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.updateButton(7, button -> button.setItem(cooldownDisplay(action, p).getItem()));
        }));
    }

    public static Button saveAction(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LILY_PAD).setDisplay(Msg.get(player, "menus.action_customizer.confirm")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(p.getUniqueId());

            if (npc == null) {
                p.sendMessage(Msg.get(p, "menus.main.error.no_npc.lore"));
                return;
            }

            if (CustomNPCs.getInstance().originalEditingActions.get(p.getUniqueId()) != null)
                npc.removeAction(CustomNPCs.getInstance().originalEditingActions.remove(p.getUniqueId()));
            npc.addAction(action);

            menuView.getAPI().openMenu(p, MenuUtils.NPC_ACTIONS);
        }));
    }

    public static Button display(Component text, Component... lore) {
        return Button.clickable(ItemBuilder.modern(CLOCK).setDisplay(text).setLore(lore).build(),
                ButtonClickAction.plain((v, e) -> e.setCancelled(true)));
    }

    public static Button saveCondition(Player player) {
        return Button.clickable(ItemBuilder.modern(LILY_PAD).setDisplay(Msg.get(player, "menus.main.create.name")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);

            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Action actionImpl = plugin.editingActions.get(p.getUniqueId());
            Condition original = plugin.originalEditingConditionals.get(p.getUniqueId());
            if (original != null) actionImpl.removeCondition(original);
            Condition edited = plugin.editingConditionals.get(p.getUniqueId());
            actionImpl.addCondition(edited);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button comparatorSwitcher(Condition condition, Player player, int slot) {

        List<Component> lore = new ArrayList<>();
        for (Comparator c : Comparator.getSupportedConditions(condition)) {
            if (condition.getComparator() != c) lore.add(Msg.get(player, c.getKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(player, c.getKey()).color(NamedTextColor.DARK_AQUA)));
        }
        lore.add(Msg.get(player, "items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR).setDisplay(Msg.get(player, "comparator")).setLore(lore.toArray(new Component[]{})).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            List<Comparator> comparators = List.copyOf(Comparator.getSupportedConditions(condition));

            int index = comparators.indexOf(condition.getComparator());
            if (event.isLeftClick()) {
                if (comparators.size() > (index + 1)) {
                    condition.setComparator(comparators.get(index + 1));
                } else {
                    condition.setComparator(comparators.getFirst());
                }
            } else if (event.isRightClick()) {
                if (index == 0) {
                    condition.setComparator(comparators.getLast());
                } else {
                    condition.setComparator(comparators.get(index - 1));
                }
            }
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.replaceButton(slot, comparatorSwitcher(condition, p, slot));
        }));
    }

    public static Button targetValueSelector(Condition condition, Player player) {
        ItemStack i = ItemBuilder.modern(OAK_HANGING_SIGN).setDisplay(Msg.get(player, "value.select")).setLore(Msg.get(player, "value.current", Arg.arg(condition.getTarget())), Msg.get(player, "items.click_to_change")).build();

        return Button.clickable(i, ButtonClickAction.plain((_, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.TARGET);
        }));
    }

    public static Button valueSwitcher(Condition condition, Player player, int slot) {
        List<Component> lore = new ArrayList<>();

        for (Condition.Value v : Condition.Value.getSupportedConditions(condition)) {

            if (condition.getValue() != v) lore.add(Msg.get(player, v.getTranslationKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(player, v.getTranslationKey()).color(NamedTextColor.DARK_AQUA)));

        }
        lore.add(Msg.get(player, "items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR).setDisplay(Msg.get(player, "statistic")).setLore(lore.toArray(new Component[]{})).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            List<Condition.Value> values = List.copyOf(Condition.Value.getSupportedConditions(condition));

            int index = values.indexOf(condition.getValue());
            if (event.isLeftClick()) {
                if (values.size() > (index + 1)) {
                    condition.setValue(values.get(index + 1));
                } else {
                    condition.setValue(values.getFirst());
                }
            } else if (event.isRightClick()) {
                if (index == 0) {
                    condition.setValue(values.getLast());
                } else {
                    condition.setValue(values.get(index - 1));
                }
            }
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.updateButton(slot, button -> button.setItem(valueSwitcher(condition, player, slot).getItem()));
        }));
    }

    public static Button interactableHologram(InternalNpc npc, Player player) {
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.modern(hideClickableTag ? RED_CANDLE : GREEN_CANDLE).setDisplay(Msg.get(player, "menus.extra.hologram_visibility")).setLore(Component.empty(), Msg.get(player, "menus.extra.hologram_visibility.description"), hideClickableTag ? Msg.get(player, "menus.extra.hologram_visibility.description.hidden") : Msg.get(player, "menus.extra.hologram_visibility.description.shown")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setHideClickableHologram(!hideClickableTag);
            menuView.replaceButton(11, interactableHologram(npc, p));
        }));
    }

    public static Button interactableText(Player player) {
        ItemStack i = ItemBuilder.modern(NAME_TAG).setDisplay(Msg.get(player, "menus.extra.hologram_text")).setLore(Msg.lore(player.locale(), "menus.extra.hologram_text.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            plugin.wait(p, WaitingType.HOLOGRAM);

            p.closeInventory();
            p.sendMessage(Msg.get(p, "menus.extra.hologram_text.type"));
        }));
    }

    public static Button upsideDown(InternalNpc npc, Player player) {
        List<Component> lore = Utils.list(Component.empty(), Msg.get(player, "menus.extra.upside_down.description"));
        boolean upsideDown = npc.getSettings().isUpsideDown();
        lore.addAll(List.of(upsideDown ? Msg.lore(player.locale(), "menus.extra.upside_down.description.true") : Msg.lore(player.locale(), "menus.extra.upside_down.description.false")));
        ItemStack i = ItemBuilder.modern(upsideDown ? GREEN_CANDLE : RED_CANDLE).setDisplay(Msg.get(player, "menus.extra.upside_down")).setLore(lore).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setUpsideDown(!upsideDown);
            menuView.replaceButton(15, upsideDown(npc, p));
        }));
    }

    public static Button importPlayer(Player player) {
        ItemStack i = ItemBuilder.modern(ANVIL).setDisplay(Msg.get(player, "menus.skins.player")).setLore(Msg.lore(player.locale(), "menus.skins.player.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.PLAYER);
            event.setCancelled(true);
        }));
    }

    public static Button useCatalog(Player player) {
        ItemStack i = ItemBuilder.modern(ARMOR_STAND).setDisplay(Msg.get(player, "menus.skins.catalog")).setLore(Msg.lore(player.locale(), "menus.skins.catalog.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            try {
                CustomNPCs.getInstance().getSkinCatalog(p).open(p);
            } catch (InvalidPageException e) {
                p.sendMessage(Msg.get(p, "error.cant_open_skin_catalog"));
                CustomNPCs.getInstance().getLogger().log(Level.SEVERE, "An error occurred whilst opening the Skin Catalog!", e);
            }
        }));
    }

    public static Button importUrl(Player player) {
        ItemStack i = ItemBuilder.modern(WRITABLE_BOOK).setDisplay(Msg.get(player, "menus.skins.url")).setLore(Msg.lore(player.locale(), "menus.skins.url.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.URL);
            event.setCancelled(true);
        }));
    }

    public static List<Button> conditions(Action action, Player player) {
        List<Button> buttons = new ArrayList<>();

        if (action.getConditions() == null) {
            return Utils.list(newCondition(action, player));
        }

        for (Condition condition : action.getConditions()) {
            Material mat = switch (condition.getType()) {
                case NUMERIC -> POPPED_CHORUS_FRUIT;
                case LOGICAL -> COMPARATOR;
                case TEXT -> BOOK;
            };
            //todo: text conditions can be inverted (outsource this to a condition impl class?)
            ItemStack i = ItemBuilder.modern(mat).setDisplay(Msg.get(player, "menus.conditions." + condition.getType().name().toLowerCase())).setLore(Component.empty(), Msg.get(player, "menus.conditions.comparator", Arg.arg(Msg.get(player, condition.getComparator().getKey()))), Msg.get(player, "menus.conditions.value", Arg.arg(Msg.get(player, condition.getValue().getTranslationKey()))), Msg.get(player, "menus.conditions.target", Arg.arg(condition.getTarget())), Component.empty(), Msg.get(player, "favicons.remove"), Msg.get(player, "favicons.edit")).build();

            buttons.add(Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    action.removeCondition(condition);
                    menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
                } else {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.editingConditionals.put(p.getUniqueId(), condition.clone());
                    plugin.originalEditingConditionals.put(p.getUniqueId(), condition);
                    menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
                }
            })));
        }
        buttons.add(newCondition(action, player));
        return buttons;
    }

    public static Button newCondition(Action action, Player player) {
        ItemStack i = ItemBuilder.modern(LILY_PAD).setDisplay(Msg.get(player, "menus.conditions.new_condition")).build();
        return Button.clickable(i, new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toggleConditionMode(Action action, Player player) {
        boolean isAll = action.getSelector() == Selector.ALL;
        ItemStack i = ItemBuilder.modern(isAll ? GREEN_CANDLE : RED_CANDLE).setDisplay(Msg.get(player, "menus.conditions.mode.toggle")).setLore(isAll ? Msg.get(player, "menus.conditions.mode.all") : Msg.get(player, "menus.conditions.mode.one")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            action.setSelector(isAll ? Selector.ONE : Selector.ALL);
            menuView.replaceButton(35, toggleConditionMode(action, p));
        }));
    }

    public static Button toggleTextConditionInversion(TextCondition cond, Player player) {
        boolean flag = cond.isInverted();
        ItemStack i = ItemBuilder.modern(flag ? GREEN_CANDLE : RED_CANDLE).setDisplay(Msg.get(player, "menus.conditions.invert.toggle")).setLore(Msg.lore(player.locale(), "menus.conditions.inverted." + flag)).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            cond.setInverted(!cond.isInverted());
            menuView.replaceButton(16, toggleTextConditionInversion(cond, p));
        }));
    }

    public static Button toCondition(Player player) {
        ItemStack i = ItemBuilder.modern(ARROW).setDisplay(Msg.get(player, "items.go_back")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button editConditions(Player player) {
        ItemStack i = ItemBuilder.modern(COMPARATOR).setDisplay(Msg.get(player, "menus.action_customizer.conditions")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button numeric(Player player) {
        ItemStack i = ItemBuilder.modern(POPPED_CHORUS_FRUIT).setDisplay(Msg.get(player, "menus.conditions.new.numeric")).setLore(Msg.lore(player.locale(), "menus.conditions.new.numeric.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new NumericCondition(Comparator.EQUAL_TO, Condition.Value.EXP_LEVELS, 0.0);
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }

    public static Button text(Player player) {
        ItemStack i = ItemBuilder.modern(WRITTEN_BOOK).setDisplay(Msg.get(player, "menus.conditions.new.text")).setLore(Msg.lore(player.locale(), "menus.conditions.new.text.description")).addFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new TextCondition(Comparator.EQUAL_TO, Condition.Value.USERNAME, "test", false);
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }

    public static Button booleanCondition(Player player) {
        ItemStack i = ItemBuilder.modern(COMPARATOR).setDisplay(Msg.get(player, "menus.conditions.new.logical")).setLore(Msg.lore(player.locale(), "menus.conditions.new.logical.description")).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new BooleanCondition(Comparator.EQUAL_TO, Condition.Value.GAMEMODE, "CREATIVE");
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }
}
