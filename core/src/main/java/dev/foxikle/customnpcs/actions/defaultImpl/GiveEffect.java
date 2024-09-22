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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.bukkit.Material.*;

@Getter
@Setter
public class GiveEffect extends Action {

    public static final Button CREATION_BUTTON = Button.clickable(ItemBuilder.modern(BREWING_STAND)
                    .setDisplay(Msg.translate("customnpcs.favicons.give_effect"))
                    .setLore(Msg.lore("customnpcs.favicons.give_effect.description"))
                    .build(),
            ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                GiveEffect actionImpl = new GiveEffect("SPEED", 100, 0, false, 0, Condition.SelectionMode.ONE, new ArrayList<>());
                CustomNPCs.getInstance().editingActions.put(player.getUniqueId(), actionImpl);
                menuView.getAPI().openMenu(player, actionImpl.getMenu());
            }));
    private static final List<Field> fields = Stream.of(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();

    boolean particles;
    private String effect;
    private int duration;
    private int amplifier;

    /**
     * Creates a new GiveEffect with the specified parameters
     *
     * @param effect The raw message
     */
    public GiveEffect(String effect, int duration, int amplifier, boolean particles, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.particles = particles;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(GiveEffect.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + GiveEffect.class.getName());
        }
        String effect = serialized.replaceAll(".*effect=`(.*?)`.*", "$1");
        int duration = Integer.parseInt(serialized.replaceAll(".*duration=(\\d+).*", "$1"));
        int amplifier = Integer.parseInt(serialized.replaceAll(".*amplifier=(\\d+).*", "$1"));
        boolean particles = Boolean.parseBoolean(serialized.replaceAll(".*particles=(true|false).*", "$1"));


        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        GiveEffect message = new GiveEffect(effect, duration, amplifier, particles, delay, mode, conditions);

        return clazz.cast(message);
    }

    public ItemStack getFavicon() {
        return ItemBuilder.modern(BREWING_STAND).setDisplay(Msg.translate("customnpcs.favicons.give_effect"))
                .setLore(
                        Msg.translate("customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate("customnpcs.favicons.give_effect.effect", effect),
                        Msg.translate("customnpcs.favicons.give_effect.duration", duration),
                        Msg.translate("customnpcs.favicons.give_effect.amplifier", amplifier),
                        Msg.translate("customnpcs.favicons.give_effect.particles", particles),
                        Msg.format(""),
                        Msg.translated("customnpcs.favicons.edit"),
                        Msg.translated("customnpcs.favicons.remove")
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
        if (PotionEffectType.getByName(effect) == null)
            throw new NullPointerException("Effect " + effect + " does not exist? Please tell @foxikle on discord how you managed this.");
        player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(effect)), duration, amplifier, true, !particles));
    }

    @Override
    public String serialize() {
        return "GiveEffect{effect=`" + effect + "`, duration=" + getDuration() + ", amplifier=" + getAmplifier() +
                ", particles=" + isParticles() + ", delay=" + getDelay() + ", mode=" + getMode().name() +
                ", conditions=" + getConditionSerialized() + "}";
    }

    public Action clone() {
        return new GiveEffect(getEffect(), getDuration(), getAmplifier(), isParticles(), getDelay(), getMode(), new ArrayList<>(getConditions()));
    }

    @Override
    public Menu getMenu() {
        return new GiveEffectCustomizer(this);
    }

    public static class GiveEffectCustomizer implements Menu {
        private final GiveEffect action;

        public GiveEffectCustomizer(GiveEffect action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "GIVE_EFFECT_CUSTOMIZER";
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
            Component[] displayLore = Msg.lore("customnpcs.menus.action.title.display.lore");
            return MenuUtils.actionBase(action)
                    .setButton(10, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_effect.duration.increase"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (event.isShiftClick()) {
                                    action.setDuration(action.getDuration() + 20);
                                } else if (event.isLeftClick()) {
                                    action.setDuration(action.getDuration() + 1);
                                } else if (event.isRightClick()) {
                                    action.setDuration(action.getDuration() + 5);
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.duration", action.getDuration()), displayLore)));
                            }))
                    ).setButton(12, Button.clickable(ItemBuilder.modern(LIME_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_effect.amplifier.increase"))
                                    .setLore(incLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                if (action.getAmplifier() == 255) {
                                    player.sendMessage(Msg.translated("customnpcs.menus.action.give_effect.amplifier_over_255"));
                                    return;
                                }
                                if (event.isShiftClick()) {
                                    action.setAmplifier(Math.min(255, action.getAmplifier() + 20));
                                } else if (event.isLeftClick()) {
                                    action.setAmplifier(Math.min(255, action.getAmplifier() + 1));
                                } else if (event.isRightClick()) {
                                    action.setAmplifier(Math.min(255, action.getAmplifier() + 5));
                                }
                                menuView.updateButton(21, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.amplifier", action.getAmplifier()), displayLore)));
                            }))
                    ).setButton(19, Button.empty(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.duration", action.getDuration()), displayLore))
                    ).setButton(21, Button.empty(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.amplifier", action.getAmplifier()), displayLore))
                    ).setButton(28, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_effect.duration.decrease"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                Player p = (Player) event.getWhoClicked();
                                if (action.getDuration() == 1) {
                                    p.sendMessage(Msg.translated("customnpcs.menus.action.give_effect.duration_under_1"));
                                    return;
                                }
                                if (event.isShiftClick()) {
                                    action.setDuration(Math.max(1, action.getDuration() - 20));
                                } else if (event.isLeftClick()) {
                                    action.setDuration(Math.max(1, action.getDuration() - 1));
                                } else if (event.isRightClick()) {
                                    action.setDuration(Math.max(1, action.getDuration() - 5));
                                }
                                menuView.updateButton(19, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.duration", action.getDuration()), displayLore)));
                            }))
                    ).setButton(30, Button.clickable(ItemBuilder.modern(RED_DYE)
                                    .setDisplay(Msg.translated("customnpcs.menus.action.give_effect.amplifier.decrease"))
                                    .setLore(decLore)
                                    .build(),
                            ButtonClickAction.plain((menuView, event) -> {
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                                Player p = (Player) event.getWhoClicked();
                                if (action.getAmplifier() == 0) {
                                    p.sendMessage(Msg.translated("customnpcs.menus.action.give_effect.amplifier_under_0"));
                                    return;
                                }
                                if (event.isShiftClick()) {
                                    action.setAmplifier(Math.max(0, action.getAmplifier() - 20));
                                } else if (event.isLeftClick()) {
                                    action.setAmplifier(Math.max(0, action.getAmplifier() - 1));
                                } else if (event.isRightClick()) {
                                    action.setAmplifier(Math.max(0, action.getAmplifier() - 5));
                                }
                                menuView.updateButton(21, button -> button.setItem(MenuItems.genericDisplay(Msg.translate("customnpcs.menus.action.give_effect.amplifier", action.getAmplifier()), displayLore)));
                            }))
                    ).setButton(23, generateParticles())
                    .setButton(25, generateToggleEffect())
                    .build();
        }

        private Button generateParticles() {
            return Button.clickable(ItemBuilder.modern(action.particles ? GREEN_CANDLE : RED_CANDLE)
                            .setDisplay(Msg.translate("customnpcs.menus.action.give_effect.particles", action.particles))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        action.setParticles(!action.isParticles());
                        menuView.updateButton(23, button -> button.setItem(generateParticles().getItem()));
                    }));
        }

        private Button generateToggleEffect() {
            List<Component> lore = new ArrayList<>();
            fields.forEach(field -> {
                if (!Objects.equals(action.getEffect(), field.getName()))
                    lore.add(Utils.mm("<green>" + field.getName()));
                else lore.add(Utils.mm("<dark_aqua>▸ " + field.getName()));
            });
            return Button.clickable(ItemBuilder.modern(POTION)
                            .setDisplay(Msg.translate("customnpcs.menus.action.give_effect.effect"))
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        List<String> effects = new ArrayList<>();
                        fields.forEach(field -> effects.add(field.getName()));

                        int index = effects.indexOf(action.getEffect());
                        if (event.isLeftClick()) {
                            if (effects.size() > (index + 1)) {
                                action.setEffect(effects.get(index + 1));
                            } else {
                                action.setEffect(effects.get(0));
                            }
                        } else if (event.isRightClick()) {
                            if (index == 0) {
                                action.setEffect(effects.get(effects.size() - 1));
                            } else {
                                action.setEffect(effects.get(index - 1));
                            }
                        }
                        menuView.updateButton(25, button -> button.setItem(generateToggleEffect().getItem()));
                    }));
        }
    }
}