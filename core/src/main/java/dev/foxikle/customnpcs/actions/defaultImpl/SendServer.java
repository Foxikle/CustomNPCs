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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.GRASS_BLOCK;
import static org.bukkit.Material.OAK_HANGING_SIGN;

@Getter
@Setter
public class SendServer extends Action {

    public static final Button CREATION_BUTTON = Button.clickable(ItemBuilder.modern(GRASS_BLOCK)
                    .setDisplay(Msg.translate("customnpcs.favicons.server"))
                    .setLore(Msg.lore("customnpcs.favicons.server.description"))
                    .build(),
            ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                //todo: watch out for duplications

                SendServer actionImpl = new SendServer("server", 0, Condition.SelectionMode.ONE, new ArrayList<>());
                CustomNPCs.getInstance().editingActions.put(player.getUniqueId(), actionImpl);
                menuView.getAPI().openMenu(player, actionImpl.getMenu());
            }));

    private String server;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param server The raw message
     */
    public SendServer(String server, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.server = server;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(SendServer.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + SendServer.class.getName());
        }
        String server = serialized.replaceAll(".*server=`(.*?)`.*", "$1");
        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        SendServer message = new SendServer(server, delay, mode, conditions);

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
    }

    @Override
    public String serialize() {
        return "SendServer{server=`" + server + "`, delay=" + getDelay() + ", mode=" + getMode().name() +
                ", conditions=" + getConditionSerialized() + "}";
    }

    @Override
    public ItemStack getFavicon() {
        return ItemBuilder.modern(GRASS_BLOCK).setDisplay(Msg.translate("customnpcs.favicons.server"))
                .setLore(
                        Msg.translate("customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate("customnpcs.favicons.server.target", server),
                        Msg.format(""),
                        Msg.translated("customnpcs.favicons.edit"),
                        Msg.translated("customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new SendServerCustomizer(this);
    }

    @Override
    public Action clone() {
        return new SendServer(server, getDelay(), getMode(), getConditions());
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
            return MenuTitles.createModern(Msg.translated("customnpcs.menus.action_customizer.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            return MenuUtils.actionBase(action)
                    .setButton(22, Button.clickable(ItemBuilder.modern(OAK_HANGING_SIGN)
                                    .setDisplay(Component.text(getServer()))
                                    .setLore(Msg.translate("customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                Player p = (Player) event.getWhoClicked();
                                p.closeInventory();
                                plugin.serverWaiting.add(p.getUniqueId());
                                new ServerRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            })))
                    .build();
        }
    }
}