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

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.OpenButtonAction;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.content.ContentBuilder;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

/**
 * The class representing the main NPC menu
 */
@NotNullByDefault
public class MainNPCMenu implements Menu<Component> {

    @Override
    public String name() {
        return MenuUtils.NPC_MAIN;
    }


    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.main.title");
    }


    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(5);
    }


    @Override
    public Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npc = plugin.getEditingNPCs().getIfPresent(view.viewer().getUniqueId());
        if (npc == null) {
            return Content.builder(view.capacity())
                    .set(Slot.of(22), Button.clickable(
                            ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                                    .displayName(Msg.get(view.viewer(), "menus.main.error.no_npc"))
                                    .lore(Msg.lore(view.viewer().locale(), "menus.main.error.no_npc.lore"))
                                    .build(),
                            (v, _) -> v.viewer().closeInventory()))
                    .build();
        }


        ContentBuilder builder = Content.builder(view.capacity());
        builder.fillAll(MenuItems.MENU_GLASS)
                .set(Slot.of(10), Button.clickable(MenuItems.looking(view.viewer()), (v, e) -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                    plugin.wait(player, WaitingType.FACING);
                    player.closeInventory();
                }))
                .set(Slot.of(8), Button.clickable(MenuItems.extraSettings(player), new OpenButtonAction(MenuUtils.NPC_EXTRA_SETTINGS)))
                .set(Slot.of(0), MenuItems.toPose(player))
                .set(Slot.of(13), Button.clickable(MenuItems.skinSelection(npc, player), new OpenButtonAction(MenuUtils.NPC_SKIN)))
                .set(Slot.of(16), MenuItems.changeLines(npc, player))
                .set(Slot.of(19), Button.clickable(MenuItems.editEquipment(npc, player), new OpenButtonAction(MenuUtils.NPC_EQUIPMENT)))
                .set(Slot.of(22), MenuItems.resilient(npc, player))
                .set(Slot.of(25), MenuItems.interactable(npc, player))
                .set(Slot.of(34), MenuItems.showActions(npc, player))
                .set(Slot.of(28), MenuItems.tunnelVision(npc, player))
                .set(Slot.of(31), Button.clickable(MenuItems.confirmCreation(player), (v, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), 1);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 3);
                    Bukkit.getScheduler().runTaskLater(plugin, npc::createNPC, 1);
                    p.spawnParticle(npc.getSpawnParticle(), npc.getSpawnLoc().clone().add(0, 1, 0), 1);

                    if (npc.getSettings().isResilient())
                        p.sendMessage(Msg.get(player, "menus.main.create.message.resilient"));
                    else
                        p.sendMessage(Msg.get(player, "menus.main.create.message.temporary"));

                    npc.reloadSettings();

                    p.closeInventory();
                }))
                .set(Slot.of(36), Button.clickable(MenuItems.cancelCreation(player), (_, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
                    p.sendMessage(Msg.get(player, "menus.main.cancel.message"));
                    p.closeInventory();
                }));
        if (plugin.getNPCByID(npc.getUniqueID()) != null)
            builder.set(Slot.of(44), Button.clickable(MenuItems.deleteNpc(player), (_, event) -> {
                Player p = (Player) event.getWhoClicked();
                plugin.getDeletionReason().put(p.getUniqueId(), true);
                plugin.getLotus().openMenu(p, MenuUtils.NPC_DELETE);
                p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }));
        return builder.build();
    }
}
