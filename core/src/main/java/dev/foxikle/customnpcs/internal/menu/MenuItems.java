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
import dev.foxikle.customnpcs.api.Skin;
import dev.foxikle.customnpcs.conditions.*;
import dev.foxikle.customnpcs.conditions.Comparator;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.*;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.Result;
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
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Slot;

import java.util.*;

import static org.bukkit.Material.*;


public class MenuItems {
    public static final Button MENU_GLASS;
    private static final CustomNPCs plugin = CustomNPCs.getInstance();

    static {
        MENU_GLASS = Button.clickable(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
                .displayName(Component.text(" ")).build(),
            (_, event) -> event.setCancelled(true));
    }

    public static Button changeLines(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        Component lines = Component.empty();

        for (int i = 0; i < npc.getSettings().getHolograms().size(); i++) {
            String raw = npc.getSettings().getRawHolograms().get(i);
            Component holo = npc.getSettings().getHolograms().get(i);
            if (raw.isEmpty()) {
                holo = Msg.get(v.viewer(), "messages.empty_string");
            }
            lines =
                lines.append(Msg.format("   <dark_gray>" + (i + 1) + ". ").append(holo)).append(Component.newline());
        }

        Component[] lore = Msg.vlore(v.viewer().locale(), "menus.main.items.name.current_name", 100, Arg.arg(lines));

        return Button.clickable(ItemBuilder.of(Material.NAME_TAG).displayName(Msg.get(v.viewer(), "menus.main.items" +
            ".name.name")).lore(lore).build(), new OpenButtonAction(MenuUtils.NPC_HOLOGRAMS, v.data()));
    }

    public static Button resilient(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack i = ItemBuilder.of(Material.BELL)
            .lore(npc.getSettings().isResilient() ? Msg.get(v.viewer(), "menus.main.items.resilient.true") :
                Msg.get(v.viewer(), "menus.main.items.resilient.false"))
            .displayName(Msg.get(v.viewer(), "menus.main.items.resilient.change"))
            .build();

        return Button.clickable(i, (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            if (npc.getSettings().isResilient())
                menuView.viewer().sendMessage(Msg.get(menuView.viewer(), "menus.main.resilient.message.now_false"));
            else menuView.viewer().sendMessage(Msg.get(menuView.viewer(), "menus.main.resilient.message.now_true"));

            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            menuView.content().set(Slot.of(22), MenuItems.resilient(menuView));
        });
    }

    public static ItemStack skinSelection(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        Object currentSkin = npc.getSettings().getSkin().name();
        if (npc.getSettings().getSkin().name().isBlank()) {
            currentSkin = Msg.get(v.viewer(), "menus.main.items.skin.default");
        }
        return ItemBuilder.of(Material.PLAYER_HEAD)
            .editMeta(SkullMeta.class, skullMeta -> {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                String texture =
                    "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9";
                profile.setProperty(new ProfileProperty("textures", texture));
                skullMeta.setPlayerProfile(profile);
            }).lore(Msg.lore(v.viewer().locale(), "menus.main.items.skin.lore", Arg.arg(currentSkin)))
            .displayName(Msg.get(v.viewer(), "menus.main.items.skin.name")).build();
    }

    public static ItemStack extraSettings(MenuView<?, ?> v) {
        return ItemBuilder.of(Material.COMPARATOR).displayName(Msg.get(v.viewer(), "menus.main.settings.name")).build();
    }

    public static ItemStack deleteNpc(MenuView<?, ?> v) {
        return ItemBuilder.of(Material.LAVA_BUCKET).displayName(Msg.get(v.viewer(), "menus.main.delete.name")).build();
    }

    public static ItemStack looking(MenuView<?, ?> v) {
        return ItemBuilder.of(Material.ENDER_EYE).displayName(Msg.get(v.viewer(), "menus.main.facing.name"))
            .lore(Msg.lore(v.viewer().locale(), "menus.main.facing.description")).build();
    }

    public static Button interactable(MenuView<?, ?> view) {
        InternalNpc npc = view.data().require(InternalNpc.KEY);
        boolean interactable = npc.getSettings().isInteractable();

        return Button.clickable(ItemBuilder.of(interactable ? Material.OAK_SAPLING : Material.DEAD_BUSH)
                .lore(interactable ? Msg.get(view.viewer(), "menus.main.interactable.true") : Msg.get(view.viewer(),
                    "menus" +
                        ".main" +
                        ".interactable.false"))
                .displayName(Msg.get(view.viewer(), "menus.main.interactable.name.toggle")).build(),
            (v, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                Utils.sound(v, Sound.UI_BUTTON_CLICK);
                if (npc.getSettings().isInteractable())
                    p.sendMessage(Msg.get(v.viewer(), "menus.main.interactable.message.now_false"));
                else p.sendMessage(Msg.get(v.viewer(), "menus.main.interactable.message.now_true"));

                npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
                v.content().set(Slot.of(25), MenuItems.interactable(v));
                v.content().set(Slot.of(34), MenuItems.showActions(v));
            });
    }

    public static Button showActions(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        if (!npc.getSettings().isInteractable()) return MENU_GLASS;
        return Button.clickable(ItemBuilder.of(Material.RECOVERY_COMPASS)
                .displayName(Msg.get(v.viewer(), "menus.main.interactable.name"))
                .lore(Msg.lore(v.viewer().locale(), "menus.main.interactable.description")).build(),
            new OpenButtonAction(MenuUtils.NPC_ACTIONS, v.data()));
    }

