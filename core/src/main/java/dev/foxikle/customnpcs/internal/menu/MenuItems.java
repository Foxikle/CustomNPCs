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
import dev.foxikle.customnpcs.internal.runnables.InteractableHologramRunnable;
import dev.foxikle.customnpcs.internal.runnables.PlayerNameRunnable;
import dev.foxikle.customnpcs.internal.runnables.TargetInputRunnable;
import dev.foxikle.customnpcs.internal.runnables.UrlRunnable;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.OpenButtonAction;
import dev.foxikle.customnpcs.internal.utils.Utils;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Material.*;


public class MenuItems {
    public static final Button MENU_GLASS;
    public static final Button ERROR_EQUIPMENT;
    private static final CustomNPCs plugin = CustomNPCs.getInstance();

    static {
        MENU_GLASS = Button.clickable(ItemBuilder.modern(Material.BLACK_STAINED_GLASS_PANE).setDisplay(Component.text(" ")).build(),
                ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
        ERROR_EQUIPMENT = Button.clickable(ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.translated("customnpcs.items.error")).setLore(Msg.translated("customnpcs.items.error.lore")).build(),
                ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button rotation(InternalNpc npc) {
        double dir = npc.getSettings().getDirection();

        List<Component> lore = new ArrayList<>();
        Map<Integer, Integer> highlightIndexMap = Map.of(180, 0, -135, 1, -90, 2, -45, 3, 0, 4, 45, 5, 90, 6, 135, 7);
        Component clickToChange = Msg.translated("customnpcs.items.click_to_change");
        List<Component> directions = List.of(Msg.translated("customnpcs.directions.north"), Msg.translated("customnpcs.directions.north_east"), Msg.translated("customnpcs.directions.east"), Msg.translated("customnpcs.directions.south_east"), Msg.translated("customnpcs.directions.south"), Msg.translated("customnpcs.directions.south_west"), Msg.translated("customnpcs.directions.west"), Msg.translated("customnpcs.directions.north_west"), Msg.translated("customnpcs.directions.player"));
        int highlightIndex = highlightIndexMap.getOrDefault((int) dir, 8);
        lore.add(Component.empty());

        for (int i = 0; i < directions.size(); ++i) {
            Component direction = directions.get(i);
            if (i == highlightIndex) {
                direction = direction.color(NamedTextColor.DARK_AQUA);
                direction = Utils.mm("<dark_aqua>▸ ").append(direction);
            }

            lore.add(direction);
        }

        lore.add(Component.empty());
        lore.add(clickToChange);

        ItemStack item = ItemBuilder.modern(COMPASS)
                .setLore(lore)
                .setDisplay(Msg.translated("customnpcs.menus.main.facing_direction.name"))
                .build();

        return Button.clickable(item, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

            double newDir = 0.0D;
            if (event.isLeftClick()) {
                if (dir % 45.0D != 0.0D) {
                    newDir = 180.0D;
                } else {
                    newDir = (dir + 225.0D) % 360.0D - 180.0D;
                    if (dir == 135.0D) {
                        newDir = player.getLocation().getYaw();
                    }
                }
            } else if (event.isRightClick()) {
                if (dir % 45.0D != 0.0D) {
                    newDir = 135.0D;
                } else {
                    newDir = (dir - 225.0D) % 360.0D + 180.0D;
                    if (dir == 180.0D) {
                        newDir = player.getLocation().getYaw();
                    }
                }
            }

            npc.getSettings().setDirection(newDir);
            menuView.replaceButton(10, rotation(npc));
        }));
    }

    public static ItemStack changeName(InternalNpc npc) {
        return ItemBuilder.modern(Material.NAME_TAG)
                .setDisplay(Msg.translated("customnpcs.menus.main.items.name.name"))
                .setLore(Msg.translated("customnpcs.menus.main.items.name.current_name", plugin.getMiniMessage().deserialize(npc.getSettings().getName())))
                .build();
    }

