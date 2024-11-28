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
import dev.foxikle.customnpcs.internal.runnables.CommandRunnable;
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
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Material.*;

@Getter
@Setter
public class RunCommand extends Action {

    public static Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(ANVIL)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.command"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.favicons.command.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    Player p = (Player) event.getWhoClicked();
                    event.setCancelled(true);
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    RunCommand actionImpl = new RunCommand("say hi", false, 0, Condition.SelectionMode.ONE, new ArrayList<>());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.getAPI().openMenu(p, actionImpl.getMenu());
                }));
    }

    private String command;
    private boolean asConsole;


    /**
     * Creates a new RunCommand with the specified command
     *
     * @param rawCommand   The raw command
     * @param asConsole    If the command should be executed as a console command.
     * @param delay        The delay
     * @param mode         The mode
     * @param conditionals The conditionals
     */
    public RunCommand(String rawCommand, boolean asConsole, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.command = rawCommand;
        this.asConsole = asConsole;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(RunCommand.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + RunCommand.class.getName());
        }
        String raw = serialized.replaceAll(".*raw=`(.*?)`.*", "$1");
        boolean asConsole = Boolean.parseBoolean(serialized.replaceAll(".*asConsole=(true|false).*", "$1"));
        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        RunCommand command = new RunCommand(raw, asConsole, delay, mode, conditions);
        return clazz.cast(command);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(ANVIL).setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.command"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.command.syntax", command),
                        Msg.translate(player.locale(), "customnpcs.favicons.command.as_console", asConsole),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new RunCommandCustomizer(this);
    }

    /**
     * Runs the command. If {@code asConsole} is true, the command will be executed as the console,
     * otherwise it will be executed as the player.
     *
     * @param npc    The NPC
     * @param menu   The menu
     * @param player The player
     */
    @Override
    public void perform(InternalNpc npc, Menu menu, Player player) {
        if (!processConditions(player)) return;
        Bukkit.dispatchCommand(asConsole ? Bukkit.getConsoleSender() : player, command);
    }

    @Override
    public String serialize() {
        return "RunCommand{raw=`" + command + "`, asConsole=" + asConsole + ", delay=" + getDelay() + ", mode=" + getMode().name() +
                ", conditions=" + getConditionSerialized() + "}";
    }

    @Override
    public Action clone() {
        return new RunCommand(command, asConsole, getDelay(), getMode(), new ArrayList<>(getConditions()));
    }

    public class RunCommandCustomizer implements Menu {

        private final RunCommand action;

        public RunCommandCustomizer(RunCommand action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "RUN_COMMAND_CUSTOMIZER";
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
                    .setButton(player.hasPermission("customnpcs.run_command.enable_console") ? 21 : 22, setCommand(player))
                    .setButton(23, toggle(player))
                    .build();
        }

        private Button toggle(Player player) {
            if (!player.hasPermission("customnpcs.run_command.enable_console")) return MenuItems.MENU_GLASS;
            List<Component> lore = new ArrayList<>();
            if (isAsConsole()) {
                lore.addAll(Utils.list(Msg.lore(player.locale(), "customnpcs.menus.action.command.as_console.warning")));
            }
            lore.add(Msg.translate(player.locale(), "customnpcs.items.click_to_change"));
            return Button.clickable(ItemBuilder.modern(isAsConsole() ? RED_CANDLE : GREEN_CANDLE)
                            .setLore(lore.toArray(new Component[]{}))
                            .setDisplay(isAsConsole() ? Msg.translate(player.locale(), "customnpcs.menus.action.command.as_console.true") : Msg.translate(player.locale(), "customnpcs.menus.action.command.as_console.false"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        Player p = (Player) event.getWhoClicked();
                        if (!p.hasPermission("customnpcs.run_command.enable_console")) {
                            p.sendMessage(Msg.translate(player.locale(), "customnpcs.commands.no_permission"));
                            return;
                        }

                        setAsConsole(!isAsConsole());
                        menuView.updateButton(23, button -> button.setItem(toggle(p).getItem()));
                    }));
        }

        private Button setCommand(Player player) {
            return Button.clickable(ItemBuilder.modern(ANVIL)
                            .setDisplay(Component.text("/" + getCommand()))
                            .setLore(Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        CustomNPCs plugin = CustomNPCs.getInstance();
                        Player p = (Player) event.getWhoClicked();
                        p.closeInventory();
                        plugin.commandWaiting.add(p.getUniqueId());
                        new CommandRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }));
        }
    }
}