    public static ItemStack editEquipment(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        Equipment equip = npc.getEquipment();
        return ItemBuilder.of(Material.ARMOR_STAND).displayName(Msg.get(v.viewer(), "menus.main.items.equipment.name"))
            .lore(Msg.get(v.viewer(), "menus.main.items.equipment.main_hand", Arg.arg(equipmentName(v,
                    equip.getHand()))),
                Msg.get(v.viewer(), "menus.main.items.equipment.off_hand", Arg.arg(equipmentName(v,
                    equip.getOffhand()))),
                Msg.get(v.viewer(), "menus.main.items.equipment.helmet", Arg.arg(equipmentName(v, equip.getHead()))),
                Msg.get(v.viewer(), "menus.main.items.equipment.chestplate", Arg.arg(equipmentName(v,
                    equip.getChest()))),
                Msg.get(v.viewer(), "menus.main.items.equipment.leggings", Arg.arg(equipmentName(v, equip.getLegs()))),
                Msg.get(v.viewer(), "menus.main.items.equipment.boots", Arg.arg(equipmentName(v, equip.getBoots())))).build();
    }

    private static Component equipmentName(MenuView<?, ?> v, @Nullable ItemStack item) {
        if (item == null) {
            return Msg.get(v.viewer(), "menus.main.items.equipment.empty");
        }
        return Component.translatable(item.translationKey());
    }

    public static Button tunnelVision(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        return Button.clickable(ItemBuilder.of(Material.SPYGLASS).displayName(Msg.get(v.viewer(), "menus.main.vision" +
            ".name")).lore(npc.getSettings().isTunnelvision() ? Msg.get(v.viewer(), "menus.main.vision.tunnel") :
            Msg.get(v.viewer(), "menus.main.vision.normal")).build(), (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            menuView.content().set(Slot.of(28), MenuItems.tunnelVision(menuView));
        });
    }

    public static ItemStack confirmCreation(MenuView<?, ?> v) {
        return ItemBuilder.of(Material.LIME_DYE).displayName(Msg.get(v.viewer(), "menus.main.create.name")).build();
    }

    public static ItemStack cancelCreation(MenuView<?, ?> v) {
        return ItemBuilder.of(BARRIER).displayName(Msg.get(v.viewer(), "menus.main.cancel.name")).build();
    }

    public static ItemStack importArmor(MenuView<?, ?> v) {
        return ItemBuilder.of(Material.ARMOR_STAND).displayName(Msg.get(v.viewer(), "menus.equipment.import"))
            .lore(Msg.get(v.viewer(), "menus.equipment.import.description")).build();
    }

