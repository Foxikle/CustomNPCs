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
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.bukkit.Material.MILK_BUCKET;
import static org.bukkit.Material.POTION;

@Getter
@Setter
public class RemoveEffect extends Action {

    public static final Button CREATION_BUTTON = Button.clickable(ItemBuilder.modern(MILK_BUCKET)
                    .setDisplay(Msg.translate("customnpcs.favicons.remove_effect"))
                    .setLore(Msg.lore("customnpcs.favicons.remove_effect.description"))
                    .build(),
            ButtonClickAction.plain((menuView, event) -> {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                RemoveEffect actionImpl = new RemoveEffect("SPEED", 0, Condition.SelectionMode.ONE, new ArrayList<>());
                CustomNPCs.getInstance().editingActions.put(player.getUniqueId(), actionImpl);
                menuView.getAPI().openMenu(player, actionImpl.getMenu());
            }));
    private static final List<Field> fields = Stream.of(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();
    private String effect;

    /**
     * Creates a new GiveEffect with the specified parameters
     *
     * @param effect The raw message
     */
    public RemoveEffect(String effect, int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        super(delay, mode, conditionals);
        this.effect = effect;
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(RemoveEffect.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + RemoveEffect.class.getName());
        }
        String effect = serialized.replaceAll(".*effect=`(.*?)`.*", "$1");

        int delay = Integer.parseInt(serialized.replaceAll(".*delay=(\\d+).*", "$1"));
        Condition.SelectionMode mode = Condition.SelectionMode.valueOf(serialized.replaceAll(".*mode=([A-Z_]+).*", "$1"));

        String conditionsJson = serialized.replaceAll(".*conditions=\\[(.*?)]}.*", "$1");
        List<Condition> conditions = deserializeConditions(conditionsJson);

        RemoveEffect message = new RemoveEffect(effect, delay, mode, conditions);

        return clazz.cast(message);
    }

    @Override
    public ItemStack getFavicon() {
        return ItemBuilder.modern(MILK_BUCKET).setDisplay(Msg.translate("customnpcs.favicons.remove_effect"))
                .setLore(
                        Msg.translate("customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate("customnpcs.favicons.give_effect.effect", effect),
                        Msg.format(""),
                        Msg.translated("customnpcs.favicons.edit"),
                        Msg.translated("customnpcs.favicons.remove")
                ).build();
    }

    @Override
    public Menu getMenu() {
        return new RemoveEffectCustomizer(this);
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
        player.removePotionEffect(Objects.requireNonNull(PotionEffectType.getByName(effect)));
    }

    @Override
    public String serialize() {

        return "RemoveEffect{effect=`" + effect + "`, delay=" + getDelay() + ", mode=" + getMode().name() +
                ", conditions=" + getConditionSerialized() + "}";
    }

    @Override
    public Action clone() {
        return new RemoveEffect(effect, getDelay(), getMode(), new ArrayList<>(getConditions()));
    }

    public class RemoveEffectCustomizer implements Menu {
        private final RemoveEffect action;

        public RemoveEffectCustomizer(RemoveEffect action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "REMOVE_EFFECT_CUSTOMIZER";
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

            return MenuUtils.actionBase(action)
                    .setButton(22, generateToggleEffect())
                    .build();
        }


        private Button generateToggleEffect() {
            List<Component> lore = new ArrayList<>();
            fields.forEach(field -> {
                if (!Objects.equals(action.getEffect(), field.getName()))
                    lore.add(Utils.mm("<green>" + field.getName()));
                else lore.add(Utils.mm("<dark_aqua>▸ " + field.getName()));
            });
            return Button.clickable(ItemBuilder.modern(POTION)
                            .setDisplay(Msg.translate("customnpcs.menus.action.remove_effect.effect"))
                            .addFlags(ItemFlag.values())
                            .setLore(lore.toArray(new Component[]{}))
                            .build(),
                    ButtonClickAction.plain((menuView, event) -> {
                        event.setCancelled(true);
                        Player player = (Player) event.getWhoClicked();
                        player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                        List<String> effects = new ArrayList<>();
                        fields.forEach(field -> effects.add(field.getName()));

                        int index = effects.indexOf(getEffect());
                        if (event.isLeftClick()) {
                            if (effects.size() > (index + 1)) {
                                setEffect(effects.get(index + 1));
                            } else {
                                setEffect(effects.get(0));
                            }
                        } else if (event.isRightClick()) {
                            if (index == 0) {
                                setEffect(effects.get(effects.size() - 1));
                            } else {
                                setEffect(effects.get(index - 1));
                            }
                        }
                        menuView.updateButton(22, button -> button.setItem(generateToggleEffect().getItem()));
                    }));
        }
    }
}