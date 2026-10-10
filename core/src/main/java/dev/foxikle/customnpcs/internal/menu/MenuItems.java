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
import dev.foxikle.customnpcs.internal.utils.*;
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
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.slot.Slot;

import java.util.*;
import java.util.logging.Level;

import static org.bukkit.Material.*;


public class MenuItems {
    public static final Button MENU_GLASS;
    private static final CustomNPCs plugin = CustomNPCs.getInstance();

    static {
        MENU_GLASS = Button.clickable(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
                        .displayName(Component.text(" ")).build(),
                (_, event) -> event.setCancelled(true));
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

        return Button.clickable(ItemBuilder.of(Material.NAME_TAG).displayName(Msg.get(player, "menus.main.items.name.name")).lore(lore).build(), new OpenButtonAction(MenuUtils.NPC_HOLOGRAMS));
    }

    public static Button resilient(InternalNpc npc, Player player) {
        ItemStack i = ItemBuilder.of(Material.BELL)
                .lore(npc.getSettings().isResilient() ? Msg.get(player, "menus.main.items.resilient.true") : Msg.get(player, "menus.main.items.resilient.false"))
                .displayName(Msg.get(player, "menus.main.items.resilient.change"))
                .build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            if (npc.getSettings().isResilient()) p.sendMessage(Msg.get(p, "menus.main.resilient.message.now_false"));
            else p.sendMessage(Msg.get(p, "menus.main.resilient.message.now_true"));

            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            menuView.content().set(Slot.of(22), MenuItems.resilient(npc, p));
        });
    }

    public static ItemStack skinSelection(InternalNpc npc, Player player) {
        Object currentSkin = npc.getSettings().getSkinName();
        if (npc.getSettings().getSkinName().isBlank()) {
            currentSkin = Msg.get(player, "menus.main.items.skin.default");
        }
        return ItemBuilder.of(Material.PLAYER_HEAD)
                .editMeta(SkullMeta.class, skullMeta -> {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9";
                    profile.setProperty(new ProfileProperty("textures", texture));
                    skullMeta.setPlayerProfile(profile);
                }).lore(Msg.lore(player.locale(), "menus.main.items.skin.lore", Arg.arg(currentSkin)))
                .displayName(Msg.get(player, "menus.main.items.skin.name")).build();
    }

    public static ItemStack extraSettings(Player player) {
        return ItemBuilder.of(Material.COMPARATOR).displayName(Msg.get(player, "menus.main.settings.name")).build();
    }

    public static ItemStack deleteNpc(Player player) {
        return ItemBuilder.of(Material.LAVA_BUCKET).displayName(Msg.get(player, "menus.main.delete.name")).build();
    }

    public static ItemStack looking(Player player) {
        return ItemBuilder.of(Material.ENDER_EYE).displayName(Msg.get(player, "menus.main.facing.name")).lore(Msg.lore(player.locale(), "menus.main.facing.description")).build();
    }

    public static Button interactable(InternalNpc npc, Player player) {
        boolean interactable = npc.getSettings().isInteractable();

        return Button.clickable(ItemBuilder.of(interactable ? Material.OAK_SAPLING : Material.DEAD_BUSH).lore(interactable ? Msg.get(player, "menus.main.interactable.true") : Msg.get(player, "menus.main.interactable.false")).displayName(Msg.get(player, "menus.main.interactable.name.toggle")).build(), (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (npc.getSettings().isInteractable())
                p.sendMessage(Msg.get(player, "menus.main.interactable.message.now_false"));
            else p.sendMessage(Msg.get(player, "menus.main.interactable.message.now_true"));

            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            menuView.content().set(Slot.of(25), MenuItems.interactable(npc, p));
            menuView.content().set(Slot.of(34), MenuItems.showActions(npc, p));
        });
    }

    public static Button showActions(InternalNpc npc, Player player) {
        if (!npc.getSettings().isInteractable()) return MENU_GLASS;
        return Button.clickable(ItemBuilder.of(Material.RECOVERY_COMPASS).displayName(Msg.get(player, "menus.main.interactable.name")).lore(Msg.lore(player.locale(), "menus.main.interactable.description")).build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static ItemStack editEquipment(InternalNpc npc, Player p) {
        Equipment equip = npc.getEquipment();
        return ItemBuilder.of(Material.ARMOR_STAND).displayName(Msg.get(p, "menus.main.items.equipment.name")).lore(Msg.get(p, "menus.main.items.equipment.main_hand", Arg.arg(equipmentName(p, equip.getHand()))), Msg.get(p, "menus.main.items.equipment.off_hand", Arg.arg(equipmentName(p, equip.getOffhand()))), Msg.get(p, "menus.main.items.equipment.helmet", Arg.arg(equipmentName(p, equip.getHead()))), Msg.get(p, "menus.main.items.equipment.chestplate", Arg.arg(equipmentName(p, equip.getChest()))), Msg.get(p, "menus.main.items.equipment.leggings", Arg.arg(equipmentName(p, equip.getLegs()))), Msg.get(p, "menus.main.items.equipment.boots", Arg.arg(equipmentName(p, equip.getBoots())))).build();
    }

    private static Component equipmentName(Player p, @Nullable ItemStack item) {
        if (item == null) {
            return Msg.get(p, "menus.main.items.equipment.empty");
        }
        return Component.translatable(item.translationKey());
    }

    public static Button tunnelVision(InternalNpc npc, Player player) {
        return Button.clickable(ItemBuilder.of(Material.SPYGLASS).displayName(Msg.get(player, "menus.main.vision.name")).lore(npc.getSettings().isTunnelvision() ? Msg.get(player, "menus.main.vision.tunnel") : Msg.get(player, "menus.main.vision.normal")).build(), (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            menuView.content().set(Slot.of(28), MenuItems.tunnelVision(npc, p));
        });
    }

    public static ItemStack confirmCreation(Player player) {
        return ItemBuilder.of(Material.LIME_DYE).displayName(Msg.get(player, "menus.main.create.name")).build();
    }

    public static ItemStack cancelCreation(Player player) {
        return ItemBuilder.of(BARRIER).displayName(Msg.get(player, "menus.main.cancel.name")).build();
    }

    public static ItemStack importArmor(Player player) {
        return ItemBuilder.of(Material.ARMOR_STAND).displayName(Msg.get(player, "menus.equipment.import")).lore(Msg.get(player, "menus.equipment.import.description")).build();
    }

    public static Button helmetSlot(InternalNpc npc, Player player) {
        ItemStack helm = npc.getEquipment().getHead();
        if (helm == null || helm.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                    .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                    .displayName(Msg.get(player, "menus.equipment.helmet.empty"))
                    .lore(Msg.lore(player.locale(), "menus.equipment.helmet.change"))
                    .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == Material.AIR || event.isRightClick()) return;
                p.playSound(p, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                npc.getEquipment().setHead(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHead()))));
                menuView.content().set(Slot.of(13), helmetSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.helmet.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(helm)
                    .displayName(Component.text(helm.getType().name().toLowerCase(), NamedTextColor.GREEN))
                    .addFlags(ItemFlag.values())
                    .lore(lore.toArray(new Component[]{}))
                    .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setHead(new ItemStack(Material.AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.reset"));
                    menuView.content().set(Slot.of(13), helmetSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == Material.AIR) return;
                    npc.getEquipment().setHead(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHead()))));
                    menuView.content().set(Slot.of(13), helmetSlot(npc, p));
                }

            });
        }
    }

    public static Button chestplateSlot(InternalNpc npc, Player player) {
        ItemStack cp = npc.getEquipment().getChest();
        if (cp == null || cp.getType().isAir()) {

            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(player.locale(), "menus.equipment.chestplate.change")).displayName(Msg.get(player, "menus.equipment.chestplate.empty")).build(), (menuView, event) -> {
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

                menuView.content().set(Slot.of(22), chestplateSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.chestplate.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(cp).addFlags(ItemFlag.values())
                    .lore(lore.toArray(new Component[]{}))
                    .displayName(Component.text(cp.getType().toString(), NamedTextColor.GREEN))
                    .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setChest(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.reset"));
                    menuView.content().set(Slot.of(22), chestplateSlot(npc, p));
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
                menuView.content().set(Slot.of(22), chestplateSlot(npc, p));
            });
        }
    }

    public static Button leggingsSlot(InternalNpc npc, Player player) {
        ItemStack legs = npc.getEquipment().getLegs();
        if (legs == null || legs.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(player.locale(), "menus.equipment.legs.change")).displayName(Msg.get(player, "menus.equipment.legs.empty")).build(), (menuView, event) -> {
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
                menuView.content().set(Slot.of(31), leggingsSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.legs.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(legs)
                    .addFlags(ItemFlag.values())
                    .displayName(Component.text(legs.getType().toString(), NamedTextColor.GREEN))
                    .lore(lore.toArray(new Component[]{})).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setLegs(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.reset"));
                    menuView.content().set(Slot.of(31), leggingsSlot(npc, player));
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getLegs()))));
                    menuView.content().set(Slot.of(31), leggingsSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.error"));
                }
            });
        }
    }

    public static Button bootsSlot(InternalNpc npc, Player player) {
        ItemStack boots = npc.getEquipment().getBoots();
        if (boots == null || boots.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(LIME_STAINED_GLASS_PANE).displayName(Msg.get(player, "menus.equipment.boots.empty")).addFlags(ItemFlag.values()).lore(Msg.lore(player.locale(), "menus.equipment.boots.change")).build(), (menuView, event) -> {
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
                menuView.content().set(Slot.of(40), bootsSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.boots.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(boots)
                    .lore(lore.toArray(new Component[]{}))
                    .displayName(Component.text(boots.getType().toString(), NamedTextColor.GREEN))
                    .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
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
                menuView.content().set(Slot.of(40), bootsSlot(npc, p));
            });
        }
    }

    public static Button handSlot(InternalNpc npc, Player player) {
        ItemStack hand = npc.getEquipment().getHand();
        if (hand == null || hand.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(YELLOW_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(player.locale(), "menus.equipment.hand.change")).displayName(Msg.get(player, "menus.equipment.hand.empty")).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setHand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHand()))));
                menuView.content().set(Slot.of(23), handSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(hand).lore(lore.toArray(new Component[]{}))
                    .displayName(Component.text(hand.getType().toString(), NamedTextColor.GREEN))
                    .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setHand(new ItemStack(AIR));
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.reset"));
                    menuView.content().set(Slot.of(23), handSlot(npc, p));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setHand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getHand()))));
                    menuView.content().set(Slot.of(23), handSlot(npc, p));
                }
            });
        }
    }

    public static Button offhandSlot(InternalNpc npc, Player player) {
        ItemStack offhand = npc.getEquipment().getOffhand();
        if (offhand == null || offhand.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(YELLOW_STAINED_GLASS_PANE).displayName(Msg.get(player, "menus.equipment.offhand.empty")).lore(Msg.lore(player.locale(), "menus.equipment.offhand.change")).addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setOffhand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                p.sendMessage(Msg.get(p, "menus.equipment.offhand.message.success", Arg.arg(equipmentName(p, npc.getEquipment().getOffhand()))));
                menuView.content().set(Slot.of(21), offhandSlot(npc, p));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(player.locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(player, "remove.description"));
            return Button.clickable(ItemBuilder.of(offhand).lore(lore.toArray(new Component[]{}))
                    .displayName(Component.text(offhand.getType().toString(), NamedTextColor.GREEN))
                    .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
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
                menuView.content().set(Slot.of(21), offhandSlot(npc, p));
            });
        }
    }

    public static Button toPose(Player player) {
        return Button.clickable(ItemBuilder.of(SNIFFER_EGG).displayName(Msg.get(player, "pose.pose_editor")).build(), new OpenButtonAction(MenuUtils.NPC_POSE));
    }

    public static Button toMain(Player player) {
        return toMain(player.locale());
    }

    public static Button toMain(Locale player) {
        return Button.clickable(ItemBuilder.of(BARRIER)
                        .displayName(Msg.get(player, "items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_MAIN));
    }

    public static Button toAction(Player player) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(player, "items.go_back")).build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static Button toActionSaveConditions(Player player) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(player, "items.go_back")).build(), (menu, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Action action = CustomNPCs.getInstance().editingActions.getOrDefault(player.getUniqueId(), null);
            if (action == null) {
                player.sendMessage(Msg.get(p, "error.npc-menu-expired"));
                return;
            }
            plugin.getLotus().openMenu(p, action.getMenu());
        });
    }

    public static Button toNewCondition(Player player) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(player, "items.go_back")).build(), new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static List<Button> currentActions(InternalNpc npc, Player player) {
        List<Button> buttons = new ArrayList<>();

        for (Action action : npc.getActions()) {
            buttons.add(Button.clickable(action.getFavicon(player), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    if (event.isShiftClick()) {
                        p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                        npc.removeAction(action);
                        menuView.lotus().openMenu(p, MenuUtils.NPC_ACTIONS);
                        return;
                    }
                    plugin.editingActions.put(p.getUniqueId(), action);
                    menuView.lotus().openMenu(p, MenuUtils.NPC_DELETE_ACTION);
                } else if (event.isLeftClick()) {
                    if (action.canEdit()) {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                        Action cloned = action.clone();
                        plugin.editingActions.put(p.getUniqueId(), cloned);
                        plugin.originalEditingActions.put(p.getUniqueId(), action);
                        plugin.getLotus().openMenu(p, cloned.getMenu());
                    } else {
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        p.sendMessage(Msg.get(p, "edit.fail"));
                    }
                }
            }));
        }

        buttons.add(Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(player, "menus.actions.new")).build(), new OpenButtonAction(MenuUtils.NPC_NEW_ACTION)));

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
            buttons.add(Button.clickable(ItemBuilder.of(PAPER).displayName(Msg.get(player, "menus.holograms.line", Arg.arg(i + 1))).lore(lore).build(), (_, event) -> {
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
            }));
        }

        buttons.add(Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(player, "menus.holograms.new_line")).build(), (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();

            plugin.wait(p, WaitingType.NAME);

            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            HologramMenu.editingIndicies.put(p.getUniqueId(), raw.size());
            p.closeInventory();
        }));

        return buttons;
    }

    public static Button delayDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(CLOCK).displayName(Msg.get(player, "menus.action_customizer.delay.name", Arg.arg(action.getDelay()))).build(), (_, event) -> event.setCancelled(true))
                ;
    }

    public static Button decrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(RED_DYE).displayName(Msg.get(player, "menus.action_customizer.delay.decrement")).lore(Msg.lore(player.locale(), "menus.action_customizer.delay.decrement.description")).build(), (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (action.getDelay() == 0) {
                p.sendMessage(Msg.get(p, "menus.action_customizer.delay.error"));
                return;
            }
            action.setDelay(Math.max(0, Utils.decrement(event).apply(action.getDelay())));
            menuView.content().set(Slot.of(4), delayDisplay(action, p));
        });
    }

    public static Button incrementDelay(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(LIME_DYE).displayName(Msg.get(player, "menus.action_customizer.delay.increment")).lore(Msg.lore(player.locale(), "menus.action_customizer.delay.increment.description")).build(), (menuView, event) -> {
            event.setCancelled(true);
            action.setDelay(Utils.increment(event).apply(action.getDelay()));

            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.content().set(Slot.of(4), delayDisplay(action, p));
        });
    }

    public static Button cooldownDisplay(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(CLOCK).displayName(Msg.get(player, "menus.action_customizer.cooldown.name", Arg.arg(action.getCooldown()))).build(), (_, event) -> event.setCancelled(true))
                ;
    }

    public static Button decrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(RED_DYE).displayName(Msg.get(player, "menus.action_customizer.cooldown.decrement")).lore(Msg.lore(player.locale(), "menus.action_customizer.cooldown.decrement.description")).build(), (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (action.getCooldown() == 0) {
                p.sendMessage(Msg.get(p, "menus.action_customizer.cooldown.error"));
                return;
            }
            action.setCooldown(Math.max(0, Utils.decrement(event).apply(action.getCooldown())));

            menuView.content().set(Slot.of(7), cooldownDisplay(action, p));
        });
    }

    public static Button incrementCooldown(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(LIME_DYE).displayName(Msg.get(player, "menus.action_customizer.cooldown.increment")).lore(Msg.lore(player.locale(), "menus.action_customizer.cooldown.increment.description")).build(), (menuView, event) -> {
            event.setCancelled(true);
            action.setCooldown(Math.max(0, Utils.increment(event).apply(action.getCooldown())));

            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.content().set(Slot.of(7), cooldownDisplay(action, p));
        });
    }

    public static Button saveAction(Action action, Player player) {
        return Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(player, "menus.action_customizer.confirm")).build(), (menuView, event) -> {
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

            menuView.lotus().openMenu(p, MenuUtils.NPC_ACTIONS);
        });
    }

    public static Button display(Component text, Component... lore) {
        return Button.clickable(ItemBuilder.of(CLOCK).displayName(text).lore(lore).build(),
                (v, e) -> e.setCancelled(true));
    }

    public static Button saveCondition(Player player) {
        return Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(player, "menus.main.create.name")).build(), (menuView, event) -> {
            event.setCancelled(true);

            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Action actionImpl = plugin.editingActions.get(p.getUniqueId());
            Condition original = plugin.originalEditingConditionals.get(p.getUniqueId());
            if (original != null) actionImpl.removeCondition(original);
            Condition edited = plugin.editingConditionals.get(p.getUniqueId());
            actionImpl.addCondition(edited);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITIONS);
        });
    }

    public static Button comparatorSwitcher(Condition condition, Player player, int slot) {

        List<Component> lore = new ArrayList<>();
        for (Comparator c : Comparator.getSupportedConditions(condition)) {
            if (condition.getComparator() != c) lore.add(Msg.get(player, c.getKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(player, c.getKey()).color(NamedTextColor.DARK_AQUA)));
        }
        lore.add(Msg.get(player, "items.click_to_change"));

        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(player, "comparator"))
                .lore(lore.toArray(new Component[]{})).build();

        return Button.clickable(i, (menuView, event) -> {
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
            menuView.content().set(Slot.of(slot), comparatorSwitcher(condition, p, slot));
        });
    }

    public static Button targetValueSelector(Condition condition, Player player) {
        if (!condition.getValue().hasInput) {
            return MENU_GLASS;
        }

        ItemStack i = ItemBuilder.of(OAK_HANGING_SIGN).displayName(Msg.get(player, "value.select")).lore(Msg.get(player, "value.current", Arg.arg(condition.getTarget())), Msg.get(player, "items.click_to_change")).build();

        return Button.clickable(i, (_, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.TARGET);
        });
    }

    public static Button valueSwitcher(Condition condition, Player player, int slot, int inputSlot) {
        List<Component> lore = new ArrayList<>();

        for (Condition.Value v : Condition.Value.getSupportedConditions(condition)) {

            if (condition.getValue() != v) lore.add(Msg.get(player, v.getTranslationKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(player, v.getTranslationKey()).color(NamedTextColor.DARK_AQUA)));

        }
        lore.add(Msg.get(player, "items.click_to_change"));

        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(player, "statistic"))
                .lore(lore.toArray(new Component[]{})).build();

        return Button.clickable(i, (menuView, event) -> {
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
            menuView.content().set(Slot.of(inputSlot), targetValueSelector(condition, player));
            menuView.content().set(Slot.of(slot), valueSwitcher(condition, player, slot, inputSlot));
        });
    }

    public static Button interactableHologram(InternalNpc npc, Player player) {
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.of(hideClickableTag ? RED_CANDLE : GREEN_CANDLE).displayName(Msg.get(player, "menus.extra.hologram_visibility")).lore(Component.empty(), Msg.get(player, "menus.extra.hologram_visibility.description"), hideClickableTag ? Msg.get(player, "menus.extra.hologram_visibility.description.hidden") : Msg.get(player, "menus.extra.hologram_visibility.description.shown")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setHideClickableHologram(!hideClickableTag);
            menuView.content().set(Slot.of(11), interactableHologram(npc, p));
        });
    }

    public static Button interactableText(Player player) {
        ItemStack i = ItemBuilder.of(NAME_TAG).displayName(Msg.get(player, "menus.extra.hologram_text")).lore(Msg.lore(player.locale(), "menus.extra.hologram_text.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            plugin.wait(p, WaitingType.HOLOGRAM);

            p.closeInventory();
            p.sendMessage(Msg.get(p, "menus.extra.hologram_text.type"));
        });
    }

    public static Button upsideDown(InternalNpc npc, Player player) {
        List<Component> lore = Utils.list(Component.empty(), Msg.get(player, "menus.extra.upside_down.description"));
        boolean upsideDown = npc.getSettings().isUpsideDown();
        lore.addAll(List.of(upsideDown ? Msg.lore(player.locale(), "menus.extra.upside_down.description.true") : Msg.lore(player.locale(), "menus.extra.upside_down.description.false")));
        ItemStack i = ItemBuilder.of(upsideDown ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(player, "menus.extra.upside_down")).lore(lore).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setUpsideDown(!upsideDown);
            menuView.content().set(Slot.of(15), upsideDown(npc, p));
        });
    }

    public static Button importPlayer(Player player) {
        ItemStack i = ItemBuilder.of(ANVIL).displayName(Msg.get(player, "menus.skins.player")).lore(Msg.lore(player.locale(), "menus.skins.player.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.PLAYER);
            event.setCancelled(true);
        });
    }

    public static Button useCatalog(Player player) {
        ItemStack i = ItemBuilder.of(ARMOR_STAND).displayName(Msg.get(player, "menus.skins.catalog")).lore(Msg.lore(player.locale(), "menus.skins.catalog.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            CustomNPCs.getInstance().getSkinCatalog(p).open(CustomNPCs.getInstance().getLotus(), p);
        });
    }

    public static Button importUrl(Player player) {
        ItemStack i = ItemBuilder.of(WRITABLE_BOOK).displayName(Msg.get(player, "menus.skins.url")).lore(Msg.lore(player.locale(), "menus.skins.url.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            p.closeInventory();
            plugin.wait(p, WaitingType.URL);
            event.setCancelled(true);
        });
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
            ItemStack i = ItemBuilder.of(mat).displayName(Msg.get(player, "menus.conditions." + condition.getType().name().toLowerCase())).lore(Component.empty(), Msg.get(player, "menus.conditions.comparator", Arg.arg(Msg.get(player, condition.getComparator().getKey()))), Msg.get(player, "menus.conditions.value", Arg.arg(Msg.get(player, condition.getValue().getTranslationKey()))), Msg.get(player, "menus.conditions.target", Arg.arg(condition.getTarget())), Component.empty(), Msg.get(player, "favicons.remove"), Msg.get(player, "favicons.edit")).build();

            buttons.add(Button.clickable(i, (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    action.removeCondition(condition);
                    menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITIONS);
                } else {
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.editingConditionals.put(p.getUniqueId(), condition.clone());
                    plugin.originalEditingConditionals.put(p.getUniqueId(), condition);
                    menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
                }
            }));
        }
        buttons.add(newCondition(action, player));
        return buttons;
    }

    public static Button newCondition(Action action, Player player) {
        ItemStack i = ItemBuilder.of(LILY_PAD).displayName(Msg.get(player, "menus.conditions.new_condition")).build();
        return Button.clickable(i, new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toggleConditionMode(Action action, Player player) {
        boolean isAll = action.getSelector() == Selector.ALL;
        ItemStack i = ItemBuilder.of(isAll ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(player, "menus.conditions.mode.toggle")).lore(isAll ? Msg.get(player, "menus.conditions.mode.all") : Msg.get(player, "menus.conditions.mode.one")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            action.setSelector(isAll ? Selector.ONE : Selector.ALL);
            menuView.content().set(Slot.of(35), toggleConditionMode(action, p));
        });
    }

    public static Button toggleTextConditionInversion(TextCondition cond, Player player) {
        boolean flag = cond.isInverted();
        ItemStack i = ItemBuilder.of(flag ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(player, "menus.conditions.invert.toggle")).lore(Msg.lore(player.locale(), "menus.conditions.inverted." + flag)).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            cond.setInverted(!cond.isInverted());
            menuView.content().set(Slot.of(16), toggleTextConditionInversion(cond, p));
        });
    }

    public static Button toCondition(Player player) {
        ItemStack i = ItemBuilder.of(ARROW).displayName(Msg.get(player, "items.go_back")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITIONS);
        });
    }

    public static Button editConditions(Player player) {
        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(player, "menus.action_customizer.conditions")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITIONS);
        });
    }

    public static Button numeric(Player player) {
        ItemStack i = ItemBuilder.of(POPPED_CHORUS_FRUIT).displayName(Msg.get(player, "menus.conditions.new.numeric")).lore(Msg.lore(player.locale(), "menus.conditions.new.numeric.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new NumericCondition(Comparator.EQUAL_TO, Condition.Value.EXP_LEVELS, 0.0);
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        });
    }

    public static Button text(Player player) {

        ItemStack i = ItemBuilder.of(WRITTEN_BOOK).displayName(Msg.get(player, "menus.conditions.new.text"))
                .lore(Msg.lore(player.locale(), "menus.conditions.new.text.description"))
                .addFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new TextCondition(Comparator.EQUAL_TO, Condition.Value.USERNAME, "test", false);
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        });
    }

    public static Button booleanCondition(Player player) {
        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(player, "menus.conditions.new.logical"))
                .lore(Msg.lore(player.locale(), "menus.conditions.new.logical.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new BooleanCondition(Comparator.EQUAL_TO, Condition.Value.HAS_PERMISSION, "customnpcs.*");
            plugin.originalEditingConditionals.remove(p.getUniqueId());
            plugin.editingConditionals.put(p.getUniqueId(), conditional);
            menuView.lotus().openMenu(p, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        });
    }
}
