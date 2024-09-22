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
import dev.foxikle.customnpcs.internal.menu.MenuItems;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
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

import static org.bukkit.Material.*;

@Getter
@Setter
public class RemoveXP extends Action {

    public static final Button CREATION_BUTTON = Button.clickable(ItemBuilder.modern(GLASS_BOTTLE)
                    .setDisplay(Msg.translate("customnpcs.favicons.remove_xp"))
                    .setLore(Msg.lore("customnpcs.favicons.remove_xp.description"))
                    .build(),
            ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);

                RemoveXP actionImpl = new RemoveXP(1, true, 0, Condition.SelectionMode.ONE, new ArrayList<>());
                CustomNPCs.getInstance().editingActions.put(player.getUniqueId(), actionImpl);
                menuView.getAPI().openMenu(player, actionImpl.getMenu());
            }));

    private int amount;
    private boolean levels;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param levels if the xp is in levels
     * @param amount the number of XP to remove
     */
    public RemoveXP(int amount, boolean levels, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.levels = levels;
        this.amount = amount;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(RemoveXP.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + RemoveXP.class.getName());
        }
        int amount = Integer.parseInt(serialized.replaceAll(".*amount=(\\d+).*", "$1"));
        boolean levels = Boolean.parseBoolean(serialized.replaceAll(".*levels=(true|false).*", "$1"));

        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        RemoveXP xp = new RemoveXP(amount, levels, delay, mode, conditions);

        return clazz.cast(xp);
    }

    @Override
    public ItemStack getFavicon() {
        return ItemBuilder.modern(GLASS_BOTTLE).setDisplay(Msg.translate("customnpcs.favicons.remove_xp"))
                .setLore(
                        Msg.translate("customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate("customnpcs.menus.action.remove_xp.xp", amount),
                        Msg.translate("customnpcs.menus.action.remove_xp.awarding", (levels ? Msg.translatedString("customnpcs.menus.action.xp.levels") : Msg.translatedString("customnpcs.menus.action.xp.points"))),
                        Msg.format(""),
                        Msg.translated("customnpcs.favicons.edit"),
                        Msg.translated("customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new RemoveXPCustomizer(this);
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

        if (levels) {
            if (amount >= player.getLevel()) {
                player.setLevel(0);
            } else {
                player.setLevel(player.getLevel() - amount);
            }
        } else {
            Utils.setTotalExperience(player, Utils.getTotalExperience(player) - amount);
        }
    }

    @Override
    public String serialize() {
        return "RemoveXP{amount=" + amount + ", levels=" + levels + ", delay=" + getDelay()
                + ", mode=" + getMode().name() + ", conditions=" + getConditionSerialized() + "}";
    }

    @Override
    public Action clone() {
        return new RemoveXP(getAmount(), isLevels(), getDelay(), getMode(), new ArrayList<>(getConditions()));
    }

    public class RemoveXPCustomizer implements Menu {
        private final RemoveXP action;

        public RemoveXPCustomizer(RemoveXP action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "REMOVE_XP_CUSTOMIZER";
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

            Component[] incLore = Msg.lore("customnpcs.menus.action_customizer.delay.increment.description");
            Component[] decLore = Msg.lore("customnpcs.menus.action_customizer.delay.decrement.description");

            return MenuUtils.actionBase(action)

                    // increment
                    .setButton(11, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_xp.increase"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    setAmount(getAmount() + 20);
                                } else if (event.isLeftClick()) {
                                    setAmount(getAmount() + 1);
                                } else if (event.isRightClick()) {
                                    setAmount(getAmount() + 5);
                                }
                                menuView.updateButton(20, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.remove_xp.xp", getAmount()))));
                            }))
                    ).setButton(20, MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.remove_xp.xp", getAmount()))
                    ).setButton(29, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_xp.decrease"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (getAmount() == 1) {
                                    event.getWhoClicked().sendMessage(Msg.translate("customnpcs.menus.action.give_xp.xp_less_one"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    setAmount(Math.max(1, getAmount() - 20));
                                } else if (event.isLeftClick()) {
                                    setAmount(Math.max(1, getAmount() - 1));
                                } else if (event.isRightClick()) {
                                    setAmount(Math.max(1, getAmount() - 5));
                                }
                                menuView.updateButton(20, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.remove_xp.xp", getAmount()))));
                            }))
                    ).setButton(24, toggle())

                    .build();
        }

        private Button toggle() {
            return Button.clickable(ItemBuilder.modern(isLevels() ? GREEN_CANDLE : RED_CANDLE)
                            .setDisplay(Msg.translate("customnpcs.menus.action.remove_xp.awarding", (isLevels() ? Msg.translated("customnpcs.menus.action.xp.levels") : Msg.translated("customnpcs.menus.action.xp.points"))))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        setLevels(!isLevels());
                        menuView.updateButton(24, button -> button.setItem(toggle().getItem()));
                    }));
        }
    }
}