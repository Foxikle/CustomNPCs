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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.*;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class RunCommand extends Action {

    public static final StructCodec<RunCommand> CODEC = StructCodec.struct(
            "raw", Codec.STRING, RunCommand::getCommand,
            "asConsole", Codec.BOOLEAN, RunCommand::isAsConsole,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            RunCommand::new
    );
    private String command;
    private boolean asConsole;

    public RunCommand(String rawCommand, boolean asConsole, int delay, Selector mode,
                      List<Condition> conditionals, int cooldown, @Nullable UUID uuid) {
        super(delay, mode, conditionals, cooldown, uuid);
        this.command = rawCommand;
        this.asConsole = asConsole;
    }

    @Deprecated(forRemoval = true)
    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(RunCommand.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + RunCommand.class.getName());
        }
        String raw = parseString(serialized, "raw");
        boolean asConsole = parseBoolean(serialized, "asConsole");
        ParseResult pr = parseBase(serialized);

        RunCommand command = new RunCommand(raw, asConsole, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown(),
                UUID.randomUUID());
        return clazz.cast(command);
    }

    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.of(ANVIL)
                        .displayName(Msg.get(player, "favicons.command"))
                        .lore(Msg.lore(player.locale(), "favicons.command.description"))
                        .build(),
                (menuView, event) -> {
                    Player p = (Player) event.getWhoClicked();
                    event.setCancelled(true);
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    RunCommand actionImpl = new RunCommand("say hi", false, 0, Selector.ONE, new ArrayList<>(), 0,
                            UUID.randomUUID());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.lotus().openMenu(p, actionImpl.getMenu());
                });
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(ANVIL).displayName(Msg.get(player, "favicons.command"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())), Msg.format(""),
                        Msg.get(player, "favicons.command.syntax", Arg.arg(command)),
                        Msg.get(player, "favicons.command.as_console", Arg.arg(asConsole)),
                        Msg.format(""),
                        Msg.get(player, "favicons.edit"),
                        Msg.get(player, "favicons.remove")
                ).build();
    }

    @Override
    public Menu<Component> getMenu() {
        return new RunCommandCustomizer(this);
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        if (!processConditions(player)) return;
        String command = Msg.papi(player, this.command);
        Bukkit.dispatchCommand(asConsole ? Bukkit.getConsoleSender() : player, command);
        activateCooldown(player.getUniqueId());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "RunCommand";
    }

    @Override
    public Action clone() {
        return new RunCommand(command, asConsole, getDelay(), getSelector(), new ArrayList<>(getConditions()),
                getCooldown(), getUuid());
    }

    @AllArgsConstructor
    @NotNullByDefault
    public class RunCommandCustomizer implements Menu<Component> {

        private final RunCommand action;

        @Override
        public String name() {
            return "RUN_COMMAND_CUSTOMIZER";
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
            return MenuUtils.actionBase(action, player)
                    .set(Slot.of(4), papiTip(player))
                    .set(player.hasPermission("customnpcs.run_command.enable_console") ? Slot.of(21) :
                            Slot.of(22), setCommand(player))
                    .set(Slot.of(23), toggle(player))
                    .build();
        }

        private Button toggle(Player player) {
            if (!player.hasPermission("customnpcs.run_command.enable_console")) return MenuItems.MENU_GLASS;
            List<Component> lore = new ArrayList<>();
            if (isAsConsole()) {
                lore.addAll(Utils.list(Msg.lore(player.locale(), "menus.action.command.as_console.warning")));
            }
            lore.add(Msg.get(player, "items.click_to_change"));
            return Button.clickable(ItemBuilder.of(isAsConsole() ? RED_CANDLE : GREEN_CANDLE)
                            .lore(lore.toArray(new Component[]{}))
                            .displayName(isAsConsole() ? Msg.get(player, "menus.action.command.as_console.true") :
                                    Msg.get(player, "menus.action.command.as_console.false"))
                            .build(),
                    (menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        Player p = (Player) event.getWhoClicked();
                        if (!p.hasPermission("customnpcs.run_command.enable_console")) {
                            p.sendMessage(Msg.get(player, "commands.no_permission"));
                            return;
                        }

                        setAsConsole(!isAsConsole());
                        menuView.content().set(Slot.of(23), toggle(p));
                    });
        }

        private Button setCommand(Player player) {
            return Button.clickable(ItemBuilder.of(ANVIL)
                            .displayName(Component.text("/" + getCommand()))
                            .lore(Msg.get(player, "items.click_to_change"))
                            .build(),
                    (_, event) -> {
                        CustomNPCs plugin = CustomNPCs.getInstance();
                        Player p = (Player) event.getWhoClicked();
                        p.closeInventory();
                        plugin.wait(p, WaitingType.COMMAND);
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    });
        }

        private Button papiTip(Player player) {
            Component[] lore;
            if (!CustomNPCs.getInstance().papi) {
                lore = Msg.lore(player.locale(), "menus.action.command.papi_tip.no_papi");
            } else if (!CustomNPCs.getInstance().papiPlayerExpansion) {
                lore = Msg.lore(player.locale(), "menus.action.command.papi_tip.no_expansion");
            } else {
                lore = Msg.lore(player.locale(), "menus.action.command.papi_tip.all_good");
            }
            return Button.clickable(ItemBuilder.of(REDSTONE_TORCH)
                    .displayName(Msg.get(player, "menus.action.command.papi_tip.title"))
                    .lore(lore)
                    .build(), (_, inventoryClickEvent) -> {
                inventoryClickEvent.setCancelled(true);
                if (!CustomNPCs.getInstance().papi) {
                    player.sendMessage(Msg.get(player, "menus.action.command.papi_tip.download.plugin"));
                    return;
                }
                if (!CustomNPCs.getInstance().papiPlayerExpansion) {
                    player.sendMessage(Msg.get(player, "menus.action.command.papi_tip.download.expansion"));
                }
            });
        }
    }
}
