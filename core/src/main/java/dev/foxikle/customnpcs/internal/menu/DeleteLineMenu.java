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
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

import java.util.ArrayList;
import java.util.List;

@NotNullByDefault
public class DeleteLineMenu implements Menu<Component> {
    @Override
    public String name() {
        return MenuUtils.NPC_DELETE_LINE;
    }

    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.hologram.delete.title");
    }

    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(3);
    }

    @Override
    public Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();
        CustomNPCs plugin = CustomNPCs.getInstance();
        InternalNpc npcFor = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
        int index = HologramMenu.editingIndicies.getOrDefault(player.getUniqueId(), -1);

        if (npcFor == null || index < 0 || npcFor.getSettings().getHolograms().size() <= index) {
            player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
            return MenuUtils.invalidNpc(view);
        }

        return Content.builder(view.capacity())
                .fillAll(MenuItems.MENU_GLASS)
                .set(Slot.of(11), Button.clickable(
                        ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                                .displayName(Msg.get(player, "menus.hologram.delete.confirm"))
                                .lore(Msg.lore(player.locale(), "menus.hologram.delete.confirm.lore", Arg.arg(npcFor.getSettings().getHolograms().get(index))))
                                .build(), (_, e) -> {

                            Player p = (Player) e.getWhoClicked();
                            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(p.getUniqueId());

                            if (npc == null) {
                                p.closeInventory();
                                p.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                return;
                            }

                            List<String> mutable = new ArrayList<>(npc.getSettings().getRawHolograms());
                            if (index >= mutable.size()) {
                                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                                p.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                                return;
                            }
                            player.playSound(p.getLocation(), Sound.ITEM_TRIDENT_HIT, 1F, 1F);
                            npc.getSettings().getRawHolograms().remove(index);
                            plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                        }))
                .set(Slot.of(15), Button.clickable(ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                        .displayName(Msg.get(player, "items.go_back"))
                        .lore(Msg.get(player, "menus.delete.to_safety")).build(), (_, e) -> {
                    InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                    Player p = (Player) e.getWhoClicked();
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1, 1);

                    if (npc == null) {
                        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                        return;
                    }

                    plugin.getLotus().openMenu(p, MenuUtils.NPC_HOLOGRAMS);
                })).build();
    }
}
