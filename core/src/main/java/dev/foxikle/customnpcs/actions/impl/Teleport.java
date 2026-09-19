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
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.*;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class Teleport extends Action {

    public static final StructCodec<Teleport> CODEC = StructCodec.struct("x", Codec.DOUBLE, Teleport::getX, "y", Codec.DOUBLE, Teleport::getY, "z", Codec.DOUBLE, Teleport::getZ, "pitch", Codec.FLOAT, Teleport::getPitch, "yaw", Codec.FLOAT, Teleport::getYaw, "delay", Codec.INT, Action::getDelay, "selector", Codec.Enum(Selector.class), Action::getSelector, "conditions", Condition.CODEC.list(), Action::getConditions, "cooldown", Codec.INT, Action::getCooldown, "uuid", Codec.UUID_STRING.optional(), Action::getUuid, Teleport::new);

    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public Teleport(double x, double y, double z, float pitch, float yaw, int delay, Selector mode, List<Condition> conditionals, int cooldown, @Nullable UUID uuid) {
        super(delay, mode, conditionals, cooldown, uuid);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(ENDER_PEARL).setDisplay(Msg.get(player, "favicons.teleport")).setLore(Msg.lore(player.locale(), "favicons.teleport.description")).build(), ButtonClickAction.plain((menuView, event) -> {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);

            Teleport actionImpl = new Teleport(0, 0, 0, 0F, 0F, 0, Selector.ONE, new ArrayList<>(), 0, UUID.randomUUID());
            CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
            menuView.getAPI().openMenu(p, actionImpl.getMenu());
        }));
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(ENDER_PEARL).setDisplay(Msg.get(player, "favicons.teleport")).setLore(Msg.get(player, "favicons.delay", Arg.arg(getDelay())), Msg.format(""), Msg.get(player, "menus.action.teleport.display.x", Arg.arg(x)), Msg.get(player, "menus.action.teleport.display.y", Arg.arg(y)), Msg.get(player, "menus.action.teleport.display.z", Arg.arg(z)), Msg.get(player, "menus.action.teleport.display.pitch", Arg.arg(pitch)), Msg.get(player, "menus.action.teleport.display.yaw", Arg.arg(yaw)), Msg.format(""), Msg.get(player, "favicons.edit"), Msg.get(player, "favicons.remove")).build();
    }

    @Override
    public Menu getMenu() {
        return new TeleportCustomizer(this);
    }


    @Override
    public void perform(InternalNpc npc, Menu menu, Player player) {
        if (!processConditions(player)) return;
        player.teleportAsync(new Location(npc.getWorld(), x, y, z, yaw, pitch));
        activateCooldown(player.getUniqueId());
    }


    @Override
    public Action clone() {
        return new Teleport(getX(), getY(), getZ(), getPitch(), getYaw(), getDelay(), getSelector(), getConditions(), getCooldown(), getUuid());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "Teleport";
    }

    @Deprecated(forRemoval = true)
    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(Teleport.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + Teleport.class.getName());
        }

        double x = parseDouble(serialized, "x");
        double y = parseDouble(serialized, "y");
        double z = parseDouble(serialized, "z");
        float pitch = parseFloat(serialized, "pitch");
        float yaw = parseFloat(serialized, "yaw");
        ParseResult pr = parseBase(serialized);

        Teleport message = new Teleport(x, y, z, pitch, yaw, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown(), UUID.randomUUID());
        return clazz.cast(message);
    }

    public static class TeleportCustomizer implements Menu {

        private final Teleport action;
        Component displayLore;

        public TeleportCustomizer(Teleport action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "TELEPORT_CUSTOMIZER";
        }

        @Override
        public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
            return MenuTitles.createModern(Msg.get(player, "menus.action_customizer.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            this.displayLore = Msg.get(player, "menus.action.teleport.in_blocks");

            Component[] incLore = Msg.lore(player.locale(), "menus.action_customizer.delay.increment.description");
            Component[] decLore = Msg.lore(player.locale(), "menus.action_customizer.delay.decrement.description");

            return MenuUtils.actionBase(action, player)

                    // displays
                    .setButton(19, MenuItems.display(Msg.get(player, "menus.action.teleport.display.x", Arg.arg(action.getX())), displayLore))
                    .setButton(20, MenuItems.display(Msg.get(player, "menus.action.teleport.display.y", Arg.arg(action.getY())), displayLore))
                    .setButton(21, MenuItems.display(Msg.get(player, "menus.action.teleport.display.z", Arg.arg(action.getZ())), displayLore))
                    .setButton(23, compassDisplay(Msg.get(player, "menus.action.teleport.display.pitch", Arg.arg(action.getPitch()))))
                    .setButton(24, compassDisplay(Msg.get(player, "menus.action.teleport.display.yaw", Arg.arg(action.getYaw()))))

                    // increments
                    .setButton(10, Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action.teleport.increase_x")).setLore(incLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.x = Utils.incrementd(event).apply(action.x);
                        menuView.replaceButton(19, MenuItems.display(Msg.get(player, "menus.action.teleport.display.x", Arg.arg(action.getX())), displayLore));
                    }))).setButton(11, Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action.teleport.increase_y")).setLore(incLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.y = Utils.incrementd(event).apply(action.y);
                        menuView.replaceButton(20, MenuItems.display(Msg.get(player, "menus.action.teleport.display.y", Arg.arg(action.getY())), displayLore));
                    }))).setButton(12, Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action.teleport.increase_z")).setLore(incLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.z = Utils.incrementd(event).apply(action.z);

                        menuView.replaceButton(21, MenuItems.display(Msg.get(player, "menus.action.teleport.display.z", Arg.arg(action.getZ())), displayLore));
                    }))).setButton(14, Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action.teleport.increase_pitch")).setLore(incLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        if (action.getPitch() == 90) {
                            event.getWhoClicked().sendMessage(Msg.get(player, "menus.action.teleport.pitch_over_90"));
                            return;
                        }

                        action.pitch = Math.min(90, (float) Utils.incrementd(event).apply((double) action.pitch).doubleValue());
                        menuView.updateButton(23, b -> b.setItem(compassDisplay(Msg.get(player, "menus.action.teleport.display.pitch", Arg.arg(action.getPitch()))).getItem()));
                    }))).setButton(15, Button.clickable(ItemBuilder.modern(LIME_DYE).setDisplay(Msg.get(player, "menus.action.teleport.increase_yaw")).setLore(incLore).build(), ButtonClickAction.plain((v, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        if (action.getYaw() == 180) {
                            player.sendMessage(Msg.get(player, "menus.action.teleport.yaw_over_180"));
                            return;
                        }
                        action.yaw = Math.min(180, (float) Utils.incrementd(event).apply((double) action.yaw).doubleValue());
                        v.updateButton(24, b -> b.setItem(compassDisplay(Msg.get(player, "menus.action.teleport.display.yaw", Arg.arg(action.getYaw()))).getItem()));
                    })))

                    // decreasers

                    .setButton(28, Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action.teleport.decrease_x")).setLore(decLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.x = Utils.decrementd(event).apply(action.x);
                        menuView.replaceButton(19, MenuItems.display(Msg.get(player, "menus.action.teleport.display.x", Arg.arg(action.getX())), displayLore));
                    }))).setButton(29, Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action.teleport.decrease_y")).setLore(decLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.y = Utils.decrementd(event).apply(action.y);
                        menuView.replaceButton(20, MenuItems.display(Msg.get(player, "menus.action.teleport.display.y", Arg.arg(action.getY())), displayLore));
                    }))).setButton(30, Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action.teleport.decrease_z")).setLore(decLore).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.z = Utils.decrementd(event).apply(action.z);
                        menuView.replaceButton(21, MenuItems.display(Msg.get(player, "menus.action.teleport.display.z", Arg.arg(action.getZ())), displayLore));
                    }))).setButton(32, Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action.teleport.decrease_pitch")).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        if (action.getPitch() == -90) {
                            player.sendMessage(Msg.get(player, "menus.action.teleport.pitch_under_90"));
                            return;
                        }

                        action.pitch = Math.max(-90, (float) Utils.decrementd(event).apply((double) action.pitch).doubleValue());

                        menuView.updateButton(23, button -> button.setItem(compassDisplay(Msg.get(player, "menus.action.teleport.display.pitch", Arg.arg(action.getPitch()))).getItem()));
                    }))).setButton(33, Button.clickable(ItemBuilder.modern(RED_DYE).setDisplay(Msg.get(player, "menus.action.teleport.decrease_yaw")).build(), ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        if (action.getYaw() == -180) {
                            player.sendMessage(Msg.get(player, "menus.action.teleport.yaw_under_180"));
                            return;
                        }
                        action.yaw = Math.max(-180, (float) Utils.decrementd(event).apply((double) action.yaw).doubleValue());
                        menuView.updateButton(24, button -> button.setItem(compassDisplay(Msg.get(player, "menus.action.teleport.display.yaw", Arg.arg(action.getYaw()))).getItem()));
                    })))

                    .build();
        }

        private Button compassDisplay(Component display) {
            return Button.clickable(ItemBuilder.modern(COMPASS).setDisplay(display).setLore(displayLore).build(), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)));
        }
    }
}
