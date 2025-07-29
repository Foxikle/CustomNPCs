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

package dev.foxikle.customnpcs.actions.defaultImpl;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.runnables.ServerRunnable;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.DataRegistry;
import io.github.mqzen.menus.misc.button.Button;
import io.github.mqzen.menus.misc.button.actions.ButtonClickAction;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import io.github.mqzen.menus.titles.MenuTitle;
import io.github.mqzen.menus.titles.MenuTitles;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.GRASS_BLOCK;
import static org.bukkit.Material.OAK_HANGING_SIGN;

@Getter
@Setter
public class SendServer extends Action {

    private String server;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param server The raw message
     */
    public SendServer(String server, int delay, Condition.SelectionMode mode, List<Condition> conditionals, int cooldown) {
        super(delay, mode, conditionals, cooldown);
        this.server = server;
    }

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param server The raw message
     * @deprecated Use {@link SendServer#SendServer(String, int, Condition.SelectionMode, List, int)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public SendServer(String server, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals, 0);
        this.server = server;
    }

    public static Button creationButton(Player player) {
        return
                Button.clickable(ItemBuilder.modern(GRASS_BLOCK)
                                .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.server"))
                                .setLore(Msg.lore(player.locale(), "customnpcs.favicons.server.description"))
                                .build(),
                        ButtonClickAction.plain((menuView, event) -> {
                            event.setCancelled(true);
                            Player p = (Player) event.getWhoClicked();
                            p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            //todo: watch out for duplications

                            SendServer actionImpl = new SendServer("server", 0, Condition.SelectionMode.ONE, new ArrayList<>(), 0);
                            CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                            menuView.getAPI().openMenu(p, actionImpl.getMenu());
                        }));
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(SendServer.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + SendServer.class.getName());
        }
        String server = parseString(serialized, "server");
        ParseResult pr = parseBase(serialized);

        SendServer message = new SendServer(server, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown());

        return clazz.cast(message);
    }

    /**
     * Sends a message to the player
     *
     * @param npc    The NPC
     * @param menu   The menu
     * @param player The player
     */
    @Override
    public void perform(InternalNpc npc, Menu menu, Player player) {
        if (!processConditions(player)) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player.getName());
        out.writeUTF(server);
        player.sendPluginMessage(CustomNPCs.getInstance(), "BungeeCord", out.toByteArray());
        activateCooldown(player.getUniqueId());
    }

    @Override
    public String serialize() {
        Map<String, Object> params = new HashMap<>();
        params.put("server", server);
        return generateSerializedString("SendServer", params);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(GRASS_BLOCK).setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.server"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.server.target", server),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new SendServerCustomizer(this);
    }

    @Override
    public Action clone() {
        return new SendServer(server, getDelay(), getMode(), getConditions(), getCooldown());
    }

    public class SendServerCustomizer implements Menu {

        private final SendServer action;

        public SendServerCustomizer(SendServer action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "SEND_SERVER_CUSTOMIZER";
        }

        @Override
        public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
            return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            return MenuUtils.actionBase(action, player)
                    .setButton(22, Button.clickable(ItemBuilder.modern(OAK_HANGING_SIGN)
                                    .setDisplay(Component.text(getServer()))
                                    .setLore(Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                Player p = (Player) event.getWhoClicked();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.SERVER);
                                new ServerRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            })))
                    .build();
        }
    }
}
