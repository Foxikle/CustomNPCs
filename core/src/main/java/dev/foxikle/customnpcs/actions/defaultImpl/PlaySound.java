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
import dev.foxikle.customnpcs.internal.runnables.SoundRunnable;
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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.foxikle.customnpcs.internal.utils.Utils.DECIMAL_FORMAT;
import static org.bukkit.Material.*;

@Getter
@Setter
public class PlaySound extends Action {

    public static Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(BELL)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.sound"))
                        .setLore(Msg.lore(player.locale(), "customnpcs.favicons.sound.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                    PlaySound actionImpl = new PlaySound("minecraft:ui.button.click", 1, 1, 0, Condition.SelectionMode.ONE, new ArrayList<>(), 0);
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.getAPI().openMenu(p, actionImpl.getMenu());
                }));
    }

    float volume;
    float pitch;
    private String sound;

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param sound  The sound enum constants
     * @param pitch  The pitch, between 0.0f and 1.0f
     * @param volume The volume, between 0.0f and 1.0f
     */
    public PlaySound(String sound, float volume, float pitch, int delay, Condition.SelectionMode mode, List<Condition> conditionals, int cooldown) {
        super(delay, mode, conditionals, cooldown);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Creates a new SendMessage with the specified message
     *
     * @param sound  The sound enum constants
     * @param pitch  The pitch, between 0.0f and 1.0f
     * @param volume The volume, between 0.0f and 1.0f
     * @deprecated Use {@link PlaySound#PlaySound(String, float, float, int, Condition.SelectionMode, List, int)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public PlaySound(String sound, float volume, float pitch, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals, 0);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(PlaySound.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + PlaySound.class.getName());
        }
        String sound = parseString(serialized, "sound");
        float volume = parseFloat(serialized, "volume");
        float pitch = parseFloat(serialized, "pitch");

        ParseResult pr = parseBase(serialized);

        PlaySound message = new PlaySound(sound, volume, pitch, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown());

        return clazz.cast(message);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.modern(BELL).setDisplay(Msg.translate(player.locale(), "customnpcs.favicons.sound"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.menus.action.sound.sound", sound),
                        Msg.translate(player.locale(), "customnpcs.menus.action.sound.volume", DECIMAL_FORMAT.format(volume)),
                        Msg.translate(player.locale(), "customnpcs.menus.action.sound.pitch", DECIMAL_FORMAT.format(pitch)),
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
        Sound sound = Sound.sound(Key.key(this.sound), Sound.Source.MASTER, volume, pitch);
        player.playSound(sound);
        activateCooldown(player.getUniqueId());
    }

    @Override
    public String serialize() {
        return generateSerializedString("PlaySound", Map.of("sound", sound, "volume", volume, "pitch", pitch));
    }

    public Action clone() {
        return new PlaySound(sound, volume, pitch, getDelay(), getMode(), new ArrayList<>(getConditions()), getCooldown());
    }

    @Override
    public Menu getMenu() {
        return new PlaySoundCustomizer(this);
    }

    public class PlaySoundCustomizer implements Menu {
        private final PlaySound action;

        public PlaySoundCustomizer(PlaySound action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "PLAY_SOUND_CUSTOMIZER";
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

            Component incLore = Msg.translate(player.locale(), "customnpcs.menus.action.sound.increase");
            Component decLore = Msg.translate(player.locale(), "customnpcs.menus.action.sound.decrease");

            return MenuUtils.actionBase(action, player)

                    // displays
                    .setButton(19, pitch(player))
                    .setButton(21, volume(player))

                    // increment
                    .setButton(10, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.increase_pitch"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                                setPitch(getPitch() + .1f);
                                menuView.replaceButton(19, pitch(player));
                            }))
                    ).setButton(12, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.increase_volume"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                                setVolume(getVolume() + .1f);
                                menuView.replaceButton(21, volume(player));
                            })))

                    // decrement
                    .setButton(28, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.decrease_pitch"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                                Player p1 = (Player) event.getWhoClicked();
                                if (getPitch() - .1 <= 0) {
                                    p1.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.sound.invalid_pitch"));
                                } else {
                                    setPitch(getPitch() - .1f);
                                }
                                menuView.replaceButton(19, pitch(player));
                            }))
                    ).setButton(30, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.decrease_volume"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                                Player p1 = (Player) event.getWhoClicked();
                                if (getVolume() - .1 <= 0) {
                                    p1.sendMessage(Msg.translate(player.locale(), "customnpcs.menus.action.sound.invalid_volume"));
                                } else {
                                    setVolume(getVolume() - .1f);
                                    menuView.replaceButton(21, volume(player));
                                }
                            })))

                    // select sound button
                    .setButton(24, Button.clickable(ItemBuilder.modern(OAK_SIGN)
                                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.sound", getSound()))
                                    .setLore(Component.empty(), Msg.translate(player.locale(), "customnpcs.items.click_to_change"))
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1, 1));
                                Player p = (Player) event.getWhoClicked();
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.SOUND);
                                new SoundRunnable(p, plugin).runTaskTimer(plugin, 0, 10);
                            })))


                    .build();


        }

        private Button volume(Player player) {
            return Button.clickable(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.volume", DECIMAL_FORMAT.format(getVolume()))), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)));
        }

        private Button pitch(Player player) {
            return Button.clickable(MenuItems.genericDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.sound.pitch", DECIMAL_FORMAT.format(getPitch()))), ButtonClickAction.plain((menu, event) -> event.setCancelled(true)));
        }
    }
}
