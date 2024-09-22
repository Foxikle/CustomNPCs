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

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.runnables.ActionbarRunnable;
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
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.IRON_INGOT;
import static org.bukkit.Material.PAPER;

@Getter
@Setter
public class ActionBar extends Action {

    public static final Button CREATION_BUTTON = Button.clickable(ItemBuilder.modern(IRON_INGOT)
                    .setDisplay(Msg.translate("customnpcs.favicons.actionbar"))
                    .setLore(Msg.lore("customnpcs.favicons.actionbar.description"))
                    .build(),
            ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                ActionBar actionImpl = new ActionBar("", 0, Condition.SelectionMode.ONE, new ArrayList<>());
                CustomNPCs.getInstance().editingActions.put(player.getUniqueId(), actionImpl);
                menuView.getAPI().openMenu(player, actionImpl.getMenu());
            }));

    private String rawMessage;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param rawMessage The raw message
     */
    public ActionBar(String rawMessage, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.rawMessage = rawMessage;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(ActionBar.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + ActionBar.class.getName());
        }
        String rawMessage = serialized.replaceAll(".*raw=`(.*?)`.*", "$1");
        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        ActionBar message = new ActionBar(rawMessage, delay, mode, conditions);

        return clazz.cast(message);
    }

    @Override
    public Menu getMenu() {
        return new ActionbarCustomizer(this);
    }

    @Override
    public ItemStack getFavicon() {
        return ItemBuilder.modern(IRON_INGOT)
                .setDisplay(Msg.translate("customnpcs.favicons.actionbar"))
                .setLore(Msg.translate("customnpcs.favicons.delay", getDelay()),
                        Msg.translated("customnpcs.favicons.preview", Msg.format(getRawMessage())),
                        Msg.format(getRawMessage().isEmpty() ? "<dark_gray><i>" + Msg.translatedString("customnpcs.messages.empty_string") : getRawMessage()),
                        Msg.format(""),
                        Msg.translated("customnpcs.favicons.edit"),
                        Msg.translated("customnpcs.favicons.remove")
                ).build();
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
        player.sendActionBar(CustomNPCs.getInstance().miniMessage.deserialize(CustomNPCs.getInstance().papi ? PlaceholderAPI.setPlaceholders(player, rawMessage) : rawMessage));
    }

    @Override
    public String serialize() {
        return "ActionBar{raw=`" + rawMessage + "`, delay=" + getDelay() + ", mode=" + getMode().name() +
                ", conditions=" + getConditionSerialized() + "}";
    }

    @Override
    public Action clone() {
        return new ActionBar(rawMessage, getDelay(), getMode(), new ArrayList<>(getConditions()));
    }

    public class ActionbarCustomizer implements Menu {

        private final ActionBar actionBar;

        public ActionbarCustomizer(ActionBar actionBar) {
            this.actionBar = actionBar;
        }

        @Override
        public String getName() {
            return "ACTIONBAR_CUSTOMIZER";
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
            return MenuUtils.actionBase(actionBar)
                    .setButton(22, Button.clickable(ItemBuilder.modern(PAPER)
                                    .setDisplay(Msg.translated(getRawMessage().isEmpty() ? "<dark_gray><i>" + Msg.translatedString("customnpcs.messages.empty_string") : getRawMessage()))
                                    .setLore(Msg.translate("customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                player.closeInventory();
                                plugin.actionbarWaiting.add(player.getUniqueId());
                                new ActionbarRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                            })))
                    .build();
        }
    }
}
