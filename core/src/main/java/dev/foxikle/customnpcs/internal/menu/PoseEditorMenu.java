/*
 * Copyright (c) 2025. Foxikle
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

import dev.foxikle.customnpcs.api.Pose;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.runnables.NudgeRunnable;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.DataRegistry;
import io.github.mqzen.menus.misc.button.Button;
import io.github.mqzen.menus.misc.button.actions.ButtonClickAction;
import io.github.mqzen.menus.misc.button.actions.impl.CloseMenuAction;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import io.github.mqzen.menus.titles.MenuTitle;
import io.github.mqzen.menus.titles.MenuTitles;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PoseEditorMenu implements Menu {
    public static final Map<UUID, InternalNpc> previewNPCs = new HashMap<>();

    @Override
    public String getName() {
        return MenuUtils.NPC_POSE;
    }

    @Override
    public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
        return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.pose.title"));
    }

    @Override
    public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
        return Capacity.ofRows(3);
    }

    @Override
    public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {

        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
        if (npc == null) {
            return Content.builder(capacity)
                    .setButton(22, Button.clickable(
                            ItemBuilder.modern(Material.RED_STAINED_GLASS_PANE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.main.error.no_npc"))
                                    .setLore(Msg.lore(player.locale(), "customnpcs.menus.main.error.no_npc.lore"))
                                    .build(),
                            new CloseMenuAction()
                    ))
                    .build();
        }

        Button nudgeButton = Button.clickable(
                ItemBuilder.modern(Material.RECOVERY_COMPASS)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.nudge.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.nudge"))
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    final InternalNpc finalClickedNpc = clickedNpc.clone();
                    finalClickedNpc.getSettings().setResilient(false);
                    Optional.ofNullable(plugin.getNPCByID(clickedNpc.getUniqueID()))
                            .ifPresent(internalNpc -> {
                                internalNpc.remove();
                                finalClickedNpc.createNPC();
                                previewNPCs.put(player.getUniqueId(), finalClickedNpc);
                            });

                    plugin.wait(player, WaitingType.NUDGE);
                    new NudgeRunnable(player, plugin).runTaskTimer(plugin, 1, 15);
                    player.closeInventory();
                })
        );

        Button standing = Button.clickable(
                ItemBuilder.modern(Material.ARMOR_STAND)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.standing.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.standing"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.STANDING ? 1 : 0)
                        .addFlags(ItemFlag.values())
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.STANDING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "standing"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.STANDING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                })
        );

        Button sitting = Button.clickable(
                ItemBuilder.modern(Material.OAK_STAIRS)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.sitting.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.sitting"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.SITTING ? 1 : 0)
                        .addFlags(ItemFlag.values())
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.SITTING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "sitting"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.SITTING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                })
        );

        Button swimming = Button.clickable(
                ItemBuilder.modern(Material.WATER_BUCKET)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.swimming.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.swimming"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.SWIMMING ? 1 : 0)
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.SWIMMING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "swimming"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.SWIMMING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                })
        );

        Button crouching = Button.clickable(
                ItemBuilder.modern(Material.SMOOTH_QUARTZ_SLAB)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.crouching.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.crouching"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.CROUCHING ? 1 : 0)
                        .addFlags(ItemFlag.values())
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.CROUCHING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "crouching"));
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.CROUCHING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                })
        );

        Button sleeping = Button.clickable(
                ItemBuilder.modern(Material.RED_BED)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.sleeping.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.sleeping"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.SLEEPING ? 1 : 0)
                        .addFlags(ItemFlag.values())
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.SLEEPING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "sleeping"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.SLEEPING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);
                })
        );

        Button dying = Button.clickable(
                ItemBuilder.modern(Material.LAVA_BUCKET)
                        .setLore(Msg.lore(player.locale(), "customnpcs.menus.pose.dying.lore"))
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.pose.dying"))
                        .enchant(Enchantment.MENDING, npc.getSettings().getPose() == Pose.DYING ? 1 : 0)
                        .addFlags(ItemFlag.values())
                        .build(),
                ButtonClickAction.plain((menu, event) -> {
                    InternalNpc clickedNpc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    event.setCancelled(true);
                    if (clickedNpc == null) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.error.npc-menu-expired "));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    if (clickedNpc.getSettings().getPose() == Pose.DYING) {
                        player.sendMessage(Msg.translate(player.locale(), "customnpcs.pose.already", "dying"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                        return;
                    }
                    clickedNpc.getSettings().setPose(Pose.DYING);
                    plugin.getLotus().openMenu((Player) event.getWhoClicked(), MenuUtils.NPC_MAIN);
                    player.playSound(player, Sound.ENTITY_VILLAGER_CELEBRATE, 1.0F, 1.0F);

                })
        );

        return Content.builder(capacity)
                .apply(content -> {
                    content.fillBorder(MenuItems.MENU_GLASS);
                    content.addButton(standing, sitting, crouching, swimming, sleeping, dying);
                })
                .setButton(18, MenuItems.toMain(player))
                .setButton(8, nudgeButton)
                .build();
    }
}
