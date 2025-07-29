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
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.IRON_INGOT;
import static org.bukkit.Material.PAPER;

@Getter
@Setter
public class ActionBar extends Action {

    private String rawMessage;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param rawMessage The raw message
     * @deprecated Use {@link ActionBar#ActionBar(String, int, Condition.SelectionMode, List, int)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public ActionBar(String rawMessage, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals, 0);
        this.rawMessage = rawMessage;
    }

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param rawMessage The raw message
     */
    public ActionBar(String rawMessage, int delay, Condition.SelectionMode mode, List<Condition> conditionals, int cooldown) {
        super(delay, mode, conditionals, cooldown);
        this.rawMessage = rawMessage;
    }

    public static Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(IRON_INGOT)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.actionbar"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.favicons.actionbar.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    ActionBar actionImpl = new ActionBar("", 0, Condition.SelectionMode.ONE, new ArrayList<>(), 0);
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.getAPI().openMenu(p, actionImpl.getMenu());
                }));
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(ActionBar.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + ActionBar.class.getName());
        }
        String rawMessage = parseString(serialized, "raw");
        ParseResult pr = parseBase(serialized);

        ActionBar message = new ActionBar(rawMessage, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown());

        return clazz.cast(message);
    }

    @Override
    public Menu getMenu() {
        return new ActionbarCustomizer(this);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(IRON_INGOT)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.actionbar"))
                .setLore(Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.translate(player.locale(), "customnpcs.favicons.preview", Msg.format(getRawMessage())),
                        Msg.format(getRawMessage().isEmpty() ? "<dark_gray><i>" + Msg.translatedString(player.locale(), "customnpcs.messages.empty_string") : getRawMessage()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
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
        player.sendActionBar(Msg.format(CustomNPCs.getInstance().papi ? PlaceholderAPI.setPlaceholders(player, rawMessage) : rawMessage));
        activateCooldown(player.getUniqueId());
    }

    @Override
    public String serialize() {
        return generateSerializedString("ActionBar", Map.of("raw", rawMessage));
    }

    @Override
    public Action clone() {
        return new ActionBar(rawMessage, getDelay(), getMode(), new ArrayList<>(getConditions()), getCooldown());
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
            return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            return MenuUtils.actionBase(actionBar, player)
                    .setButton(22, Button.clickable(ItemBuilder.modern(PAPER)
                                    .setDisplay(Msg.translate(player.locale(), getRawMessage().isEmpty() ? "<dark_gray><i>" + Msg.translatedString(player.locale(), "customnpcs.messages.empty_string") : getRawMessage()))
                                    .setLore(Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                player.closeInventory();
                                plugin.wait(player, WaitingType.ACTIONBAR);
                                new ActionbarRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                            })))
                    .build();
        }
    }
}
