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

import dev.foxikle.customnpcs.api.events.NpcDeleteEvent;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;


@NotNullByDefault
public class DeleteMenu implements Menu<Component> {


    @Override
    public String name() {
        return MenuUtils.NPC_DELETE;
    }

    @Override
    public Component title(MenuView<Component, ?> view) {
        return Msg.get(view.viewer(), "menus.delete.title");
    }

    @Override
    public Capacity capacity(MenuView<Component, ?> view) {
        return Capacity.ofRows(3);
    }

    @Override
    public Content content(MenuView<Component, ?> view) {
        Player player = view.viewer();
        return Content.builder(view.capacity())
            .fillAll(MenuItems.MENU_GLASS)
            .set(Slot.of(11), Button.clickable(
                ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                    .displayName(Msg.get(player, "menus.delete.items.confirm.name"))
                    .lore(Component.empty(), Msg.get(player, "menus.delete.items.confirm.lore"))
                    .build(), (v, e) -> {
                    CustomNPCs plugin = CustomNPCs.getInstance();
                    Player player1 = (Player) e.getWhoClicked();
                    InternalNpc dontUse = v.data().require(InternalNpc.KEY);

                    // we have to do this because the npcs stored in the cache are clones, and are not actually in
                    // the world.
                    InternalNpc npc = plugin.getNPCByID(dontUse.getUniqueID());

                    if (npc == null) {
                        player1.closeInventory();
                        Utils.sound(v, Sound.ENTITY_VILLAGER_NO);
                        player1.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                        return;
                    }

                    NpcDeleteEvent.DeletionSource source = v.data().get(NpcDeleteEvent.DeletionSource.KEY)
                        .orElse(NpcDeleteEvent.DeletionSource.UNKNOWN);


                    NpcDeleteEvent event = new NpcDeleteEvent(player1, npc, source);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        player1.sendMessage(Msg.get(player, "delete.failed_api"));
                        return;
                    }

                    npc.remove();
                    npc.delete();
                    plugin.npcs.remove(npc.getUniqueID());
                    player1.sendMessage(Msg.get(player, "delete.success",
                        Arg.arg(npc.getSettings().getRawHolograms().getFirst())));
                    player1.closeInventory();
                    Utils.sound(view, Sound.BLOCK_END_PORTAL_SPAWN);
                    npc.getCurrentLocation().getWorld().strikeLightningEffect(npc.getCurrentLocation());
                }))
            .set(Slot.of(15), Button.clickable(
                ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                    .displayName(Msg.get(player, "items.go_back"))
                    .lore(Msg.get(player, "menus.delete.items.to_safety"))
                    .build(), (v, _) -> {
                    Utils.sound(view, Sound.UI_BUTTON_CLICK);

                    NpcDeleteEvent.DeletionSource source = v.data().get(NpcDeleteEvent.DeletionSource.KEY)
                        .orElse(NpcDeleteEvent.DeletionSource.UNKNOWN);

                    if (source == NpcDeleteEvent.DeletionSource.MENU) {
                        MenuUtils.open(v.viewer(), MenuUtils.NPC_MAIN, view.data());
                        return;
                    }
                    v.viewer().closeInventory();
                })).build();
    }
}
