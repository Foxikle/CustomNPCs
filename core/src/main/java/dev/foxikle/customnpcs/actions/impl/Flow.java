/*
 * Copyright (c) 2026. Foxikle
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
import dev.foxikle.customnpcs.api.actions.ActionCustomizer;
import dev.foxikle.customnpcs.api.callbacks.ActionCreationCallback;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.DeleteActionMenu;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.menu.NewActionMenu;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.data.Key;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class Flow extends Action {

    private final Supplier<Flow> defaultValue = () -> new Flow(Utils.list(), 0, Selector.ONE, Utils.list(), 0, UUID.randomUUID());
    private final ActionCustomizer menu = new FlowCustomizer();

    // the first integer should always be 0
    private final List<Action> actions;

    public static final StructCodec<Flow> CODEC = StructCodec.struct(
            "actions", Action.CODEC.list(), Flow::getActions,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            Flow::new
    );

    public Flow(List<Action> actions, int delay, Selector selector, List<Condition> conditions, int cooldown, UUID uuid) {
        super(delay, selector, conditions, cooldown, uuid);
        this.actions = actions;
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        AtomicInteger offset = new AtomicInteger(1);
        actions.forEach(action -> {
            player.getScheduler().runDelayed(CustomNPCs.getInstance(),
                    _ -> action.perform(npc, player),
                    () -> {
                    }, offset.get());
            offset.addAndGet(action.getDelay());
        });
    }

    @Override
    public ItemStack getFavicon(Player player) {

        List<Component> lore = new ArrayList<>(actions.size() + 6);
        lore.add(Msg.get(player, "favicons.delay", Arg.arg(getDelay())));
        lore.add(Msg.get(player, "favicons.actions"));
        lore.add(Component.empty());

        actions.forEach(action -> {
            Component title = action.getFavicon(player).displayName();
            lore.add(title.append(Msg.format(" <gray><i>" + action.displayId())));
        });

        lore.add(Component.empty());
        lore.add(Msg.get(player, "favicons.edit"));
        lore.add(Msg.get(player, "favicons.remove"));

        return ItemBuilder.of(Material.GLOW_ITEM_FRAME)
                .displayName(Msg.get(player, "favicons.flow"))
                .lore(lore)
                .build();
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "flow";
    }

    @Override
    public Button creationButton(Player player) {
        return Button.clickable(
                ItemBuilder.of(Material.GLOW_ITEM_FRAME)
                        .displayName(Msg.get(player, "favicons.flow"))
                        .lore(
                                Msg.get(player, "favicons.delay", Arg.arg(getDelay())),
                                Component.empty(),
                                Msg.get(player, "favicons.flow.description")
                        )
                        .build(),
                new ActionCreationCallback(this)
        );
    }

    @Override
    public Action clone() {
        return new Flow(new ArrayList<>(actions), getDelay(), getSelector(),
                getConditions(), getCooldown(), getUuid());
    }

    @NotNullByDefault
    @AllArgsConstructor
    public static class FlowCustomizer implements ActionCustomizer {

        @Override
        public String name() {
            return "ACTIONBAR_CUSTOMIZER";
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
            Flow flow = getAction(view);
            return MenuUtils.actionBase(flow, view)
                    .apply(content -> getButtons(view.viewer(), flow).forEach(content::add))
                    .build();
        }

        @SuppressWarnings("unchecked")
        private List<Button> getButtons(Player player, Flow flow) {
            List<Button> buttons = new ArrayList<>(flow.actions.size());
            flow.getActions().forEach(action -> {
                ItemStack i = action.getFavicon(player);
                List<Component> lore = i.lore();
                if (lore == null) return;
                lore.removeLast();
                lore.removeLast();
                lore.add(Msg.get(player, "menus.ordered.edit"));
                lore.add(Msg.get(player, "menus.ordered.delete"));
                lore.add(Msg.get(player, "menus.flow.move_up"));
                lore.add(Msg.get(player, "menus.flow.move_down"));

                buttons.add(Button.clickable(i, (view, e) -> {
                    switch (e.getClick()) {
                        case CONTROL_DROP -> {
                            flow.getActions().remove(action);
                            Utils.sound(view, Sound.ITEM_TRIDENT_HIT);
                            view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                        }
                        case DROP -> view.lotus().openMenu(view.viewer(), new DeleteActionMenu(), DataRegistry.empty()
                                .put(Key.of("onDelete", BiConsumer.class), (o, o2) -> {
                                    flow.getActions().remove(action);
                                    Utils.sound(view, Sound.ITEM_TRIDENT_HIT);
                                    view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                                })
                                .put(Key.of("onCancel", BiConsumer.class), (o, o2) -> {
                                    Utils.sound(view, Sound.UI_BUTTON_CLICK);
                                    view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                                })
                                .put(Key.of("action", Action.class), action));
                        case LEFT -> {
                            int index = flow.getActions().indexOf(action);
                            if (index == 0) {
                                Utils.sound(view, Sound.ENTITY_VILLAGER_NO);
                                view.viewer().sendMessage(Msg.get(view.viewer(), "menus.flow.move_up_fail"));
                                return;
                            }

                            Collections.swap(flow.getActions(), index, index - 1);
                            Utils.sound(view, Sound.BLOCK_PISTON_EXTEND);
                            view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                        }
                        case RIGHT -> {
                            int index = flow.getActions().indexOf(action);
                            if (index == flow.getActions().size() - 1) {
                                Utils.sound(view, Sound.ENTITY_VILLAGER_NO);
                                view.viewer().sendMessage(Msg.get(view.viewer(), "menus.flow.move_down_fail"));
                                return;
                            }

                            Collections.swap(flow.getActions(), index, index + 1);
                            Utils.sound(view, Sound.BLOCK_PISTON_CONTRACT);
                            view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                        }
                    }
                }));
            });

            Button add = Button.clickable(ItemBuilder.of(Material.LILY_PAD)
                    .displayName(Msg.get(player, "menus.actions.new"))
                    .build(), (view, event) -> {
                if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                    // todo: Add action clone menu
                    return;
                }

                view.lotus().openMenu(view.viewer(), new NewActionMenu(), DataRegistry.empty()
                        .put(Key.of("onDelete", BiConsumer.class), (o, o2) -> {
                            flow.getActions().remove(flow);
                            Utils.sound(view, Sound.ITEM_TRIDENT_HIT);
                            view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                        })
                        .put(Key.of("onCancel", BiConsumer.class), (o, o2) -> {
                            Utils.sound(view, Sound.UI_BUTTON_CLICK);
                            view.lotus().openMenu(view.viewer(), new FlowCustomizer(), view.data());
                        })
                        .put(Key.of("action", Action.class), flow));

            });
            buttons.add(add);

            return buttons;
        }
    }

}
