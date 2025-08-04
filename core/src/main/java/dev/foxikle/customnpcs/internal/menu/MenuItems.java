/*
 * Copyright (c) 2024. Foxikle
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
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.actions.conditions.LogicalCondition;
import dev.foxikle.customnpcs.actions.conditions.NumericCondition;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.runnables.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Material.*;


public class MenuItems {
    public static final Button MENU_GLASS;
    private static final CustomNPCs plugin = CustomNPCs.getInstance();

    static {
        MENU_GLASS = Button.clickable(ItemBuilder.modern(Material.BLACK_STAINED_GLASS_PANE).setDisplay(Component.text(" ")).build(),
                ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button changeLines(InternalNpc npc, Player player) {

        Component lines = Component.empty();

        for (int i = 0; i < npc.getSettings().getHolograms().length; i++) {
            Component holo = npc.getSettings().getHolograms()[i];
            lines = lines.append(Msg.format("   <dark_gray>" + (i + 1) + ". ").append(holo)).append(Component.newline());
        }

        Component[] lore = Msg.vlore(player.locale(), "customnpcs.menus.main.items.name.current_name", 100, lines);

        return Button.clickable(ItemBuilder.modern(Material.NAME_TAG)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.items.name.name"))
                .setLore(lore)
                .build(), new OpenButtonAction(MenuUtils.NPC_HOLOGRAMS));
    }

    public static Button resilient(InternalNpc npc, Player player) {
        ItemStack i = ItemBuilder.modern(Material.BELL)
                .setLore(npc.getSettings().isResilient() ? Msg.translate(player.locale(), "customnpcs.menus.main.items.resilient.true") : Msg.translate(player.locale(), "customnpcs.menus.main.items.resilient.false"))
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.items.resilient.change"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            if (npc.getSettings().isResilient())
                p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.main.resilient.message.now_false"));
            else p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.main.resilient.message.now_true"));

            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            menuView.replaceButton(22, MenuItems.resilient(npc, p));
        }));
    }

    public static ItemStack skinSelection(InternalNpc npc, Player player) {
        return ItemBuilder.modern(Material.PLAYER_HEAD).modifyMeta(SkullMeta.class, skullMeta -> {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9";
                    profile.setProperty(new ProfileProperty("textures", texture));
                    skullMeta.setPlayerProfile(profile);
                })
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.main.items.skin.lore", Component.text(npc.getSettings().getSkinName())))
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.items.skin.name"))
                .build();
    }

    public static ItemStack extraSettings(Player player) {
        return ItemBuilder.modern(Material.COMPARATOR)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.settings.name"))
                .build();
    }

    public static ItemStack deleteNpc(Player player) {
        return ItemBuilder.modern(Material.LAVA_BUCKET)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.delete.name"))
                .build();
    }

    public static ItemStack looking(Player player) {
        return ItemBuilder.modern(Material.ENDER_EYE)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.facing.name"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.main.facing.description"))
                .build();
    }

    public static Button interactable(InternalNpc npc, Player player) {
        boolean interactable = npc.getSettings().isInteractable();

        return Button.clickable(ItemBuilder.modern(interactable ? Material.OAK_SAPLING : Material.DEAD_BUSH)
                .setLore(interactable ? Msg.translate(player.locale(), "customnpcs.menus.main.interactable.true") : Msg.translate(player.locale(), "customnpcs.menus.main.interactable.false"))
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.interactable.name.toggle"))
                .build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (npc.getSettings().isInteractable())
                p.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.main.interactable.message.now_false"));
            else p.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.main.interactable.message.now_true"));

            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            menuView.replaceButton(25, MenuItems.interactable(npc, p));
            menuView.replaceButton(34, MenuItems.showActions(npc, p));
        }));
    }

    public static Button showActions(InternalNpc npc, Player player) {
        if (!npc.getSettings().isInteractable()) return MENU_GLASS;
        return Button.clickable(ItemBuilder.modern(Material.RECOVERY_COMPASS)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.interactable.name"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.main.interactable.description"))
                .build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static ItemStack editEquipment(InternalNpc npc, Player player) {
        Equipment equip = npc.getEquipment();
        return ItemBuilder.modern(Material.ARMOR_STAND)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.name"))
                .setLore(Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.main_hand", equip.getHand().getType().name().toLowerCase()),
                        Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.off_hand", equip.getOffhand().getType().name().toLowerCase()),
                        Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.helmet", equip.getHead().getType().name().toLowerCase()),
                        Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.chestplate", equip.getChest().getType().name().toLowerCase()),
                        Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.leggings", equip.getLegs().getType().name().toLowerCase()),
                        Msg.translate(player.locale(), "customnpcs.menus.main.items.equipment.boots", equip.getBoots().getType().name().toLowerCase())
                ).build();
    }

    public static Button tunnelVision(InternalNpc npc, Player player) {
        return Button.clickable(ItemBuilder.modern(Material.SPYGLASS)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.vision.name"))
                .setLore(npc.getSettings().isTunnelvision() ? Msg.translate(player.locale(), "customnpcs.menus.main.vision.tunnel") : Msg.translate(player.locale(), "customnpcs.menus.main.vision.normal"))
                .build(), ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            menuView.replaceButton(28, MenuItems.tunnelVision(npc, p));
        }));
    }

    public static ItemStack confirmCreation(Player player) {
        return ItemBuilder.modern(Material.LIME_DYE)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.create.name"))
                .build();
    }

    public static ItemStack cancelCreation(Player player) {
        return ItemBuilder.modern(BARRIER)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.cancel.name"))
                .build();
    }

    public static ItemStack importArmor(Player player) {
        return ItemBuilder.modern(Material.ARMOR_STAND)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.import"))
                .setLore(Msg.translate(player.locale(), "customnpcs.menus.equipment.import.description"))
                .build();
    }

    public static Button helmetSlot(InternalNpc npc, Player player) {
        ItemStack helm = npc.getEquipment().getHead();
        if (helm == null || helm.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.helmet.empty"))
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.helmet.change"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == Material.AIR || event.isRightClick()) return;
                        p.playSound(p, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                        npc.getEquipment().setHead(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.helmet.message.success", Component.text(npc.getEquipment().getHead().getType().name().toLowerCase())));
                        menuView.replaceButton(13, helmetSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.helmet.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(helm)
                            .setDisplay(Component.text(helm.getType().name().toLowerCase(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();

                        if (event.isRightClick()) {
                            npc.getEquipment().setHead(new ItemStack(Material.AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.helmet.reset"));
                            menuView.replaceButton(13, helmetSlot(npc, p));
                        } else {
                            if (event.getCursor().getType() == Material.AIR) return;
                            npc.getEquipment().setHead(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.helmet.message.success", Component.text(npc.getEquipment().getHead().getType().name().toLowerCase())));
                            menuView.replaceButton(13, helmetSlot(npc, p));
                        }
                    }));

        }
    }

    public static Button chestplateSlot(InternalNpc npc, Player player) {
        ItemStack cp = npc.getEquipment().getChest();
        if (cp.getType().isAir()) {

            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.chestplate.change"))
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.chestplate.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        Player p = (Player) event.getWhoClicked();
                        event.setCancelled(true);
                        if (event.getCursor().getType().name().contains("CHESTPLATE") || event.getCursor().getType() == Material.ELYTRA) {
                            npc.getEquipment().setChest(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.chestplate.message.success", npc.getEquipment().getChest().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.chestplate.message.error"));
                        }

                        menuView.replaceButton(22, chestplateSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.chestplate.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(cp)
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(cp.getType().toString(), NamedTextColor.GREEN))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setChest(new ItemStack(AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.chestplate.reset"));
                            menuView.replaceButton(22, chestplateSlot(npc, p));
                            return;
                        } else if (event.getCursor().getType().name().contains("CHESTPLATE") || event.getCursor().getType() == Material.ELYTRA) {
                            npc.getEquipment().setChest(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.chestplate.message.success", npc.getEquipment().getChest().getType().name().toLowerCase()));
                            return;
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                        }
                        event.setCancelled(true);
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.chestplate.message.error"));
                        menuView.replaceButton(22, chestplateSlot(npc, p));
                    }));
        }
    }

    public static Button leggingsSlot(InternalNpc npc, Player player) {
        ItemStack legs = npc.getEquipment().getLegs();
        if (legs.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.legs.change"))
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.legs.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setLegs(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.legs.message.success", npc.getEquipment().getLegs().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.legs.message.error"));
                        }
                        menuView.replaceButton(31, leggingsSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.legs.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(legs)
                            .addFlags(ItemFlag.values())
                            .setDisplay(Component.text(legs.getType().toString(), NamedTextColor.GREEN))
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();

                        if (event.isRightClick()) {
                            npc.getEquipment().setLegs(new ItemStack(AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.legs.reset"));
                            menuView.replaceButton(31, leggingsSlot(npc, player));
                        } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setLegs(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.legs.message.success", npc.getEquipment().getLegs().getType().name().toLowerCase()));
                            menuView.replaceButton(31, leggingsSlot(npc, p));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.legs.message.error"));
                        }
                    }));
        }
    }

    public static Button bootsSlot(InternalNpc npc, Player player) {
        ItemStack boots = npc.getEquipment().getBoots();
        if (boots.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(LIME_STAINED_GLASS_PANE)
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.boots.empty"))
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.boots.change"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.getCursor().getType().name().contains("BOOTS")) {
                            npc.getEquipment().setBoots(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.boots.message.success", npc.getEquipment().getBoots().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.boots.message.error"));
                            return;
                        }
                        menuView.replaceButton(40, bootsSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.boots.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(boots)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(boots.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setBoots(new ItemStack(AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.boots.reset"));
                        } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setBoots(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.boots.message.success", npc.getEquipment().getBoots().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.boots.message.error"));
                            return;
                        }
                        menuView.replaceButton(40, bootsSlot(npc, p));
                    }));
        }
    }

    public static Button handSlot(InternalNpc npc, Player player) {
        ItemStack hand = npc.getEquipment().getHand();
        if (hand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.hand.change"))
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.hand.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == AIR) return;
                        npc.getEquipment().setHand(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.hand.message.success", npc.getEquipment().getHand().getType().name().toLowerCase()));
                        menuView.replaceButton(23, handSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.hand.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(hand)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(hand.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setHand(new ItemStack(AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.hand.reset"));
                            menuView.replaceButton(23, handSlot(npc, p));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            npc.getEquipment().setHand(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.hand.message.success", npc.getEquipment().getHand().getType().name().toLowerCase()));
                            menuView.replaceButton(23, handSlot(npc, p));
                        }
                    }));
        }
    }

    public static Button offhandSlot(InternalNpc npc, Player player) {
        ItemStack offhand = npc.getEquipment().getOffhand();
        if (offhand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE)
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.equipment.offhand.empty"))
                            .setLore(Msg.lore(player.locale(), "customnpcs.menus.equipment.offhand.change"))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == AIR) return;
                        npc.getEquipment().setOffhand(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.offhand.message.success", npc.getEquipment().getOffhand().getType().name().toLowerCase()));
                        menuView.replaceButton(21, offhandSlot(npc, p));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "customnpcs.menus.equipment.hand.change"));
            lore.add(Msg.translate(player.locale(), "customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(offhand)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(offhand.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setOffhand(new ItemStack(AIR));
                            p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.offhand.reset"));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            npc.getEquipment().setOffhand(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.equipment.offhand.message.success", npc.getEquipment().getOffhand().getType().name().toLowerCase()));
                        }
                        menuView.replaceButton(21, offhandSlot(npc, p));
                    }));
        }
    }

    public static Button toMain(Player player) {
        return Button.clickable(ItemBuilder.modern(BARRIER).setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_MAIN));
    }

    public static Button toPose(Player player) {
        return Button.clickable(ItemBuilder.modern(SNIFFER_EGG).setDisplay(Msg.translate(player.locale(), "customnpcs.pose.pose_editor")).build(),
                new OpenButtonAction(MenuUtils.NPC_POSE));
    }

    public static Button toAction(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static Button toActionSaveConditions(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back")).build(),
                ButtonClickAction.plain((menu, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.getLotus().openMenu(p, MenuUtils.NPC_ACTION_CUSTOMIZER);
                }));
    }

    public static Button toNewCondition(Player player) {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toConditionCustomizer(Player player) {
        return Button.clickable(ItemBuilder.modern(COMPARATOR).setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.conditions")).build(),
                new OpenButtonAction(MenuUtils.NPC_CONDITION_CUSTOMIZER));
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
                    if (CustomNPCs.ACTION_REGISTRY.canEdit(action.getClass())) {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                        plugin.editingActions.put(p.getUniqueId(), action.clone());
                        plugin.originalEditingActions.put(p.getUniqueId(), action);
                        plugin.getLotus().openMenu(p, MenuUtils.NPC_ACTION_CUSTOMIZER);
                    } else {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.edit.fail"));
                    }
                }
            })));
        }

        buttons.add(Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.actions.new"))
                        .build(),
                new OpenButtonAction(MenuUtils.NPC_NEW_ACTION)
        ));

        return buttons;
    }

    public static List<Button> currentLines(InternalNpc npc, Player player) {
        List<Button> buttons = new ArrayList<>();

        String[] raw = npc.getSettings().getRawHolograms();
        List<String> mutable = Utils.list(raw);
        for (int i = 0; i < raw.length; i++) {
            String line = raw[i];
            List<Component> lore = Utils.list(
                    Msg.format(line), Component.empty(),
                    Msg.translate(player.locale(), "customnpcs.menus.holograms.edit"),
                    Msg.translate(player.locale(), "customnpcs.menus.holograms.delete")
            );
            boolean canMoveDown = i < raw.length - 1;
            boolean canMoveUp = i > 0;

            if (canMoveDown) lore.add(Msg.translate(player.locale(), "customnpcs.menus.holograms.move_down"));
            if (canMoveUp) lore.add(Msg.translate(player.locale(), "customnpcs.menus.holograms.move_up"));

            int finalI = i;
            buttons.add(Button.clickable(ItemBuilder.modern(PAPER)
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.holograms.line", (i + 1)))
                            .setLore(lore).build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();

                        // DROP is delete
                        // DROP_STACK is delete without confirmation
                        // SWAP TO OFFHAND is edit
                        // LEFT is up
                        // RIGHT is down

                        if (event.getClick() == ClickType.DROP) {
                            HologramMenu.editingIndicies.put(p.getUniqueId(), finalI);
                            plugin.getLotus().openMenu(p, MenuUtils.NPC_DELETE_LINE);
                            return;
                        }
                        if (event.getClick() == ClickType.CONTROL_DROP) {
                            mutable.remove(finalI);
                            player.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1F, 1F);
                            npc.getSettings().setRawHolograms(mutable.toArray(new String[0]));
                            plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                            return;
                        }
                        if (event.getClick() == ClickType.SWAP_OFFHAND) {
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.data.name.title"));

                            plugin.wait(p, WaitingType.NAME);

                            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                            HologramMenu.editingIndicies.put(p.getUniqueId(), finalI);
                            new NameRunnable(p, plugin).runTaskTimer(plugin, 1, 15);
                            p.closeInventory();

                            if (plugin.getConfig().getBoolean("NameReferenceMessages")) {
                                p.sendMessage(Msg.translate(p.locale(), "customnpcs.name.reference"));
                                p.sendMessage(line);
                                p.sendMessage(Msg.translate(p.locale(), "customnpcs.name.toggle_reference_message"));
                            }
                            return;
                        }

                        if (event.isLeftClick()) {
                            if (!canMoveUp) {
                                p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.holograms.move_up_fail"));
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                                return;
                            }

                            Collections.swap(mutable, finalI, finalI - 1);
                            npc.getSettings().setRawHolograms(mutable.toArray(new String[0]));
                            plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                            p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, .7F, .9F);
                            return;
                        }

                        if (!canMoveDown) {
                            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.holograms.move_down_fail"));
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                            return;
                        }

                        Collections.swap(mutable, finalI, finalI + 1);
                        npc.getSettings().setRawHolograms(mutable.toArray(new String[0]));
                        plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                        p.playSound(p.getLocation(), Sound.BLOCK_PISTON_CONTRACT, .7F, .9F);
                    })));
        }

        buttons.add(Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.holograms.new_line"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();

                    plugin.wait(p, WaitingType.NAME);

                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    HologramMenu.editingIndicies.put(p.getUniqueId(), raw.length);
                    new NameRunnable(p, plugin).runTaskTimer(plugin, 1, 15);
                    p.closeInventory();
                })
        ));

        return buttons;
    }

    public static Button delayDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(CLOCK)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.delay.name", action.getDelay()))
                .build(), ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button decrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(RED_DYE)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.delay.decrement"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.decrement.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    if (action.getDelay() == 0) {
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.action_customizer.delay.error"));
                        return;
                    }
                    if (event.isShiftClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 20));
                    } else if (event.isLeftClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 1));
                    } else if (event.isRightClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 5));
                    }
                    menuView.updateButton(1, button -> button.setItem(delayDisplay(action, p).getItem()));
                }));
    }

    public static Button incrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LIME_DYE)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.delay.increment"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.increment.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    if (event.isShiftClick()) {
                        action.setDelay(action.getDelay() + 20);
                    } else if (event.isLeftClick()) {
                        action.setDelay(action.getDelay() + 1);
                    } else if (event.isRightClick()) {
                        action.setDelay(action.getDelay() + 5);
                    }
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    menuView.updateButton(1, button -> button.setItem(delayDisplay(action, p).getItem()));
                }));
    }

    public static Button cooldownDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(CLOCK)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.cooldown.name", action.getCooldown()))
                .build(), ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button decrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(RED_DYE)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.cooldown.decrement"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.action_customizer.cooldown.decrement.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    if (action.getCooldown() == 0) {
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.action_customizer.cooldown.error"));
                        return;
                    }
                    if (event.isShiftClick()) {
                        action.setCooldown(Math.max(0, action.getCooldown() - 20));
                    } else if (event.isLeftClick()) {
                        action.setCooldown(Math.max(0, action.getCooldown() - 1));
                    } else if (event.isRightClick()) {
                        action.setCooldown(Math.max(0, action.getCooldown() - 5));
                    }
                    menuView.updateButton(7, button -> button.setItem(cooldownDisplay(action, p).getItem()));
                }));
    }

    public static Button incrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LIME_DYE)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.cooldown.increment"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.action_customizer.cooldown.increment.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    if (event.isShiftClick()) {
                        action.setCooldown(action.getCooldown() + 20);
                    } else if (event.isLeftClick()) {
                        action.setCooldown(action.getCooldown() + 1);
                    } else if (event.isRightClick()) {
                        action.setCooldown(action.getCooldown() + 5);
                    }
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    menuView.updateButton(7, button -> button.setItem(cooldownDisplay(action, p).getItem()));
                }));
    }

    public static Button saveAction(Action action, Player player) {
        return Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.confirm"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    InternalNpc npc = plugin.getEditingNPCs().getIfPresent(p.getUniqueId());

                    if (npc == null) {
                        p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.main.error.no_npc.lore"));
                        return;
                    }

                    if (CustomNPCs.getInstance().originalEditingActions.get(p.getUniqueId()) != null)
                        npc.removeAction(CustomNPCs.getInstance().originalEditingActions.remove(p.getUniqueId()));
                    npc.addAction(action);

                    menuView.getAPI().openMenu(p, MenuUtils.NPC_ACTIONS);
                }));
    }

    public static ItemStack genericDisplay(Component text, Component... lore) {
        return ItemBuilder.modern(CLOCK).setDisplay(text).setLore(lore).build();
    }

    public static Button saveCondition(Player player) {
        return Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.create.name"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
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

    public static Button comparatorSwitcher(Condition condition, Player player) {

        List<Component> lore = new ArrayList<>();
        for (Condition.Comparator c : Condition.Comparator.values()) {
            if (condition.getType() == Condition.Type.NUMERIC || (condition.getType() == Condition.Type.LOGICAL && c.isStrictlyLogical())) {
                if (condition.getComparator() != c)
                    lore.add(Msg.translate(player.locale(), c.getKey()).color(NamedTextColor.GREEN));
                else
                    lore.add(Component.text("▸ ", NamedTextColor.DARK_AQUA).append(Msg.translate(player.locale(), c.getKey())));
            }
        }
        lore.add(Msg.translate(player.locale(), "customnpcs.items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.comparator"))
                .setLore(lore.toArray(new Component[]{}))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            List<Condition.Comparator> comparators = new ArrayList<>();
            for (Condition.Comparator value : Condition.Comparator.values()) {
                if (condition.getType() == Condition.Type.LOGICAL && !value.isStrictlyLogical()) {
                    continue;
                }
                comparators.add(value);
            }
            int index = comparators.indexOf(condition.getComparator());
            if (event.isLeftClick()) {
                if (comparators.size() > (index + 1)) {
                    condition.setComparator(comparators.get(index + 1));
                } else {
                    condition.setComparator(comparators.get(0));
                }
            } else if (event.isRightClick()) {
                if (index == 0) {
                    condition.setComparator(comparators.get(comparators.size() - 1));
                } else {
                    condition.setComparator(comparators.get(index - 1));
                }
            }
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.replaceButton(11, comparatorSwitcher(condition, p));
        }));
    }

    public static Button targetValueSelector(Condition condition, Player player) {
        ItemStack i = ItemBuilder.modern(OAK_HANGING_SIGN)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.value.select"))
                .setLore(Msg.translate(player.locale(), "customnpcs.value.current", condition.getTarget()),
                        Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.TARGET);
            new TargetInputRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
        }));
    }

    public static Button valueSwitcher(Condition condition, Player player) {
        List<Component> lore = new ArrayList<>();

        for (Condition.Value v : Condition.Value.values()) {
            //todo: re-evaluate this
            if (v.isLogical() && condition.getType() != Condition.Type.LOGICAL) continue;
            if (!v.isLogical() && condition.getType() != Condition.Type.NUMERIC) continue;


            if (condition.getValue() != v)
                lore.add(Msg.translate(player.locale(), v.getTranslationKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Component.text("▸ ", NamedTextColor.DARK_AQUA).append(Msg.translate(player.locale(), v.getTranslationKey())));

        }
        lore.add(Msg.translate(player.locale(), "customnpcs.items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.statistic"))
                .setLore(lore.toArray(new Component[]{}))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            List<Condition.Value> statistics = new ArrayList<>();
            for (Condition.Value value : Condition.Value.values()) {
                if (condition.getType() == Condition.Type.LOGICAL) {
                    if (value.isLogical()) statistics.add(value);
                } else if (!value.isLogical()) statistics.add(value);
            }

            int index = statistics.indexOf(condition.getValue());
            if (event.isLeftClick()) {
                if (statistics.size() > (index + 1)) {
                    condition.setValue(statistics.get(index + 1));
                } else {
                    condition.setValue(statistics.get(0));
                }
            } else if (event.isRightClick()) {
                if (index == 0) {
                    condition.setValue(statistics.get(statistics.size() - 1));
                } else {
                    condition.setValue(statistics.get(index - 1));
                }
            }
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.updateButton(15, button -> button.setItem(valueSwitcher(condition, player).getItem()));
        }));
    }

    public static Button interactableHologram(InternalNpc npc, Player player) {
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.modern(hideClickableTag ? RED_CANDLE : GREEN_CANDLE)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.extra.hologram_visibility"))
                .setLore(
                        Component.empty(),
                        Msg.translate(player.locale(), "customnpcs.menus.extra.hologram_visibility.description"),
                        hideClickableTag ? Msg.translate(player.locale(), "customnpcs.menus.extra.hologram_visibility.description.hidden") : Msg.translate(player.locale(), "customnpcs.menus.extra.hologram_visibility.description.shown")
                ).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setHideClickableHologram(!hideClickableTag);
            menuView.replaceButton(11, interactableHologram(npc, p));
        }));
    }

    public static Button interactableText(Player player) {
        ItemStack i = ItemBuilder.modern(NAME_TAG)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.extra.hologram_text"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.extra.hologram_text.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            plugin.wait(p, WaitingType.HOLOGRAM);

            p.closeInventory();
            p.sendMessage(Msg.translate(p.locale(), "customnpcs.menus.extra.hologram_text.type"));
            new InteractableHologramRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
        }));
    }

    public static Button upsideDown(InternalNpc npc, Player player) {
        boolean upsideDown = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.modern(upsideDown ? RED_CANDLE : GREEN_CANDLE)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.extra.upside_down"))
                .setLore(
                        Component.empty(),
                        Msg.translate(player.locale(), "customnpcs.menus.extra.upside_down.description"),
                        upsideDown ? Msg.translate(player.locale(), "customnpcs.menus.extra.upside_down.description.false") : Msg.translate(player.locale(), "customnpcs.menus.extra.upside_down.description.true")
                ).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setUpsideDown(!upsideDown);
            menuView.replaceButton(15, upsideDown(npc, p));
        }));
    }

    public static Button importPlayer(Player player) {
        ItemStack i = ItemBuilder.modern(ANVIL)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.skins.player"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.skins.player.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();

            plugin.wait(p, WaitingType.PLAYER);
            new PlayerNameRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);
        }));
    }

    public static Button useCatalog(Player player) {
        ItemStack i = ItemBuilder.modern(ARMOR_STAND)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.skins.catalog"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.skins.catalog.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            try {
                CustomNPCs.getInstance().getSkinCatalog(p).open(p);
            } catch (InvalidPageException e) {
                p.sendMessage(Msg.translate(p.locale(), "customnpcs.error.cant_open_skin_catalog"));
                CustomNPCs.getInstance().getLogger().log(Level.SEVERE, "An error occurred whilst opening the Skin Catalog!", e);
            }
        }));
    }

    public static Button importUrl(Player player) {
        ItemStack i = ItemBuilder.modern(WRITABLE_BOOK)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.skins.url"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.skins.url.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();

            plugin.wait(p, WaitingType.URL);
            new UrlRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);
        }));
    }

    public static List<Button> conditions(Action action, Player player) {
        List<Button> buttons = new ArrayList<>();

        if (action.getConditions() == null) {
            return Utils.list(newCondition(action, player));
        }

        for (Condition condition : action.getConditions()) {
            boolean logical = condition.getType() == Condition.Type.LOGICAL;
            ItemStack i = ItemBuilder.modern(logical ? COMPARATOR : POPPED_CHORUS_FRUIT)
                    .setDisplay(logical ? Msg.translate(player.locale(), "customnpcs.menus.conditions.logical") : Msg.translate(player.locale(), "customnpcs.menus.conditions.numeric"))
                    .setLore(
                            Component.empty(),
                            Msg.translate(player.locale(), "customnpcs.menus.conditions.comparator", Msg.translate(player.locale(), condition.getComparator().getKey())),
                            Msg.translate(player.locale(), "customnpcs.menus.conditions.value", Msg.translate(player.locale(), condition.getValue().getTranslationKey())),
                            Msg.translate(player.locale(), "customnpcs.menus.conditions.target", condition.getTarget()),
                            Component.empty(),
                            Msg.translate(player.locale(), "customnpcs.favicons.remove"),
                            Msg.translate(player.locale(), "customnpcs.favicons.edit")
                    ).build();

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
        ItemStack i = ItemBuilder.modern(LILY_PAD)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.conditions.new_condition"))
                .build();
        return Button.clickable(i, new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toggleConditionMode(Action action, Player player) {
        boolean isAll = action.getMode() == Condition.SelectionMode.ALL;
        ItemStack i = ItemBuilder.modern(isAll ? GREEN_CANDLE : RED_CANDLE)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.conditions.mode.toggle"))
                .setLore(isAll ? Msg.translate(player.locale(), "customnpcs.menus.conditions.mode.all") : Msg.translate(player.locale(), "customnpcs.menus.conditions.mode.one"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            action.setMode(isAll ? Condition.SelectionMode.ONE : Condition.SelectionMode.ALL);
            menuView.replaceButton(35, toggleConditionMode(action, p));
        }));
    }

    public static Button toCondition(Player player) {
        ItemStack i = ItemBuilder.modern(ARROW)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.items.go_back"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button editConditions(Player player) {
        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.conditions"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button numeric(Player player) {
        ItemStack i = ItemBuilder.modern(POPPED_CHORUS_FRUIT)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.conditions.new.numeric"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.conditions.new.numeric.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new NumericCondition(Condition.Comparator.EQUAL_TO, Condition.Value.EXP_LEVELS, 0.0);
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }

    public static Button logic(Player player) {
        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.conditions.new.logical"))
                .setLore(Msg.lore(player.locale(), "customnpcs.menus.conditions.new.logical.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new LogicalCondition(Condition.Comparator.EQUAL_TO, Condition.Value.GAMEMODE, "CREATIVE");
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.getAPI().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }
}
