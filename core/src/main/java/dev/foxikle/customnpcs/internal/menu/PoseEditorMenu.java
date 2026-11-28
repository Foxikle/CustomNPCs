/*
 * Copyright (c) 2025-2026. Foxikle
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
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.Result;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.data.Key;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

import java.util.List;
import java.util.Optional;

@NotNullByDefault
public class PoseEditorMenu implements Menu<Component> {
    public static final Key<InternalNpc> PREVIEW_NPC = Key.of("preview_npc", InternalNpc.class);

    @Override
    public String name() {
        return MenuUtils.NPC_POSE;
    }

    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.pose.title");
    }

    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(3);
    }

    @Override
    public Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = view.data().require(InternalNpc.KEY);
        
        Button nudgeButton = Button.clickable(
            ItemBuilder.of(Material.RECOVERY_COMPASS)
                .lore(Msg.lore(player.locale(), "menus.pose.nudge.lore"))
                .displayName(Msg.get(player, "menus.pose.nudge"))
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                final InternalNpc finalClickedNpc = clickedNpc.clone();
                finalClickedNpc.getSettings().setResilient(false);
                Optional.ofNullable(plugin.getNPCByID(clickedNpc.getUniqueID()))
                    .ifPresent(internalNpc -> {
                        internalNpc.remove();
                        finalClickedNpc.createNPC();
                        view.data().put(PREVIEW_NPC, finalClickedNpc);
                    });

                Await.signal(player, WaitingType.NUDGE, view.data())
                    .thenAccept(result -> {
                        switch (result) {
                            case Result.Cancelled<?> _ -> MenuUtils.open(view);
                            case Result.Failed<?> res -> MenuUtils.awaitFail(res, view);
                            case Result.Ok<?> _ -> {
                                InternalNpc npc_ = view.data().getOrNull(InternalNpc.KEY);
                                InternalNpc previewNpc = view.data().getOrNull(PREVIEW_NPC);
                                if (previewNpc == null || npc_ == null) {
                                    view.viewer().sendMessage(Msg.get(view.viewer(), "await.error", Arg.arg("NPC is null")));
                                    return;
                                }

                                Location finalLoc = previewNpc.getCurrentLocation();
                                finalLoc.setPitch(npc_.getSpawnLoc().getPitch());
                                finalLoc.setYaw(npc_.getSpawnLoc().getYaw());
                                npc_.setSpawnLoc(finalLoc);
                                previewNpc.remove();
                                npc_.createNPC();
                                MenuUtils.open(view);
                            }
                        }
                    });
            }
        );

        Button standing = Button.clickable(
            ItemBuilder.of(Material.ARMOR_STAND)
                .lore(Msg.lore(player.locale(), "menus.pose.standing.lore"))
                .displayName(Msg.get(player, "menus.pose.standing"))
                .glowing(npc.getSettings().getPose() == Pose.STANDING)
                .addFlags(ItemFlag.values())
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.STANDING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("standing")));
                    Utils.sound(menu, Sound.ENTITY_VILLAGER_NO);
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.STANDING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        Button sitting = Button.clickable(
            ItemBuilder.of(Material.OAK_STAIRS)
                .lore(Msg.lore(player.locale(), "menus.pose.sitting.lore"))
                .displayName(Msg.get(player, "menus.pose.sitting"))
                .glowing(npc.getSettings().getPose() == Pose.SITTING)
                .addFlags(ItemFlag.values())
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.SITTING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("sitting")));
                    Utils.sound(menu, Sound.ENTITY_VILLAGER_NO);
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.SITTING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        Button swimming = Button.clickable(
            ItemBuilder.of(Material.WATER_BUCKET)
                .lore(Msg.lore(player.locale(), "menus.pose.swimming.lore"))
                .displayName(Msg.get(player, "menus.pose.swimming"))
                .glowing(npc.getSettings().getPose() == Pose.SWIMMING)
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.SWIMMING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("swimming")));
                    Utils.sound(menu, Sound.ENTITY_VILLAGER_NO);
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.SWIMMING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        Button crouching = Button.clickable(
            ItemBuilder.of(Material.SMOOTH_QUARTZ_SLAB)
                .lore(Msg.lore(player.locale(), "menus.pose.crouching.lore"))
                .displayName(Msg.get(player, "menus.pose.crouching"))
                .glowing(npc.getSettings().getPose() == Pose.CROUCHING)
                .addFlags(ItemFlag.values())
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.CROUCHING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("crouching")));
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.CROUCHING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        Button sleeping = Button.clickable(
            ItemBuilder.of(Material.RED_BED)
                .lore(Msg.lore(player.locale(), "menus.pose.sleeping.lore"))
                .displayName(Msg.get(player, "menus.pose.sleeping"))
                .glowing(npc.getSettings().getPose() == Pose.SLEEPING)
                .addFlags(ItemFlag.values())
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.SLEEPING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("sleeping")));
                    Utils.sound(menu, Sound.ENTITY_VILLAGER_NO);
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.SLEEPING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        Button dying = Button.clickable(
            ItemBuilder.of(Material.LAVA_BUCKET)
                .lore(Msg.lore(player.locale(), "menus.pose.dying.lore"))
                .displayName(Msg.get(player, "menus.pose.dying"))
                .glowing(npc.getSettings().getPose() == Pose.DYING)
                .addFlags(ItemFlag.values())
                .build(),
            (menu, event) -> {
                InternalNpc clickedNpc = menu.data().require(InternalNpc.KEY);
                event.setCancelled(true);
                if (clickedNpc.getSettings().getPose() == Pose.DYING) {
                    player.sendMessage(Msg.get(player, "pose.already", Arg.arg("dying")));
                    Utils.sound(menu, Sound.ENTITY_VILLAGER_NO);
                    return;
                }
                clickedNpc.getSettings().setPose(Pose.DYING);
                MenuUtils.open(player, MenuUtils.NPC_MAIN, view.data());
                Utils.sound(menu, Sound.ENTITY_VILLAGER_CELEBRATE);
            }
        );

        return Content.builder(view.capacity())
            .fillBorder(MenuItems.MENU_GLASS)
            .apply(content -> List.of(standing, sitting, crouching, swimming, sleeping, dying).forEach(content::add))
            .set(Slot.of(18), MenuItems.toMain(view))
            .set(Slot.of(8), nudgeButton)
            .build();
    }
}