    public static Button helmetSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack helm = npc.getEquipment().getHead();
        if (helm == null || helm.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                .displayName(Msg.get(v.viewer(), "menus.equipment.helmet.empty"))
                .lore(Msg.lore(v.viewer().locale(), "menus.equipment.helmet.change"))
                .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == Material.AIR || event.isRightClick()) return;
                Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                npc.getEquipment().setHead(event.getCursor().clone());
                event.getCursor().setAmount(0);
                p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(menuView,
                    npc.getEquipment().getHead()))));
                menuView.content().set(Slot.of(13), helmetSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.helmet.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(helm)
                .displayName(Component.text(helm.getType().name().toLowerCase(), NamedTextColor.GREEN))
                .addFlags(ItemFlag.values())
                .lore(lore.toArray(new Component[]{}))
                .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setHead(new ItemStack(Material.AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.reset"));
                    menuView.content().set(Slot.of(13), helmetSlot(menuView));
                } else {
                    if (event.getCursor().getType() == Material.AIR) return;
                    npc.getEquipment().setHead(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.helmet.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getHead()))));
                    menuView.content().set(Slot.of(13), helmetSlot(menuView));
                }

            });
        }
    }

    public static Button chestplateSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack cp = npc.getEquipment().getChest();
        if (cp == null || cp.getType().isAir()) {

            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(v.viewer().locale(), "menus.equipment.chestplate.change")).displayName(Msg.get(v.viewer(), "menus.equipment.chestplate.empty")).build(), (menuView, event) -> {
                Player p = (Player) event.getWhoClicked();
                event.setCancelled(true);
                //todo: look into item components
                if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.success",
                        Arg.arg(equipmentName(menuView,
                            npc.getEquipment().getChest()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.error"));
                }

                menuView.content().set(Slot.of(22), chestplateSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.chestplate.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(cp).addFlags(ItemFlag.values())
                .lore(lore.toArray(new Component[]{}))
                .displayName(Component.text(cp.getType().toString(), NamedTextColor.GREEN))
                .build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setChest(new ItemStack(AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.reset"));
                    menuView.content().set(Slot.of(22), chestplateSlot(menuView));
                    return;
                } else if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.success",
                        Arg.arg(equipmentName(menuView,
                            npc.getEquipment().getChest()))));
                    return;
                } else {
                    if (event.getCursor().getType() == AIR) return;
                }
                event.setCancelled(true);
                p.sendMessage(Msg.get(p, "menus.equipment.chestplate.message.error"));
                menuView.content().set(Slot.of(22), chestplateSlot(menuView));
            });
        }
    }

    public static Button leggingsSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack legs = npc.getEquipment().getLegs();
        if (legs == null || legs.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(v.viewer().locale(), "menus.equipment.legs.change")).displayName(Msg.get(v.viewer(), "menus.equipment.legs.empty")).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getLegs()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.error"));
                }
                menuView.content().set(Slot.of(31), leggingsSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.legs.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(legs)
                .addFlags(ItemFlag.values())
                .displayName(Component.text(legs.getType().toString(), NamedTextColor.GREEN))
                .lore(lore.toArray(new Component[]{})).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    npc.getEquipment().setLegs(new ItemStack(AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.reset"));
                    menuView.content().set(Slot.of(31), leggingsSlot(menuView));
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getLegs()))));
                    menuView.content().set(Slot.of(31), leggingsSlot(menuView));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.legs.message.error"));
                }
            });
        }
    }

    public static Button bootsSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack boots = npc.getEquipment().getBoots();
        if (boots == null || boots.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(LIME_STAINED_GLASS_PANE).displayName(Msg.get(v.viewer(), "menus" +
                ".equipment.boots.empty")).addFlags(ItemFlag.values()).lore(Msg.lore(v.viewer().locale(), "menus" +
                ".equipment.boots.change")).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType().name().contains("BOOTS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getBoots()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.error"));
                    return;
                }
                menuView.content().set(Slot.of(40), bootsSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.boots.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(boots)
                .lore(lore.toArray(new Component[]{}))
                .displayName(Component.text(boots.getType().toString(), NamedTextColor.GREEN))
                .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setBoots(new ItemStack(AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.reset"));
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getBoots()))));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    p.sendMessage(Msg.get(p, "menus.equipment.boots.message.error"));
                    return;
                }
                menuView.content().set(Slot.of(40), bootsSlot(menuView));
            });
        }
    }

    public static Button handSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack hand = npc.getEquipment().getHand();
        if (hand == null || hand.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(YELLOW_STAINED_GLASS_PANE).addFlags(ItemFlag.values()).lore(Msg.lore(v.viewer().locale(), "menus.equipment.hand.change")).displayName(Msg.get(v.viewer(), "menus.equipment.hand.empty")).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setHand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(menuView,
                    npc.getEquipment().getHand()))));
                menuView.content().set(Slot.of(23), handSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(hand).lore(lore.toArray(new Component[]{}))
                .displayName(Component.text(hand.getType().toString(), NamedTextColor.GREEN))
                .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setHand(new ItemStack(AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.reset"));
                    menuView.content().set(Slot.of(23), handSlot(menuView));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setHand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.hand.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getHand()))));
                    menuView.content().set(Slot.of(23), handSlot(menuView));
                }
            });
        }
    }

    public static Button offhandSlot(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        ItemStack offhand = npc.getEquipment().getOffhand();
        if (offhand == null || offhand.getType().isAir()) {
            return Button.clickable(ItemBuilder.of(YELLOW_STAINED_GLASS_PANE).displayName(Msg.get(v.viewer(), "menus" +
                ".equipment.offhand.empty")).lore(Msg.lore(v.viewer().locale(), "menus.equipment.offhand.change")).addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setOffhand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                p.sendMessage(Msg.get(p, "menus.equipment.offhand.message.success", Arg.arg(equipmentName(menuView,
                    npc.getEquipment().getOffhand()))));
                menuView.content().set(Slot.of(21), offhandSlot(menuView));
            });
        } else {
            List<Component> lore = Utils.list(Msg.lore(v.viewer().locale(), "menus.equipment.hand.change"));
            lore.add(Msg.get(v.viewer(), "remove.description"));
            return Button.clickable(ItemBuilder.of(offhand).lore(lore.toArray(new Component[]{}))
                .displayName(Component.text(offhand.getType().toString(), NamedTextColor.GREEN))
                .addFlags(ItemFlag.values()).build(), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    npc.getEquipment().setOffhand(new ItemStack(AIR));
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    p.sendMessage(Msg.get(p, "menus.equipment.offhand.reset"));
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setOffhand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    Utils.sound(menuView, Sound.ITEM_ARMOR_EQUIP_LEATHER);
                    p.sendMessage(Msg.get(p, "menus.equipment.offhand.message.success", Arg.arg(equipmentName(menuView,
                        npc.getEquipment().getOffhand()))));
                }
                menuView.content().set(Slot.of(21), offhandSlot(menuView));
            });
        }
    }

    public static Button toPose(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(SNIFFER_EGG).displayName(Msg.get(v.viewer(), "pose.pose_editor")).build(),
            new OpenButtonAction(MenuUtils.NPC_POSE, v.data()));
    }

    public static Button toMain(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(BARRIER)
                .displayName(Msg.get(v.viewer(), "items.go_back")).build(),
            new OpenButtonAction(MenuUtils.NPC_MAIN, v.data()));
    }

    public static Button toMain(Locale locale, InternalNpc npc) {
        return Button.clickable(ItemBuilder.of(BARRIER)
                .displayName(Msg.get(locale, "items.go_back")).build(),
            new OpenButtonAction(MenuUtils.NPC_MAIN, DataRegistry.empty().put(InternalNpc.KEY, npc)));
    }

    public static Button goBack(MenuView<?, ?> v, Menu<Component> back, DataRegistry data) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(v.viewer(), "items.go_back")).build(),
            new OpenButtonAction(back, data));
    }

    public static Button toAction(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(v.viewer(), "items.go_back")).build(),
            new OpenButtonAction(MenuUtils.NPC_ACTIONS, v.data()));
    }

    public static Button toActionSaveConditions(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(v.viewer(), "items.go_back")).build(),
            (menu, event) -> {
                Action action = v.data().require(Action.KEY);
                event.setCancelled(true);
                Utils.sound(menu, Sound.UI_BUTTON_CLICK);
                plugin.getLotus().openMenu(menu.viewer(), action.getMenu(), menu.data());
            });
    }

    public static Button toNewCondition(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(ARROW).displayName(Msg.get(v.viewer(), "items.go_back")).build(),
            new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION, v.data()));
    }

    public static List<Button> currentActions(MenuView<?, ?> view) {
        InternalNpc npc = view.data().require(InternalNpc.KEY);
        List<Button> buttons = new ArrayList<>();

        for (Action action : npc.getActions()) {
            buttons.add(Button.clickable(action.getFavicon(view.viewer()), (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    if (event.isShiftClick()) {
                        Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                        npc.removeAction(action);
                        MenuUtils.open(p, MenuUtils.NPC_ACTIONS, view.data());
                        return;
                    }
                    Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                    Await.deleteAction(p, npc, action)
                        .thenAccept(result -> {
                            switch (result) {
                                case Result.Ok<Action> res -> {
                                    Utils.sound(view, Sound.ITEM_TRIDENT_HIT);
                                    npc.removeAction(res.value());
                                    MenuUtils.open(p, MenuUtils.NPC_ACTIONS, view.data());
                                }
                                case Result.Failed<?> res -> MenuUtils.awaitFail(res, view);
                                case Result.Cancelled<?> _ -> MenuUtils.open(view);
                            }
                        });
                } else if (event.isLeftClick()) {
                    if (action.canEdit()) {
                        Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                        Action cloned = action.clone();
                        MenuUtils.open(p, action.getMenu(), view.data().put(Action.KEY, cloned));
                    } else {
                        Utils.sound(menuView, Sound.ENTITY_VILLAGER_NO);
                        p.sendMessage(Msg.get(p, "edit.fail"));
                    }
                }
            }));
        }

        buttons.add(Button.clickable(ItemBuilder.of(LILY_PAD)
            .displayName(Msg.get(view.viewer(), "menus.actions.new"))
            .build(), (v, _) -> Await.createAction(v.viewer(), npc)
            .thenAccept(result -> {
                switch (result) {
                    case Result.Cancelled<?> _ -> MenuUtils.open(v);
                    case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                    case Result.Ok<Action> res -> npc.addAction(res.value());
                }
            })));

        return buttons;
    }

    public static List<Button> currentLines(MenuView<?, ?> view) {
        InternalNpc npc = view.data().require(InternalNpc.KEY);
        List<Button> buttons = new ArrayList<>();

        List<String> raw = npc.getSettings().getRawHolograms();
        List<String> mutable = new ArrayList<>(raw);
        for (int i = 0; i < raw.size(); i++) {
            String line = raw.get(i);
            List<Component> lore = Utils.list(Msg.format(line), Component.empty(), Msg.get(view.viewer(), "menus" +
                ".ordered" +
                ".edit"), Msg.get(view.viewer(), "menus.ordered.delete"));
            boolean canMoveDown = i < raw.size() - 1;
            boolean canMoveUp = i > 0;

            if (canMoveDown) lore.add(Msg.get(view.viewer(), "menus.holograms.move_down"));
            if (canMoveUp) lore.add(Msg.get(view.viewer(), "menus.holograms.move_up"));

            int finalI = i;
            buttons.add(Button.clickable(ItemBuilder.of(PAPER).displayName(Msg.get(view.viewer(), "menus.holograms" +
                        ".line",
                    Arg.arg(i + 1))).lore(lore).build(),
                (v, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();

                    // DROP is delete
                    // DROP_STACK is delete without confirmation
                    // SWAP TO OFFHAND is edit
                    // LEFT is up
                    // RIGHT is down

                    if (event.getClick() == ClickType.DROP) {
                        if (npc.getSettings().getRawHolograms().size() == 1) {
                            v.viewer().sendMessage(Msg.get(v.viewer(), "menus.holograms.min_one"));
                            return;
                        }
                        v.data().put(HologramMenu.EDITING_INDEX, finalI);
                        MenuUtils.open(p, MenuUtils.NPC_DELETE_LINE, v.data());
                        return;
                    }
                    if (event.getClick() == ClickType.CONTROL_DROP) {
                        if (npc.getSettings().getRawHolograms().size() == 1) {
                            v.viewer().sendMessage(Msg.get(v.viewer(), "menus.holograms.min_one"));
                            return;
                        }
                        mutable.remove(finalI);
                        Utils.sound(v, Sound.ITEM_TRIDENT_HIT);
                        npc.getSettings().getRawHolograms().remove(finalI);
                        npc.getSettings().setHolograms(mutable);
                        MenuUtils.open(p, MenuUtils.NPC_HOLOGRAMS, v.data());
                        return;
                    }
                    if (event.getClick() == ClickType.SWAP_OFFHAND) {
                        p.sendMessage(Msg.get(p, "data.name.title"));


                        Utils.sound(v, Sound.UI_BUTTON_CLICK);
                        Await.input(p, WaitingType.NAME, v.data()
                                .put(HologramMenu.EDITING_INDEX, finalI))
                            .thenAccept(result -> {
                                switch (result) {
                                    case Result.Cancelled<?> _ -> MenuUtils.open(v);
                                    case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                                    case Result.Ok<String> res -> {
                                        int index = v.data().require(HologramMenu.EDITING_INDEX);
                                        if (npc.getSettings().getRawHolograms().size() <= index) {
                                            npc.getSettings().getRawHolograms().add(res.value());
                                        } else {
                                            npc.getSettings().getRawHolograms().set(index, res.value());
                                        }
                                        v.viewer().sendMessage(Msg.get(v.viewer(), "set.name", Arg.arg(index + 1),
                                            Arg.arg(res.value())));
                                        v.data().put(HologramMenu.EDITING_INDEX,
                                            npc.getSettings().getHolograms().size());
                                        MenuUtils.open(v);
                                    }
                                }
                            });

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
                            Utils.sound(v, Sound.ENTITY_VILLAGER_NO);
                            return;
                        }

                        Collections.swap(mutable, finalI, finalI - 1);
                        npc.getSettings().setHolograms(mutable);
                        MenuUtils.open(p, MenuUtils.NPC_HOLOGRAMS, v.data());
                        Utils.sound(v, Sound.BLOCK_PISTON_EXTEND);
                        return;
                    }

                    if (!canMoveDown) {
                        p.sendMessage(Msg.get(p, "menus.holograms.move_down_fail"));
                        Utils.sound(v, Sound.ENTITY_VILLAGER_NO);
                        return;
                    }

                    Collections.swap(mutable, finalI, finalI + 1);
                    npc.getSettings().setHolograms(mutable);
                    MenuUtils.open(p, MenuUtils.NPC_HOLOGRAMS, v.data());
                    Utils.sound(v, Sound.BLOCK_PISTON_CONTRACT);
                }));
        }

        buttons.add(Button.clickable(ItemBuilder.of(LILY_PAD)
                .displayName(Msg.get(view.viewer(), "menus.holograms.new_line")).build(),
            (v, _) -> {
                Player p = v.viewer();
                Utils.sound(v, Sound.UI_BUTTON_CLICK);
                Await.input(p, WaitingType.NAME, v.data()
                        .put(HologramMenu.EDITING_INDEX, npc.getSettings().getHolograms().size()))
                    .thenAccept(result -> {
                        switch (result) {
                            case Result.Cancelled<?> _ -> MenuUtils.open(v);
                            case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                            case Result.Ok<String> res -> {
                                int index = v.data().require(HologramMenu.EDITING_INDEX);
                                if (npc.getSettings().getRawHolograms().size() <= index) {
                                    npc.getSettings().getRawHolograms().add(res.value());
                                } else {
                                    npc.getSettings().getRawHolograms().set(index, res.value());
                                }
                                v.viewer().sendMessage(Msg.get(v.viewer(), "set.name", Arg.arg(index + 1),
                                    Arg.arg(res.value())));
                                v.data().put(HologramMenu.EDITING_INDEX, npc.getSettings().getHolograms().size());
                                MenuUtils.open(v);
                            }
                        }
                    });
            }));

        return buttons;
    }

    public static Button delayDisplay(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(CLOCK)
                .displayName(Msg.get(v.viewer(), "menus.action_customizer.delay.name", Arg.arg(action.getDelay()))).build(),
            (_, event) -> event.setCancelled(true));
    }

    public static Button decrementDelay(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(RED_DYE).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
                    ".delay.decrement"))
                .lore(Msg.lore(v.viewer().locale(), "menus.action_customizer.delay.decrement.description")).build(),
            (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                if (action.getDelay() == 0) {
                    p.sendMessage(Msg.get(p, "menus.action_customizer.delay.error"));
                    return;
                }
                action.setDelay(Math.max(0, Utils.decrement(event).apply(action.getDelay())));
                menuView.content().set(Slot.of(4), delayDisplay(action, v));
            });
    }

    public static Button incrementDelay(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(LIME_DYE).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
                ".delay" +
                ".increment")).lore(Msg.lore(v.viewer().locale(), "menus.action_customizer.delay.increment" +
                ".description")).build(),
            (menuView, event) -> {
                event.setCancelled(true);
                action.setDelay(Utils.increment(event).apply(action.getDelay()));
                Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                menuView.content().set(Slot.of(4), delayDisplay(action, v));
            });
    }

    public static Button cooldownDisplay(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(CLOCK).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
            ".cooldown" +
            ".name", Arg.arg(action.getCooldown()))).build(), (_, event) -> event.setCancelled(true));
    }

    public static Button decrementCooldown(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(RED_DYE).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
                ".cooldown" +
                ".decrement")).lore(Msg.lore(v.viewer().locale(), "menus.action_customizer.cooldown.decrement" +
                ".description")).build(),
            (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();
                Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                if (action.getCooldown() == 0) {
                    p.sendMessage(Msg.get(p, "menus.action_customizer.cooldown.error"));
                    return;
                }
                action.setCooldown(Math.max(0, Utils.decrement(event).apply(action.getCooldown())));

                menuView.content().set(Slot.of(7), cooldownDisplay(action, v));
            });
    }

    public static Button incrementCooldown(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(LIME_DYE).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
            ".cooldown.increment")).lore(Msg.lore(v.viewer().locale(), "menus.action_customizer.cooldown.increment" +
            ".description")).build(), (menuView, event) -> {
            event.setCancelled(true);
            action.setCooldown(Math.max(0, Utils.increment(event).apply(action.getCooldown())));
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            menuView.content().set(Slot.of(7), cooldownDisplay(action, v));
        });
    }

    public static Button saveAction(Action action, MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(v.viewer(), "menus.action_customizer" +
            ".confirm")).build(), (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            InternalNpc npc = v.data().require(InternalNpc.KEY);

            npc.addAction(action);
            MenuUtils.open(p, MenuUtils.NPC_ACTIONS, v.data());
        });
    }

    public static Button display(Component text, Component... lore) {
        return Button.clickable(ItemBuilder.of(CLOCK).displayName(text).lore(lore).build(),
            (_, e) -> e.setCancelled(true));
    }

    public static Button saveCondition(MenuView<?, ?> v) {
        return Button.clickable(ItemBuilder.of(LILY_PAD).displayName(Msg.get(v.viewer(), "menus.main.create.name")).build(),
            (menuView, event) -> {
                event.setCancelled(true);

                Player p = (Player) event.getWhoClicked();
                Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                Action action = menuView.data().require(Action.KEY);
                Condition edited = menuView.data().require(Condition.KEY);
                action.addCondition(edited);
                MenuUtils.open(p, MenuUtils.NPC_CONDITIONS, v.data());
            });
    }

    public static Button comparatorSwitcher(Condition condition, MenuView<?, ?> v, int slot) {

        List<Component> lore = new ArrayList<>();
        for (Comparator c : Comparator.getSupportedConditions(condition)) {
            if (condition.getComparator() != c) lore.add(Msg.get(v.viewer(), c.getKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(v.viewer(), c.getKey()).color(NamedTextColor.DARK_AQUA)));
        }
        lore.add(Msg.get(v.viewer(), "items.click_to_change"));

        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(v.viewer(), "comparator"))
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
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            menuView.content().set(Slot.of(slot), comparatorSwitcher(condition, v, slot));
        });
    }

    public static Button targetValueSelector(MenuView<?, ?> view) {
        Condition condition = view.data().require(Condition.KEY);
        if (!condition.getValue().hasInput) {
            return MENU_GLASS;
        }

        ItemStack i =
            ItemBuilder.of(OAK_HANGING_SIGN).displayName(Msg.get(view.viewer(), "value.select")).lore(Msg.get(view.viewer(), "value" +
                ".current", Arg.arg(condition.getTarget())), Msg.get(view.viewer(), "items.click_to_change")).build();

        return Button.clickable(i, (v, event) -> {
            Player p = (Player) event.getWhoClicked();
            Utils.sound(v, Sound.UI_BUTTON_CLICK);
            Await.input(p, WaitingType.TARGET, v.data())
                .thenAccept(result -> {
                    switch (result) {
                        case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                        case Result.Cancelled<?> _ -> MenuUtils.open(v);
                        case Result.Ok<String> res -> {
                            condition.setTarget(res.value());
                            v.data().put(Condition.KEY, condition);
                            v.viewer().sendMessage(Msg.get(v.viewer(), "action.conditions.set.target",
                                Arg.arg(res.value())));
                            MenuUtils.open(v);
                        }
                    }
                });
        });
    }

    public static Button valueSwitcher(Condition condition, MenuView<?, ?> view, int slot, int inputSlot) {
        List<Component> lore = new ArrayList<>();

        for (Condition.Value v : Condition.Value.getSupportedConditions(condition)) {

            if (condition.getValue() != v)
                lore.add(Msg.get(view.viewer(), v.getTranslationKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Msg.format("<dark_aqua>▸ ").append(Msg.get(view.viewer(), v.getTranslationKey()).color(NamedTextColor.DARK_AQUA)));

        }
        lore.add(Msg.get(view.viewer(), "items.click_to_change"));

        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(view.viewer(), "statistic"))
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
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            menuView.content().set(Slot.of(inputSlot), targetValueSelector(menuView));
            menuView.content().set(Slot.of(slot), valueSwitcher(condition, menuView, slot, inputSlot));
        });
    }

    public static Button interactableHologram(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.of(hideClickableTag ? RED_CANDLE : GREEN_CANDLE).displayName(Msg.get(v.viewer(),
            "menus" +
                ".extra.hologram_visibility")).lore(Component.empty(), Msg.get(v.viewer(), "menus.extra" +
            ".hologram_visibility" +
            ".description"), hideClickableTag ?
            Msg.get(v.viewer(), "menus.extra.hologram_visibility.description.hidden") : Msg.get(v.viewer(), "menus" +
            ".extra" +
            ".hologram_visibility.description.shown")).build();

        return Button.clickable(i, (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            npc.getSettings().setHideClickableHologram(!hideClickableTag);
            menuView.content().set(Slot.of(11), interactableHologram(menuView));
        });
    }

    public static Button interactableText(MenuView<?, ?> view) {
        ItemStack i =
            ItemBuilder.of(NAME_TAG).displayName(Msg.get(view.viewer(), "menus.extra.hologram_text")).lore(Msg.lore(view.viewer().locale(), "menus.extra.hologram_text.description")).build();

        return Button.clickable(i, (v, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(v, Sound.UI_BUTTON_CLICK);
            Await.input(p, WaitingType.HOLOGRAM, v.data())
                .thenAccept(result -> {
                    switch (result) {
                        case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                        case Result.Cancelled<?> _ -> MenuUtils.open(v);
                        case Result.Ok<String> res -> {
                            InternalNpc npc = v.data().require(InternalNpc.KEY);
                            v.viewer().sendMessage(Msg.get(v.viewer(), "set.clickable_hologram", Arg.arg(res.value())));
                            npc.getSettings().setCustomInteractableHologram(res.value());
                            MenuUtils.open(v);
                        }
                    }
                });
            p.sendMessage(Msg.get(p, "menus.extra.hologram_text.type"));
        });
    }

    public static Button upsideDown(MenuView<?, ?> v) {
        InternalNpc npc = v.data().require(InternalNpc.KEY);
        List<Component> lore = Utils.list(Component.empty(), Msg.get(v.viewer(), "menus.extra.upside_down" +
            ".description"));
        boolean upsideDown = npc.getSettings().isUpsideDown();
        lore.addAll(List.of(upsideDown ? Msg.lore(v.viewer().locale(), "menus.extra.upside_down.description.true") :
            Msg.lore(v.viewer().locale(), "menus.extra.upside_down.description.false")));
        ItemStack i = ItemBuilder.of(upsideDown ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(v.viewer(), "menus" +
            ".extra" +
            ".upside_down")).lore(lore).build();

        return Button.clickable(i, (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            npc.getSettings().setUpsideDown(!upsideDown);
            menuView.content().set(Slot.of(15), upsideDown(menuView));
        });
    }

    public static Button importPlayer(MenuView<?, ?> view) {
        ItemStack i = ItemBuilder.of(ANVIL)
            .displayName(Msg.get(view.viewer(), "menus.skins.player"))
            .lore(Msg.lore(view.viewer().locale(), "menus.skins.player.description")).build();

        return Button.clickable(i, (v, _) -> {
            Player p = v.viewer();
            Utils.sound(v, Sound.UI_BUTTON_CLICK);
            p.closeInventory();
            Await.skin(p, WaitingType.PLAYER, v.data()).thenAccept(result -> {
                switch (result) {
                    case Result.Cancelled<?> _ -> MenuUtils.open(v);
                    case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                    case Result.Ok<Skin> res -> {
                        InternalNpc npc = v.data().require(InternalNpc.KEY);
                        npc.getSettings().setSkinData(res.value());
                        v.viewer().sendMessage(Msg.get(v.viewer(), "skins.success.player_name",
                            Arg.arg(res.value().name())));
                        MenuUtils.open(v.viewer(), MenuUtils.NPC_MAIN, v.data());
                    }
                }
            });
        });
    }

    public static Button useCatalog(MenuView<?, ?> v) {
        ItemStack i =
            ItemBuilder.of(ARMOR_STAND).displayName(Msg.get(v.viewer(), "menus.skins.catalog"))
                .lore(Msg.lore(v.viewer().locale(), "menus.skins.catalog.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            MenuUtils.getSkinCatalogue(menuView.viewer().locale(), v.data().require(InternalNpc.KEY)).open(CustomNPCs.getInstance().getLotus(), v.viewer());
        });
    }

    public static Button importUrl(MenuView<?, ?> view) {
        ItemStack i =
            ItemBuilder.of(WRITABLE_BOOK).displayName(Msg.get(view.viewer(), "menus.skins.url"))
                .lore(Msg.lore(view.viewer().locale(), "menus.skins.url.description")).build();

        return Button.clickable(i, (v, _) -> {
            Utils.sound(v, Sound.UI_BUTTON_CLICK);
            Await.skin(v.viewer(), WaitingType.URL, v.data())
                .thenAccept(result -> {
                    switch (result) {
                        case Result.Cancelled<?> _ -> MenuUtils.open(v);
                        case Result.Failed<?> res -> MenuUtils.awaitFail(res, v);
                        case Result.Ok<Skin> res -> {
                            InternalNpc npc = v.data().require(InternalNpc.KEY);
                            npc.getSettings().setSkinData(res.value());
                            v.viewer().sendMessage(Msg.get(v.viewer(), "skins.success.url",
                                Arg.arg(res.value().name())));
                            MenuUtils.open(v.viewer(), MenuUtils.NPC_MAIN, v.data());
                        }
                    }
                });
        });
    }

    public static List<Button> conditions(Action action, MenuView<?, ?> v) {
        List<Button> buttons = new ArrayList<>();

        if (action.getConditions() == null) {
            return Utils.list(newCondition(v));
        }

        for (Condition condition : action.getConditions()) {
            Material mat = switch (condition.getType()) {
                case NUMERIC -> POPPED_CHORUS_FRUIT;
                case LOGICAL -> COMPARATOR;
                case TEXT -> BOOK;
            };
            //todo: text conditions can be inverted (outsource this to a condition impl class?)
            ItemStack i = ItemBuilder.of(mat).displayName(Msg.get(v.viewer(),
                "menus.conditions." + condition.getType().name().toLowerCase())).lore(Component.empty(),
                Msg.get(v.viewer(), "menus.conditions.comparator", Arg.arg(Msg.get(v.viewer(),
                    condition.getComparator().getKey()))), Msg.get(v.viewer(), "menus.conditions.value",
                    Arg.arg(Msg.get(v.viewer(), condition.getValue().getTranslationKey()))), Msg.get(v.viewer(),
                    "menus" +
                        ".conditions.target", Arg.arg(condition.getTarget())), Component.empty(), Msg.get(v.viewer(),
                    "favicons.remove"), Msg.get(v.viewer(), "favicons.edit")).build();

            buttons.add(Button.clickable(i, (menuView, event) -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    Utils.sound(menuView, Sound.ITEM_TRIDENT_HIT);
                    action.removeCondition(condition);
                    MenuUtils.open(p, MenuUtils.NPC_CONDITIONS, v.data());
                } else {
                    Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
                    menuView.data().put(Condition.KEY, condition.clone());
                    MenuUtils.open(p, MenuUtils.NPC_CONDITION_CUSTOMIZER, menuView.data());
                }
            }));
        }
        buttons.add(newCondition(v));
        return buttons;
    }

    public static Button newCondition(MenuView<?, ?> v) {
        ItemStack i =
            ItemBuilder.of(LILY_PAD).displayName(Msg.get(v.viewer(), "menus.conditions.new_condition")).build();
        return Button.clickable(i, new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION, v.data()));
    }

    public static Button toggleConditionMode(MenuView<?, ?> v) {
        Action action = v.data().require(Action.KEY);
        boolean isAll = action.getSelector() == Selector.ALL;
        ItemStack i = ItemBuilder.of(isAll ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(v.viewer(), "menus" +
            ".conditions" +
            ".mode.toggle")).lore(isAll ? Msg.get(v.viewer(), "menus.conditions.mode.all") : Msg.get(v.viewer(),
            "menus" +
                ".conditions.mode.one")).build();

        return Button.clickable(i, (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            action.setSelector(isAll ? Selector.ONE : Selector.ALL);
            menuView.content().set(Slot.of(35), toggleConditionMode(menuView));
        });
    }

    public static Button toggleTextConditionInversion(TextCondition cond, MenuView<?, ?> v) {
        boolean flag = cond.isInverted();
        ItemStack i = ItemBuilder.of(flag ? GREEN_CANDLE : RED_CANDLE).displayName(Msg.get(v.viewer(), "menus" +
            ".conditions" +
            ".invert.toggle")).lore(Msg.lore(v.viewer().locale(), "menus.conditions.inverted." + flag)).build();

        return Button.clickable(i, (menuView, event) -> {
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            cond.setInverted(!cond.isInverted());
            menuView.content().set(Slot.of(16), toggleTextConditionInversion(cond, menuView));
        });
    }

    public static Button toCondition(MenuView<?, ?> v) {
        ItemStack i = ItemBuilder.of(ARROW).displayName(Msg.get(v.viewer(), "items.go_back")).build();

        return Button.clickable(i, (menuView, event) -> {
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            event.setCancelled(true);
            MenuUtils.open(p, MenuUtils.NPC_CONDITIONS, v.data());
        });
    }

    public static Button editConditions(MenuView<?, ?> v) {
        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(v.viewer(),
            "menus.action_customizer.conditions")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            MenuUtils.open(p, MenuUtils.NPC_CONDITIONS, menuView.data());
        });
    }

    public static Button numeric(MenuView<?, ?> v) {
        ItemStack i =
            ItemBuilder.of(POPPED_CHORUS_FRUIT).displayName(Msg.get(v.viewer(), "menus.conditions.new.numeric")).lore(Msg.lore(v.viewer().locale(), "menus.conditions.new.numeric.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            Condition cond = new NumericCondition(Comparator.EQUAL_TO, Condition.Value.EXP_LEVELS, UUID.randomUUID(),
                0.0);
            MenuUtils.open(p, MenuUtils.NPC_CONDITION_CUSTOMIZER, menuView.data().put(Condition.KEY, cond));
        });
    }

    public static Button text(MenuView<?, ?> v) {

        ItemStack i = ItemBuilder.of(WRITTEN_BOOK).displayName(Msg.get(v.viewer(), "menus.conditions.new.text"))
            .lore(Msg.lore(v.viewer().locale(), "menus.conditions.new.text.description"))
            .addFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            Condition cond = new TextCondition(Comparator.EQUAL_TO, Condition.Value.USERNAME, UUID.randomUUID(),
                "test", false);
            MenuUtils.open(p, MenuUtils.NPC_CONDITION_CUSTOMIZER, menuView.data().put(Condition.KEY, cond));
        });
    }

    public static Button booleanCondition(MenuView<?, ?> v) {
        ItemStack i = ItemBuilder.of(COMPARATOR).displayName(Msg.get(v.viewer(), "menus.conditions.new.logical"))
            .lore(Msg.lore(v.viewer().locale(), "menus.conditions.new.logical.description")).build();

        return Button.clickable(i, (menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            Utils.sound(menuView, Sound.UI_BUTTON_CLICK);
            Condition conditional = new BooleanCondition(Comparator.EQUAL_TO, Condition.Value.HAS_PERMISSION,
                UUID.randomUUID(), "customnpcs.*");
            MenuUtils.open(p, MenuUtils.NPC_CONDITION_CUSTOMIZER, menuView.data().put(Condition.KEY, conditional));
        });
    }
}