    public static Button resilient(InternalNpc npc) {
        ItemStack i = ItemBuilder.modern(Material.BELL)
                .setLore(npc.getSettings().isResilient() ? Msg.translated("customnpcs.menus.main.items.resilient.true") : Msg.translated("customnpcs.menus.main.items.resilient.false"))
                .setDisplay(Msg.translated("customnpcs.menus.main.items.resilient.change"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            if (npc.getSettings().isResilient())
                player.sendMessage(Msg.translated("customnpcs.menus.main.resilient.message.now_false"));
            else player.sendMessage(Msg.translated("customnpcs.menus.main.resilient.message.now_true"));

            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            menuView.replaceButton(22, MenuItems.resilient(npc));
        }));
    }

    public static ItemStack skinSelection(InternalNpc npc) {
        return ItemBuilder.modern(Material.PLAYER_HEAD).modifyMeta(SkullMeta.class, skullMeta -> {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    String texture = "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9";
                    profile.setProperty(new ProfileProperty("textures", texture));
                    skullMeta.setPlayerProfile(profile);
                })
                .setLore(Msg.lore("customnpcs.menus.main.items.skin.lore", Component.text(npc.getSettings().getSkinName())))
                .setDisplay(Msg.translated("customnpcs.menus.main.items.skin.name"))
                .build();
    }

    public static ItemStack extraSettings() {
        return ItemBuilder.modern(Material.COMPARATOR)
                .setDisplay(Msg.translated("customnpcs.menus.main.settings.name"))
                .build();
    }

    public static ItemStack deleteNpc() {
        return ItemBuilder.modern(Material.LAVA_BUCKET)
                .setDisplay(Msg.translated("customnpcs.menus.main.delete.name"))
                .build();
    }

    public static ItemStack looking() {
        return ItemBuilder.modern(Material.ENDER_EYE)
                .setDisplay(Msg.translated("customnpcs.menus.main.facing.name"))
                .setLore(Msg.lore("customnpcs.menus.main.facing.description"))
                .build();
    }

    public static Button interactable(InternalNpc npc) {
        boolean interactable = npc.getSettings().isInteractable();

        return Button.clickable(ItemBuilder.modern(interactable ? Material.OAK_SAPLING : Material.DEAD_BUSH)
                .setLore(interactable ? Msg.translated("customnpcs.menus.main.interactable.true") : Msg.translated("customnpcs.menus.main.interactable.false"))
                .setDisplay(Msg.translated("customnpcs.menus.main.interactable.name.toggle"))
                .build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            if (npc.getSettings().isInteractable())
                p.sendMessage(Msg.translated("customnpcs.menus.main.interactable.message.now_false"));
            else p.sendMessage(Msg.translated("customnpcs.menus.main.interactable.message.now_true"));

            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            menuView.replaceButton(25, MenuItems.interactable(npc));
            menuView.replaceButton(34, MenuItems.showActions(npc));
        }));
    }

    public static Button showActions(InternalNpc npc) {
        if (!npc.getSettings().isInteractable()) return MENU_GLASS;
        return Button.clickable(ItemBuilder.modern(Material.RECOVERY_COMPASS)
                .setDisplay(Msg.translated("customnpcs.menus.main.interactable.name"))
                .setLore(Msg.lore("customnpcs.menus.main.interactable.description"))
                .build(), new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static ItemStack editEquipment(InternalNpc npc) {
        Equipment equip = npc.getEquipment();
        return ItemBuilder.modern(Material.ARMOR_STAND)
                .setDisplay(Msg.translated("customnpcs.menus.main.items.equipment.name"))
                .setLore(Msg.translate("customnpcs.menus.main.items.equipment.main_hand", equip.getHand().getType().name().toLowerCase()),
                        Msg.translate("customnpcs.menus.main.items.equipment.off_hand", equip.getOffhand().getType().name().toLowerCase()),
                        Msg.translate("customnpcs.menus.main.items.equipment.helmet", equip.getHead().getType().name().toLowerCase()),
                        Msg.translate("customnpcs.menus.main.items.equipment.chestplate", equip.getChest().getType().name().toLowerCase()),
                        Msg.translate("customnpcs.menus.main.items.equipment.leggings", equip.getLegs().getType().name().toLowerCase()),
                        Msg.translate("customnpcs.menus.main.items.equipment.boots", equip.getBoots().getType().name().toLowerCase())
                ).build();
    }

    public static Button tunnelVision(InternalNpc npc) {
        return Button.clickable(ItemBuilder.modern(Material.SPYGLASS)
                .setDisplay(Msg.translated("customnpcs.menus.main.vision.name"))
                .setLore(npc.getSettings().isTunnelvision() ? Msg.translated("customnpcs.menus.main.vision.tunnel") : Msg.translated("customnpcs.menus.main.vision.normal"))
                .build(), ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            menuView.replaceButton(28, MenuItems.tunnelVision(npc));
        }));
    }

    public static ItemStack confirmCreation() {
        return ItemBuilder.modern(Material.LIME_DYE)
                .setDisplay(Msg.translated("customnpcs.menus.main.create.name"))
                .build();
    }

    public static ItemStack cancelCreation() {
        return ItemBuilder.modern(BARRIER)
                .setDisplay(Msg.translated("customnpcs.menus.main.cancel.name"))
                .build();
    }

    public static ItemStack importArmor() {
        return ItemBuilder.modern(Material.ARMOR_STAND)
                .setDisplay(Msg.translated("customnpcs.menus.equipment.import"))
                .setLore(Msg.translated("customnpcs.menus.equipment.import.description"))
                .build();
    }

    public static Button helmetSlot(InternalNpc npc) {
        ItemStack helm = npc.getEquipment().getHead();
        if (helm == null || helm.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.helmet.empty"))
                            .setLore(Msg.lore("customnpcs.menus.equipment.helmet.change"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == Material.AIR || event.isRightClick()) return;
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
                        npc.getEquipment().setHead(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        player.sendMessage(Msg.translated("customnpcs.menus.equipment.helmet.message.success", Component.text(npc.getEquipment().getHead().getType().name().toLowerCase())));
                        menuView.replaceButton(13, helmetSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.helmet.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(helm)
                            .setDisplay(Component.text(helm.getType().name().toLowerCase(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();

                        if (event.isRightClick()) {
                            npc.getEquipment().setHead(new ItemStack(Material.AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.helmet.reset"));
                            menuView.replaceButton(13, helmetSlot(npc));
                        } else {
                            if (event.getCursor().getType() == Material.AIR) return;
                            npc.getEquipment().setHead(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.helmet.message.success", Component.text(npc.getEquipment().getHead().getType().name().toLowerCase())));
                            menuView.replaceButton(13, helmetSlot(npc));
                        }
                    }));

        }
    }

    public static Button chestplateSlot(InternalNpc npc) {
        ItemStack cp = npc.getEquipment().getChest();
        if (cp.getType().isAir()) {

            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore("customnpcs.menus.equipment.chestplate.change"))
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.chestplate.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        Player player = (Player) event.getWhoClicked();
                        event.setCancelled(true);
                        if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                            npc.getEquipment().setChest(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.chestplate.message.success", npc.getEquipment().getChest().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.chestplate.message.error"));
                        }

                        menuView.replaceButton(22, chestplateSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.chestplate.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(cp)
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(cp.getType().toString(), NamedTextColor.GREEN))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setChest(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.chestplate.reset"));
                            menuView.replaceButton(22, chestplateSlot(npc));
                            return;
                        } else if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                            npc.getEquipment().setChest(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.chestplate.message.success", npc.getEquipment().getChest().getType().name().toLowerCase()));
                            return;
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                        }
                        event.setCancelled(true);
                        player.sendMessage(Msg.translated("customnpcs.menus.equipment.chestplate.message.error"));
                        menuView.replaceButton(22, chestplateSlot(npc));
                    }));
        }
    }

    public static Button leggingsSlot(InternalNpc npc) {
        ItemStack legs = npc.getEquipment().getLegs();
        if (legs.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(Material.LIME_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore("customnpcs.menus.equipment.legs.change"))
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.legs.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setLegs(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.legs.message.success", npc.getEquipment().getLegs().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.legs.message.error"));
                        }
                        menuView.replaceButton(31, leggingsSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.legs.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(legs)
                            .addFlags(ItemFlag.values())
                            .setDisplay(Component.text(legs.getType().toString(), NamedTextColor.GREEN))
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();

                        if (event.isRightClick()) {
                            npc.getEquipment().setLegs(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.legs.reset"));
                            menuView.replaceButton(31, leggingsSlot(npc));
                        } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setLegs(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.legs.message.success", npc.getEquipment().getLegs().getType().name().toLowerCase()));
                            menuView.replaceButton(31, leggingsSlot(npc));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.legs.message.error"));
                        }
                    }));
        }
    }

    public static Button bootsSlot(InternalNpc npc) {
        ItemStack boots = npc.getEquipment().getBoots();
        if (boots.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(LIME_STAINED_GLASS_PANE)
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.boots.empty"))
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore("customnpcs.menus.equipment.boots.change"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.getCursor().getType().name().contains("BOOTS")) {
                            npc.getEquipment().setBoots(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.boots.message.success", npc.getEquipment().getBoots().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.boots.message.error"));
                            return;
                        }
                        menuView.replaceButton(40, bootsSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.boots.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(boots)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(boots.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setBoots(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.boots.reset"));
                        } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                            npc.getEquipment().setBoots(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.boots.message.success", npc.getEquipment().getBoots().getType().name().toLowerCase()));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.boots.message.error"));
                            return;
                        }
                        menuView.replaceButton(40, bootsSlot(npc));
                    }));
        }
    }

    public static Button handSlot(InternalNpc npc) {
        ItemStack hand = npc.getEquipment().getHand();
        if (hand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE)
                            .addFlags(ItemFlag.values())
                            .setLore(Msg.lore("customnpcs.menus.equipment.hand.change"))
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.hand.empty"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == AIR) return;
                        npc.getEquipment().setHand(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(Msg.translate("customnpcs.menus.equipment.hand.message.success", npc.getEquipment().getHand().getType().name().toLowerCase()));
                        menuView.replaceButton(23, handSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.hand.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(hand)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(hand.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setHand(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.hand.reset"));
                            menuView.replaceButton(23, handSlot(npc));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            npc.getEquipment().setHand(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.hand.message.success", npc.getEquipment().getHand().getType().name().toLowerCase()));
                            menuView.replaceButton(23, handSlot(npc));
                        }
                    }));
        }
    }

    public static Button offhandSlot(InternalNpc npc) {
        ItemStack offhand = npc.getEquipment().getOffhand();
        if (offhand.getType().isAir()) {
            return Button.clickable(ItemBuilder.modern(YELLOW_STAINED_GLASS_PANE)
                            .setDisplay(Msg.translated("customnpcs.menus.equipment.offhand.empty"))
                            .setLore(Msg.lore("customnpcs.menus.equipment.offhand.change"))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.getCursor().getType() == AIR) return;
                        npc.getEquipment().setOffhand(event.getCursor().clone());
                        event.getCursor().setAmount(0);
                        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                        player.sendMessage(Msg.translate("customnpcs.menus.equipment.offhand.message.success", npc.getEquipment().getOffhand().getType().name().toLowerCase()));
                        menuView.replaceButton(21, offhandSlot(npc));
                    }));
        } else {
            List<Component> lore = Utils.list(Msg.lore("customnpcs.menus.equipment.hand.change"));
            lore.add(Msg.translated("customnpcs.remove.description"));
            return Button.clickable(ItemBuilder.modern(offhand)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(Component.text(offhand.getType().toString(), NamedTextColor.GREEN))
                            .addFlags(ItemFlag.values())
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        if (event.isRightClick()) {
                            npc.getEquipment().setOffhand(new ItemStack(AIR));
                            player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                            player.sendMessage(Msg.translated("customnpcs.menus.equipment.offhand.reset"));
                        } else {
                            if (event.getCursor().getType() == AIR) return;
                            npc.getEquipment().setOffhand(event.getCursor().clone());
                            event.getCursor().setAmount(0);
                            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                            player.sendMessage(Msg.translate("customnpcs.menus.equipment.offhand.message.success", npc.getEquipment().getOffhand().getType().name().toLowerCase()));
                        }
                        menuView.replaceButton(21, offhandSlot(npc));
                    }));
        }
    }

    public static Button toMain() {
        return Button.clickable(ItemBuilder.modern(BARRIER).setDisplay(Msg.translated("customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_MAIN));
    }

    public static Button toAction() {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translated("customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_ACTIONS));
    }

    public static Button toActionSaveConditions() {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translated("customnpcs.items.go_back")).build(),
                ButtonClickAction.plain((menu, event) -> {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.getLotus().openMenu(player, MenuUtils.NPC_ACTION_CUSTOMIZER);
                }));
    }

    public static Button toNewCondition() {
        return Button.clickable(ItemBuilder.modern(ARROW).setDisplay(Msg.translated("customnpcs.items.go_back")).build(),
                new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toConditionCustomizer() {
        return Button.clickable(ItemBuilder.modern(COMPARATOR).setDisplay(Msg.translated("customnpcs.menus.action_customizer.conditions")).build(),
                new OpenButtonAction(MenuUtils.NPC_CONDITION_CUSTOMIZER));
    }

    public static List<Button> currentActions(InternalNpc npc) {
        List<Button> buttons = new ArrayList<>();

        for (Action action : npc.getActions()) {
            buttons.add(Button.clickable(action.getFavicon(), ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                if (event.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    npc.removeAction(action);
                    menuView.getAPI().openMenu(player, MenuUtils.NPC_ACTIONS);
                } else if (event.isLeftClick()) {
                    if (CustomNPCs.ACTION_REGISTRY.canEdit(action.getClass())) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                        plugin.editingActions.put(player.getUniqueId(), action.clone());
                        plugin.originalEditingActions.put(player.getUniqueId(), action);
                        plugin.getLotus().openMenu(player, MenuUtils.NPC_ACTION_CUSTOMIZER);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        player.sendMessage(Msg.translated("customnpcs.edit.fail"));
                    }
                }
            })));
        }

        buttons.add(Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translated("customnpcs.menus.actions.new"))
                        .build(),
                new OpenButtonAction(MenuUtils.NPC_NEW_ACTION)
        ));

        return buttons;
    }

    public static Button delayDisplay(Action action) {
        return Button.clickable(ItemBuilder.modern(CLOCK)
                .setDisplay(Msg.translate("customnpcs.menus.action_customizer.delay.name", action.getDelay()))
                .build(), ButtonClickAction.plain((menuView, event) -> event.setCancelled(true)));
    }

    public static Button decrementDelay(Action action) {
        return Button.clickable(ItemBuilder.modern(RED_DYE)
                        .setDisplay(Msg.translated("customnpcs.menus.action_customizer.delay.decrement"))
                        .setLore(Msg.lore("customnpcs.menus.action_customizer.delay.decrement.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    if (action.getDelay() == 0) {
                        player.sendMessage(Msg.translated("customnpcs.menus.action_customizer.delay.error"));
                        return;
                    }
                    if (event.isShiftClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 20));
                    } else if (event.isLeftClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 1));
                    } else if (event.isRightClick()) {
                        action.setDelay(Math.max(0, action.getDelay() - 5));
                    }
                    menuView.updateButton(4, button -> button.setItem(delayDisplay(action).getItem()));
                }));
    }

    public static Button incrementDelay(Action action) {
        return Button.clickable(ItemBuilder.modern(LIME_DYE)
                        .setDisplay(Msg.translated("customnpcs.menus.action_customizer.delay.increment"))
                        .setLore(Msg.lore("customnpcs.menus.action_customizer.delay.increment.description"))
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
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    menuView.updateButton(4, button -> button.setItem(delayDisplay(action).getItem()));
                }));
    }

    public static Button saveAction(Action action) {
        return Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translated("customnpcs.menus.action_customizer.confirm"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());

                    if (npc == null) {
                        player.sendMessage(Msg.translated("customnpcs.menus.main.error.no_npc.lore"));
                        return;
                    }

                    if (CustomNPCs.getInstance().originalEditingActions.get(player.getUniqueId()) != null)
                        npc.removeAction(CustomNPCs.getInstance().originalEditingActions.remove(player.getUniqueId()));
                    npc.addAction(action);

                    menuView.getAPI().openMenu(player, MenuUtils.NPC_ACTIONS);
                }));
    }

    public static ItemStack genericDisplay(Component text, Component... lore) {
        return ItemBuilder.modern(CLOCK).setDisplay(text).setLore(lore).build();
    }

    public static Button saveCondition() {
        return Button.clickable(ItemBuilder.modern(LILY_PAD)
                        .setDisplay(Msg.translated("customnpcs.menus.main.create.name"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);

                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    Action actionImpl = plugin.editingActions.get(player.getUniqueId());
                    Condition original = plugin.originalEditingConditionals.get(player.getUniqueId());
                    if (original != null) actionImpl.removeCondition(original);
                    Condition edited = plugin.editingConditionals.get(player.getUniqueId());
                    actionImpl.addCondition(edited);
                    menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITIONS);
                }));
    }

    public static Button comparatorSwitcher(Condition condition) {

        List<Component> lore = new ArrayList<>();
        for (Condition.Comparator c : Condition.Comparator.values()) {
            if (condition.getType() == Condition.Type.NUMERIC || (condition.getType() == Condition.Type.LOGICAL && c.isStrictlyLogical())) {
                if (condition.getComparator() != c) lore.add(Msg.translated(c.getKey()).color(NamedTextColor.GREEN));
                else lore.add(Component.text("▸ ", NamedTextColor.DARK_AQUA).append(Msg.translated(c.getKey())));
            }
        }
        lore.add(Msg.translated("customnpcs.items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translated("customnpcs.comparator"))
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
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.replaceButton(11, comparatorSwitcher(condition));
        }));
    }

    public static Button targetValueSelector(Condition condition) {
        ItemStack i = ItemBuilder.modern(OAK_HANGING_SIGN)
                .setDisplay(Msg.translated("customnpcs.value.select"))
                .setLore(Msg.translate("customnpcs.value.current", condition.getTarget()),
                        Msg.translated("customnpcs.items.click_to_change"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            player.closeInventory();
            plugin.targetWaiting.add(player.getUniqueId());
            new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
        }));
    }

    public static Button valueSwitcher(Condition condition) {
        List<Component> lore = new ArrayList<>();

        for (Condition.Value v : Condition.Value.values()) {
            //todo: re-evaluate this
            if (v.isLogical() && condition.getType() != Condition.Type.LOGICAL) continue;
            if (!v.isLogical() && condition.getType() != Condition.Type.NUMERIC) continue;


            if (condition.getValue() != v)
                lore.add(Msg.translated(v.getTranslationKey()).color(NamedTextColor.GREEN));
            else
                lore.add(Component.text("▸ ", NamedTextColor.DARK_AQUA).append(Msg.translated(v.getTranslationKey())));

        }
        lore.add(Msg.translated("customnpcs.items.click_to_change"));

        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translated("customnpcs.statistic"))
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
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.updateButton(15, button -> button.setItem(valueSwitcher(condition).getItem()));
        }));
    }

    public static Button interactableHologram(InternalNpc npc) {
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        ItemStack i = ItemBuilder.modern(hideClickableTag ? RED_CANDLE : GREEN_CANDLE)
                .setDisplay(Msg.translated("customnpcs.menus.extra.hologram_visibility"))
                .setLore(
                        Component.empty(),
                        Msg.translated("customnpcs.menus.extra.hologram_visibility.description"),
                        hideClickableTag ? Msg.translated("customnpcs.menus.extra.hologram_visibility.description.hidden") : Msg.translated("customnpcs.menus.extra.hologram_visibility.description.shown")
                ).build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            npc.getSettings().setHideClickableHologram(!hideClickableTag);
            menuView.replaceButton(12, interactableHologram(npc));
        }));
    }

    public static Button interactableText() {
        ItemStack i = ItemBuilder.modern(NAME_TAG)
                .setDisplay(Msg.translated("customnpcs.menus.extra.hologram_text"))
                .setLore(Msg.lore("customnpcs.menus.extra.hologram_text.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            plugin.hologramWaiting.add(player.getUniqueId());

            player.closeInventory();
            player.sendMessage(Msg.translated("customnpcs.menus.extra.hologram_text.type"));
            new InteractableHologramRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
        }));
    }

    public static Button importPlayer() {
        ItemStack i = ItemBuilder.modern(ANVIL)
                .setDisplay(Msg.translated("customnpcs.menus.skins.player"))
                .setLore(Msg.lore("customnpcs.menus.skins.player.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            player.closeInventory();

            plugin.playerWaiting.add(player.getUniqueId());
            new PlayerNameRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);
        }));
    }

    public static Button useCatalog() {
        ItemStack i = ItemBuilder.modern(ARMOR_STAND)
                .setDisplay(Msg.translated("customnpcs.menus.skins.catalog"))
                .setLore(Msg.lore("customnpcs.menus.skins.catalog.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            try {
                CustomNPCs.getInstance().skinCatalogue.open(player);
            } catch (InvalidPageException e) {
                player.sendMessage(Msg.translated("customnpcs.error.cant_open_skin_catalog"));
                CustomNPCs.getInstance().getLogger().log(Level.SEVERE, "An error occurred whilst opening the Skin Catalog!", e);
            }
        }));
    }

    public static Button importUrl() {
        ItemStack i = ItemBuilder.modern(WRITABLE_BOOK)
                .setDisplay(Msg.translated("customnpcs.menus.skins.url"))
                .setLore(Msg.lore("customnpcs.menus.skins.url.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            player.closeInventory();

            plugin.urlWaiting.add(player.getUniqueId());
            new UrlRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);
        }));
    }

    public static List<Button> conditions(Action action) {
        List<Button> buttons = new ArrayList<>();

        if (action.getConditions() == null) {
            return Utils.list(newCondition(action));
        }

        for (Condition condition : action.getConditions()) {
            boolean logical = condition.getType() == Condition.Type.LOGICAL;
            ItemStack i = ItemBuilder.modern(logical ? COMPARATOR : POPPED_CHORUS_FRUIT)
                    .setDisplay(logical ? Msg.translated("customnpcs.menus.conditions.logical") : Msg.translated("customnpcs.menus.conditions.numeric"))
                    .setLore(
                            Component.empty(),
                            Msg.translated("customnpcs.menus.conditions.comparator", Msg.translated(condition.getComparator().getKey())),
                            Msg.translated("customnpcs.menus.conditions.value", Msg.translated(condition.getValue().getTranslationKey())),
                            Msg.translate("customnpcs.menus.conditions.target", condition.getTarget()),
                            Component.empty(),
                            Msg.translated("customnpcs.favicons.remove"),
                            Msg.translated("customnpcs.favicons.edit")
                    ).build();

            buttons.add(Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();

                if (event.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    action.removeCondition(condition);
                    menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITIONS);
                } else {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.editingConditionals.put(player.getUniqueId(), condition.clone());
                    plugin.originalEditingConditionals.put(player.getUniqueId(), condition);
                    menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITION_CUSTOMIZER);
                }
            })));
        }
        buttons.add(newCondition(action));
        return buttons;
    }

    public static Button newCondition(Action action) {
        ItemStack i = ItemBuilder.modern(LILY_PAD)
                .setDisplay(Msg.translated("customnpcs.menus.conditions.new_condition"))
                .build();
        return Button.clickable(i, new OpenButtonAction(MenuUtils.NPC_NEW_CONDITION));
    }

    public static Button toggleConditionMode(Action action) {
        boolean isAll = action.getMode() == Condition.SelectionMode.ALL;
        ItemStack i = ItemBuilder.modern(isAll ? GREEN_CANDLE : RED_CANDLE)
                .setDisplay(Msg.translated("customnpcs.menus.conditions.mode.toggle"))
                .setLore(isAll ? Msg.translated("customnpcs.menus.conditions.mode.all") : Msg.translated("customnpcs.menus.conditions.mode.one"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            action.setMode(isAll ? Condition.SelectionMode.ONE : Condition.SelectionMode.ALL);
            menuView.replaceButton(35, toggleConditionMode(action));
        }));
    }

    public static Button toCondition() {
        ItemStack i = ItemBuilder.modern(ARROW)
                .setDisplay(Msg.translated("customnpcs.items.go_back"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            event.setCancelled(true);
            menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button editConditions() {
        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translated("customnpcs.menus.action_customizer.conditions"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITIONS);
        }));
    }

    public static Button numeric() {
        ItemStack i = ItemBuilder.modern(POPPED_CHORUS_FRUIT)
                .setDisplay(Msg.translated("customnpcs.menus.conditions.new.numeric"))
                .setLore(Msg.lore("customnpcs.menus.conditions.new.numeric.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new NumericCondition(Condition.Comparator.EQUAL_TO, Condition.Value.EXP_LEVELS, 0.0);
            plugin.originalEditingConditionals.remove(player.getUniqueId());
            plugin.editingConditionals.put(player.getUniqueId(), conditional);
            menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }

    public static Button logic() {
        ItemStack i = ItemBuilder.modern(COMPARATOR)
                .setDisplay(Msg.translated("customnpcs.menus.conditions.new.logical"))
                .setLore(Msg.lore("customnpcs.menus.conditions.new.logical.description"))
                .build();

        return Button.clickable(i, ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            Condition conditional = new LogicalCondition(Condition.Comparator.EQUAL_TO, Condition.Value.GAMEMODE, "CREATIVE");
            plugin.originalEditingConditionals.remove(player.getUniqueId());
            plugin.editingConditionals.put(player.getUniqueId(), conditional);
            menuView.getAPI().openMenu(player, MenuUtils.NPC_CONDITION_CUSTOMIZER);
        }));
    }
}
