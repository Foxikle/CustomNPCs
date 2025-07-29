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
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.Material.*;

@Getter
@Setter
public class Teleport extends Action {

    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    /**
     * Creates a new SendMessage with the specified message
     *
     * @param x            The x coordinate
     * @param y            The y coordinate
     * @param z            The z coordinate
     * @param pitch        The pitch of the player
     * @param yaw          The yaw of the player
     * @param conditionals The conditionals
     * @param delay        The delay
     * @param mode         The selection mode of the action's conditions
     */
    public Teleport(double x, double y, double z, float pitch, float yaw, int delay, Condition.SelectionMode mode, List<Condition> conditionals, int cooldown) {
        super(delay, mode, conditionals, cooldown);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param x            The x coordinate
     * @param y            The y coordinate
     * @param z            The z coordinate
     * @param pitch        The pitch of the player
     * @param yaw          The yaw of the player
     * @param conditionals The conditionals
     * @param delay        The delay
     * @param mode         The selection mode of the action's conditions
     * @deprecated Use {@link Teleport#Teleport(double, double, double, float, float, int, Condition.SelectionMode, List, int)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Teleport(double x, double y, double z, float pitch, float yaw, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals, 0);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(ENDER_PEARL)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.teleport"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.favicons.teleport.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);

                    Teleport actionImpl = new Teleport(0, 0, 0, 0F, 0F, 0, Condition.SelectionMode.ONE, new ArrayList<>(), 0);
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.getAPI().openMenu(p, actionImpl.getMenu());
                }));
    }

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

        Teleport message = new Teleport(x, y, z, pitch, yaw, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown());
        return clazz.cast(message);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(ENDER_PEARL).setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.teleport"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.x", x),
                        Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.y", y),
                        Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.z", z),
                        Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.pitch", pitch),
                        Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.yaw", yaw),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new TeleportCustomizer(this);
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
        player.teleportAsync(new Location(npc.getWorld(), x, y, z, yaw, pitch));
        activateCooldown(player.getUniqueId());
    }

    @Override
    public String serialize() {
        Map<String, Object> params = new HashMap<>();
        params.put("x", x);
        params.put("y", y);
        params.put("z", z);
        params.put("yaw", yaw);
        params.put("pitch", pitch);
        return generateSerializedString("Teleport", params);
    }

    @Override
    public Action clone() {
        return new Teleport(getX(), getY(), getZ(), getPitch(), getYaw(), getDelay(), getMode(), getConditions(), getCooldown());
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
            return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.action_customizer.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            this.displayLore = Msg.translate(player.locale(), "customnpcs.menus.action.teleport.in_blocks");

            Component[] incLore = Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.increment.description");
            Component[] decLore = Msg.lore(player.locale(), "customnpcs.menus.action_customizer.delay.decrement.description");

            return MenuUtils.actionBase(action, player)

                    // displays
                    .setButton(19, Button.clickable(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.x", action.getX()), displayLore), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)))
                    ).setButton(20, Button.clickable(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.y", action.getY()), displayLore), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)))
                    ).setButton(21, Button.clickable(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.z", action.getZ()), displayLore), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)))
                    ).setButton(23, compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.pitch", action.getPitch()))
                    ).setButton(24, compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.yaw", action.getYaw())))

                    // increments
                    .setButton(10, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.increase_x"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setX(action.getX() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setX(action.getX() + 1);
                                } else if (event.isRightClick()) {
                                    action.setX(action.getX() + 5);
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.x", action.getX()), displayLore)));
                            }))
                    ).setButton(11, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.increase_y"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setY(action.getY() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setY(action.getY() + 1);
                                } else if (event.isRightClick()) {
                                    action.setY(action.getY() + 5);
                                }
                                menuView.updateButton(20, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.y", action.getY()), displayLore)));
                            }))
                    ).setButton(12, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.increase_z"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setZ(action.getZ() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setZ(action.getZ() + 1);
                                } else if (event.isRightClick()) {
                                    action.setZ(action.getZ() + 5);
                                }
                                menuView.updateButton(21, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.z", action.getZ()), displayLore)));
                            }))
                    ).setButton(14, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.increase_pitch"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.getPitch() == 90) {
                                    event.getWhoClicked().sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.pitch_over_90"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setPitch(Math.min(action.getPitch() + 20, 90));
                                } else if (event.isLeftClick()) {
                                    action.setPitch(Math.min(action.getPitch() + 1, 90));
                                } else if (event.isRightClick()) {
                                    action.setPitch(Math.min(action.getPitch() + 5, 90));
                                }
                                menuView.updateButton(23, button -> button.setItem(compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.pitch", action.getPitch())).getItem()));
                            }))
                    ).setButton(15, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.increase_yaw"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.getYaw() == 180) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.yaw_over_180"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setYaw(Math.min(action.getYaw() + 20, 180));
                                } else if (event.isLeftClick()) {
                                    action.setYaw(Math.min(action.getYaw() + 1, 180));
                                } else if (event.isRightClick()) {
                                    action.setYaw(Math.min(action.getYaw() + 5, 180));
                                }
                                menuView.updateButton(24, button -> button.setItem(compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.yaw", action.getYaw())).getItem()));
                            })))

                    // decreasers

                    .setButton(28, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.decrease_x"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setX(action.getX() - 20);
                                } else if (event.isLeftClick()) {
                                    action.setX(action.getX() - 1);
                                } else if (event.isRightClick()) {
                                    action.setX(action.getX() - 5);
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.x", action.getX()), displayLore)));
                            }))
                    ).setButton(29, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.decrease_y"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setY(action.getY() - 20);
                                } else if (event.isLeftClick()) {
                                    action.setY(action.getY() - 1);
                                } else if (event.isRightClick()) {
                                    action.setY(action.getY() - 5);
                                }
                                menuView.updateButton(20, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.y", action.getY()), displayLore)));
                            }))
                    ).setButton(30, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.decrease_z"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setZ(action.getZ() - 20);
                                } else if (event.isLeftClick()) {
                                    action.setZ(action.getZ() - 1);
                                } else if (event.isRightClick()) {
                                    action.setZ(action.getZ() - 5);
                                }
                                menuView.updateButton(21, button -> button.setItem(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.z", action.getZ()), displayLore)));
                            }))
                    ).setButton(32, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.decrease_pitch"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.getPitch() == -90) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.pitch_under_90"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setPitch(Math.max(action.getPitch() - 20, -90));
                                } else if (event.isLeftClick()) {
                                    action.setPitch(Math.max(action.getPitch() - 1, -90));
                                } else if (event.isRightClick()) {
                                    action.setPitch(Math.max(action.getPitch() - 5, -90));
                                }
                                menuView.updateButton(23, button -> button.setItem(compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.pitch", action.getPitch())).getItem()));
                            }))
                    ).setButton(33, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.decrease_yaw"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.getYaw() == -180) {
                                    player.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.yaw_under_180"));
                                    return;
                                }

                                if (event.isShiftClick()) {
                                    action.setYaw(Math.max(action.getYaw() - 20, -180));
                                } else if (event.isLeftClick()) {
                                    action.setYaw(Math.max(action.getYaw() - 1, -180));
                                } else if (event.isRightClick()) {
                                    action.setYaw(Math.max(action.getYaw() - 5, -180));
                                }
                                menuView.updateButton(24, button -> button.setItem(compassDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.teleport.display.yaw", action.getYaw())).getItem()));
                            })))

                    .build();
        }

        private Button compassDisplay(Component display) {
            return Button.clickable(ItemBuilder.modern(COMPASS).setDisplay(display).setLore(displayLore).build(), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)));
        }
    }
}
