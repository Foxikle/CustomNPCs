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

package dev.foxikle.customnpcs.actions.impl;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuItems;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.*;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class DisplayTitle extends Action {

    public static final StructCodec<DisplayTitle> CODEC = StructCodec.struct(
            "title", Codec.STRING, DisplayTitle::getTitle,
            "subtitle", Codec.STRING, DisplayTitle::getSubTitle,
            "fade_in", Codec.INT, DisplayTitle::getFadeIn,
            "stay", Codec.INT, DisplayTitle::getStay,
            "fade_out", Codec.INT, DisplayTitle::getFadeOut,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            DisplayTitle::new
    );

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
    public DisplayTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut, int delay, Selector mode,
                        List<Condition> conditions, int cooldown, @Nullable UUID uuid) {
        super(delay, mode, conditions, cooldown, uuid);
        this.title = title;
        this.subTitle = subTitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }


    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.of(OAK_SIGN)
                        .displayName(Msg.get(player, "favicons.title"))
                        .lore(Msg.lore(player.locale(), "favicons.title.description"))
                        .build(),
                (menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 1, 1);
                    DisplayTitle action = new DisplayTitle("Title", "Subtitle", 10, 10, 10, 0, Selector.ONE,
                            new ArrayList<>(), 0, UUID.randomUUID());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), action);
                    menuView.lotus().openMenu(p, action.getMenu());
                });
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(OAK_SIGN)
                .displayName(Msg.get(player, "favicons.title"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.get(player, "favicons.preview"),
                        Msg.format("<white><!i>" + getTitle()),
                        Msg.format("<white><!i>" + getSubTitle()),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.get(player, "menus.action.title.display.fade_in", Arg.arg(fadeIn)),
                        Msg.get(player, "menus.action.title.display.stay", Arg.arg(stay)),
                        Msg.get(player, "menus.action.title.display.fade_out", Arg.arg(fadeOut)),
                        Msg.format(""),
                        Msg.get(player, "favicons.edit"),
                        Msg.get(player, "favicons.remove")
                ).build();
    }

    @Override
    public Menu<Component> getMenu() {
        return new DisplayTitleCustomizer(this);
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        if (!processConditions(player)) return;

        Component titleComponent = Msg.format(Msg.papi(player, title));
        Component subtitleComponent = Msg.format(Msg.papi(player, subTitle));

        player.showTitle(Title.title(titleComponent, subtitleComponent, Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))));
        activateCooldown(player.getUniqueId());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "DisplayTitle";
    }

    public Action clone() {
        return new DisplayTitle(title, subTitle, fadeIn, stay, fadeOut, getDelay(), getSelector(),
                new ArrayList<>(getConditions()), getCooldown(), getUuid());
    }

    @Deprecated(forRemoval = true)
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

        DisplayTitle message = new DisplayTitle(title, subTitle, in, stay, out, pr.delay(), pr.mode(),
                pr.conditions(), pr.cooldown(), UUID.randomUUID());

        return clazz.cast(message);
    }

    @NotNullByDefault
    public static class DisplayTitleCustomizer implements Menu<Component> {

        private final DisplayTitle action;

        public DisplayTitleCustomizer(DisplayTitle action) {
            this.action = action;
        }

        @Override
        public String name() {
            return "DISPLAY_TITLE_CUSTOMIZER";
        }

        @Override
        public Component title(MenuView<Component, ?> view) {
            return Msg.get(view.viewer(), "menus.action_customizer.title");
        }

        @Override
        public Capacity capacity(MenuView<Component, ?> view) {
            return Capacity.ofRows(5);
        }

        @Override
        public Content content(MenuView<Component, ?> view) {
            Player player = view.viewer();
            Component[] incLore = Msg.lore(player.locale(), "menus.action_customizer.delay.increment.description");
            Component[] decLore = Msg.lore(player.locale(), "menus.action_customizer.delay.decrement.description");
            Component displayLore = Msg.get(player, "menus.action.title.display.lore");

            return MenuUtils.actionBase(action, player)
                    .set(Slot.of(10), Button.clickable(ItemBuilder.of(LIME_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.fade_in.increase"))
                                    .lore(incLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                action.setFadeIn(Utils.increment(event).apply(action.fadeIn));
                                menuView.content().set(Slot.of(19), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_in", Arg.arg(action.getFadeIn())), displayLore));
                            }))

                    .set(Slot.of(12), Button.clickable(ItemBuilder.of(LIME_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.stay.increase"))
                                    .lore(incLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                action.setStay(Utils.increment(event).apply(action.stay));
                                menuView.content().set(Slot.of(21), MenuItems.display(Msg.get(player, "menus.action.title.display.stay", Arg.arg(action.getStay())), displayLore));
                            }))

                    .set(Slot.of(14), Button.clickable(ItemBuilder.of(LIME_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.fade_out.increase"))
                                    .lore(incLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                action.setFadeOut(Utils.increment(event).apply(action.fadeOut));
                                menuView.content().set(Slot.of(23), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_out", Arg.arg(action.fadeOut)), displayLore));
                            })
                    )
                    .set(Slot.of(19), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_in", Arg.arg(action.fadeIn)), displayLore))
                    .set(Slot.of(21), MenuItems.display(Msg.get(player, "menus.action.title.display.stay", Arg.arg(action.stay)), displayLore))
                    .set(Slot.of(23), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_out", Arg.arg(action.fadeOut)), displayLore))
                    .set(Slot.of(28), Button.clickable(ItemBuilder.of(RED_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.fade_in.decrease"))
                                    .lore(decLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeIn == 1) {
                                    player.sendMessage(Msg.get(player, "menus.action.title.duration_less_than_1"));
                                    return;
                                }
                                action.setFadeIn(Math.max(1, Utils.decrement(event).apply(action.fadeIn)));
                                menuView.content().set(Slot.of(19), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_in", Arg.arg(action.getFadeIn())), displayLore));
                            }))
                    .set(Slot.of(30), Button.clickable(ItemBuilder.of(RED_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.stay.decrease"))
                                    .lore(decLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeIn == 1) {
                                    player.sendMessage(Msg.get(player, "menus.action.title.duration_less_than_1"));
                                    return;
                                }
                                action.setStay(Math.max(1, Utils.decrement(event).apply(action.stay)));
                                menuView.content().set(Slot.of(19), MenuItems.display(Msg.get(player, "menus.action.title.display.stay", Arg.arg(action.getStay())), displayLore));
                            }))
                    .set(Slot.of(32), Button.clickable(ItemBuilder.of(RED_DYE)
                                    .displayName(Msg.get(player, "menus.action.title.fade_out.decrease"))
                                    .lore(decLore).build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.fadeOut == 1) {
                                    player.sendMessage(Msg.get(player, "menus.action.title.duration_less_than_1"));
                                    return;
                                }
                                action.setFadeOut(Math.max(1, Utils.decrement(event).apply(action.fadeOut)));
                                menuView.content().set(Slot.of(19), MenuItems.display(Msg.get(player, "menus.action.title.display.fade_out", Arg.arg(action.getFadeOut())), displayLore));
                            }))
                    .set(Slot.of(16), Button.clickable(ItemBuilder.of(OAK_HANGING_SIGN)
                            .displayName(Msg.get(player, "menus.action.title.current.title"))
                            .lore(Msg.format("<white><!i>%s", action.getTitle()), Component.empty(),
                                    Msg.get(player, "items.click_to_change"))
                            .build(), (_, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        Player p = (Player) event.getWhoClicked();
                        CustomNPCs plugin = CustomNPCs.getInstance();
                        p.closeInventory();
                        plugin.wait(p, WaitingType.TITLE);
                    }))
                    .set(Slot.of(34), Button.clickable(ItemBuilder.of(DARK_OAK_HANGING_SIGN)
                                    .displayName(Msg.get(player, "menus.action.title.current.subtitle"))
                                    .lore(Msg.format("<white><!i>%s", action.getSubTitle()), Component.empty(),
                                            Msg.get(player, "items.click_to_change"))
                                    .build(),
                            (menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                Player p = (Player) event.getWhoClicked();
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.SUBTITLE);
                            })
                    ).build();
        }
    }
}
