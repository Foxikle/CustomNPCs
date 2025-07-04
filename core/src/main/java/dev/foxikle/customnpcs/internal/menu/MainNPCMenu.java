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

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.runnables.FacingDirectionRunnable;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.OpenButtonAction;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The class representing the main NPC menu
 */
public class MainNPCMenu implements Menu {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return MenuUtils.NPC_MAIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
        return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.main.title"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
        return Capacity.ofRows(5);
    }

    /**
     * {@inheritDoc}
     */
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


        Content.Builder builder = Content.builder(capacity);
        builder.apply(content -> content.fill(MenuItems.MENU_GLASS))
                .setButton(10, Button.clickable(MenuItems.looking(player), ButtonClickAction.plain((menuView, inventoryClickEvent) -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.wait(player, WaitingType.FACING);
                    new FacingDirectionRunnable(plugin, player).go();
                    player.closeInventory();
                })))
                .setButton(8, Button.clickable(MenuItems.extraSettings(player), new OpenButtonAction(MenuUtils.NPC_EXTRA_SETTINGS)))
                .setButton(0, MenuItems.toPose(player))
                .setButton(13, Button.clickable(MenuItems.skinSelection(npc, player), new OpenButtonAction(MenuUtils.NPC_SKIN)))
                .setButton(16, MenuItems.changeLines(npc, player))
                .setButton(19, Button.clickable(MenuItems.editEquipment(npc, player), new OpenButtonAction(MenuUtils.NPC_EQUIPMENT)))
                .setButton(22, MenuItems.resilient(npc, player))
                .setButton(25, MenuItems.interactable(npc, player))
                .setButton(34, MenuItems.showActions(npc, player))
                .setButton(28, MenuItems.tunnelVision(npc, player))
                .setButton(31, Button.clickable(MenuItems.confirmCreation(player), ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 3);
                    Bukkit.getScheduler().runTaskLater(plugin, npc::createNPC, 1);
                    p.spawnParticle(npc.getSpawnParticle(), npc.getSpawnLoc().clone().add(0, 1, 0), 1);

                    if (npc.getSettings().isResilient())
                        p.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.main.create.message.resilient"));
                    else
                        p.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.main.create.message.temporary"));

                    npc.reloadSettings();

                    p.closeInventory();
                })))
                .setButton(36, Button.clickable(MenuItems.cancelCreation(player), ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
                    p.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.main.cancel.message"));
                    p.closeInventory();
                })));
        if (plugin.getNPCByID(npc.getUniqueID()) != null)
            builder.setButton(44, Button.clickable(MenuItems.deleteNpc(player), ButtonClickAction.plain((menu, event) -> {
                Player p = (Player) event.getWhoClicked();
                plugin.getDeltionReason().put(p.getUniqueId(), true);
                plugin.getLotus().openMenu(p, MenuUtils.NPC_DELETE);
                p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            })));
        return builder.build();
    }
}
