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
import dev.foxikle.customnpcs.internal.runnables.SubtitleRunnable;
import dev.foxikle.customnpcs.internal.runnables.TitleRunnable;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.*;

@Getter
@Setter
public class DisplayTitle extends Action {

    private String title;
    private String subTitle;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param title The raw message
     */
    public DisplayTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, int delay, Condition.SelectionMode mode, List<Condition> conditionals, int cooldown) {
        super(delay, mode, conditionals, cooldown);
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param title The raw message
     * @deprecated Use {@link DisplayTitle#DisplayTitle(String, String, int, int, int, int, Condition.SelectionMode, List, int)}}
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    @Deprecated
    public DisplayTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals, 0);
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public static Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(OAK_SIGN)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.title"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.favicons.title.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1, 1);
                    DisplayTitle actionImpl = new DisplayTitle("Title", "Subtitle", 10, 10, 10, 0, Condition.SelectionMode.ONE, new ArrayList<>(), 0);
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.getAPI().openMenu(p, actionImpl.getMenu());
                }));
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(DisplayTitle.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + DisplayTitle.class.getName());
        }

        String title = parseString(serialized, "title");
        String subTitle = parseString(serialized, "subTitle");
        int in = parseInt(serialized, "in");
        int stay = parseInt(serialized, "stay");
        int out = parseInt(serialized, "out");

        ParseResult pr = parseBase(serialized);

        DisplayTitle message = new DisplayTitle(title, subTitle, in, stay, out, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown());

        return clazz.cast(message);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(OAK_SIGN).setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.title"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.translate(player.locale(), "customnpcs.favicons.preview"),
                        Msg.format("<white><!i>" + getTitle()),
                        Msg.format("<white><!i>" + getSubTitle()),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_in", fadeIn),
                        Msg.translate(player.locale(), "customnpcs.menus.action.title.display.stay", stay),
                        Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_out", fadeOut),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new DisplayTitleCustomizer(this);
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

        Component titleComponent = CustomNPCs.getInstance().miniMessage.deserialize(CustomNPCs.getInstance().papi ? PlaceholderAPI.setPlaceholders(player, title) : title);
        Component subtitleComponent = CustomNPCs.getInstance().miniMessage.deserialize(CustomNPCs.getInstance().papi ? PlaceholderAPI.setPlaceholders(player, subTitle) : subTitle);

        player.showTitle(Title.title(titleComponent, subtitleComponent, Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))));
        activateCooldown(player.getUniqueId());
    }

    @Override
    public String serialize() {

        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("subTitle", subTitle);
        params.put("in", fadeIn);
        params.put("stay", stay);
        params.put("out", fadeOut);

        return generateSerializedString("DisplayTitle", params);
    }

    public Action clone() {
        return new DisplayTitle(title, subTitle, fadeIn, stay, fadeOut, getDelay(), getMode(), new ArrayList<>(getConditions()), getCooldown());
    }

    public static class DisplayTitleCustomizer implements Menu {

        private final DisplayTitle action;

        public DisplayTitleCustomizer(DisplayTitle action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "DISPLAY_TITLE_CUSTOMIZER";
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

            Component[] incLore = Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.increment.description");
            Component[] decLore = Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.decrement.description");
            Component displayLore = Msg.translate(player.locale(), "customnpcs.menus.action.title.display.lore");

            return MenuUtils.actionBase(action, player)
                    .setButton(10, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.fade_in.increase"))
                                    .setLore(incLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setFadeIn(action.getFadeIn() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setFadeIn(action.getFadeIn() + 1);
                                } else if (event.isRightClick()) {
                                    action.setFadeIn(action.getFadeIn() + 5);
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_in", action.getFadeIn(), displayLore))));
                            }))
                    ).setButton(12, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.stay.increase"))
                                    .setLore(incLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setStay(action.getStay() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setStay(action.getStay() + 1);
                                } else if (event.isRightClick()) {
                                    action.setStay(action.getStay() + 5);
                                }
                                menuView.updateButton(21, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.stay", action.getStay(), displayLore))));
                            }))
                    ).setButton(14, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.fade_out.increase"))
                                    .setLore(incLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setFadeOut(action.getFadeOut() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setFadeOut(action.getFadeOut() + 1);
                                } else if (event.isRightClick()) {
                                    action.setFadeOut(action.getFadeOut() + 5);
                                }
                                menuView.updateButton(23, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_out", action.getFadeOut(), displayLore))));
                            }))
                    ).setButton(19, Button.empty(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_in", action.fadeIn, displayLore)))
                    ).setButton(21, Button.empty(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.stay", action.stay, displayLore)))
                    ).setButton(23, Button.empty(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_out", action.fadeOut, displayLore)))
                    ).setButton(28, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.fade_in.decrease"))
                                    .setLore(decLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeIn == 1) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.title.duration_less_than_1"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setFadeIn(Math.max((action.fadeIn - 20), 1));
                                } else if (event.isLeftClick()) {
                                    action.setFadeIn(Math.max((action.fadeIn - 1), 1));
                                } else if (event.isRightClick()) {
                                    action.setFadeIn(Math.max((action.fadeIn - 5), 1));
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_in", action.getFadeIn(), displayLore))));
                            }))
                    ).setButton(30, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.stay.decrease"))
                                    .setLore(decLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeIn == 1) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.title.duration_less_than_1"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setStay(Math.max((action.stay - 20), 1));
                                } else if (event.isLeftClick()) {
                                    action.setStay(Math.max((action.stay - 1), 1));
                                } else if (event.isRightClick()) {
                                    action.setStay(Math.max((action.stay - 5), 1));
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.stay", action.getStay(), displayLore))));
                            }))
                    ).setButton(32, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.fade_out.decrease"))
                                    .setLore(decLore).build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeOut == 1) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.title.duration_less_than_1"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setFadeOut(Math.max((action.fadeOut - 20), 1));
                                } else if (event.isLeftClick()) {
                                    action.setFadeOut(Math.max((action.fadeOut - 1), 1));
                                } else if (event.isRightClick()) {
                                    action.setFadeOut(Math.max((action.fadeOut - 5), 1));
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.display.fade_out", action.getFadeOut(), displayLore))));
                            }))
                    ).setButton(16, Button.clickable(ItemBuilder.modern(OAK_HANGING_SIGN)
                            .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.current.title"))
                            .setLore(Msg.format("<white><!i>" + action.getTitle()), Component.empty(), Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                                    .build(), ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                Player p = (Player) event.getWhoClicked();
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                p.closeInventory();
                        plugin.wait(p, WaitingType.TITLE);
                                new TitleRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                            }))
                    ).setButton(34, Button.clickable(ItemBuilder.modern(DARK_OAK_HANGING_SIGN)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.title.current.subtitle"))
                                    .setLore(Msg.format("<white><!i>" + action.getSubTitle()), Component.empty(), Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                Player p = (Player) event.getWhoClicked();
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.SUBTITLE);
                                new SubtitleRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                            }))
                    ).build();
        }
    }
}
