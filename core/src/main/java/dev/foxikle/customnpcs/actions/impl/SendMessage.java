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
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.OAK_HANGING_SIGN;
import static org.bukkit.Material.PAPER;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class SendMessage extends Action {

    public static final StructCodec<SendMessage> CODEC = StructCodec.struct(
            "raw", Codec.STRING, SendMessage::getRawMessage,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            SendMessage::new
    );

    private String rawMessage;


    public SendMessage(String rawMessage, int delay, Selector mode, List<Condition> conditionals, int cooldown,
                       @Nullable UUID uuid) {
        super(delay, mode, conditionals, cooldown, uuid);
        this.rawMessage = rawMessage;
    }


    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.of(PAPER)
                        .displayName(Msg.get(player, "favicons.message"))
                        .lore(Msg.lore(player.locale(), "favicons.message.description"))
                        .build(),
                (menuView, event) -> {
                    event.setCancelled(true);
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);

                    SendMessage actionImpl = new SendMessage("", 0, Selector.ONE, new ArrayList<>(), 0,
                            UUID.randomUUID());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                    menuView.lotus().openMenu(p, actionImpl.getMenu());
                });
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(PAPER)
                .displayName(Msg.get(player, "favicons.message"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.get(player, "favicons.preview"),
                        Msg.format(getRawMessage().isEmpty() ?
                                "<dark_gray><i>" + Msg.translatedString(player.locale(), "messages.empty_string") :
                                "<!i>" + getRawMessage()),
                        Msg.format("<dark_aqua><st>                                    "),
                        Msg.get(player, "favicons.edit"),
                        Msg.get(player, "favicons.remove")
                ).build();
    }

    public Menu<Component> getMenu() {
        return new SendMessageCustomizer(this);
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        if (!processConditions(player)) return;

        player.sendMessage(Msg.format(Msg.papi(player, rawMessage)));
        activateCooldown(player.getUniqueId());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "SendMessage";
    }

    @Override
    public Action clone() {
        return new SendMessage(rawMessage, getDelay(), getSelector(), getConditions(), getCooldown(), getUuid());
    }

    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(SendMessage.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + SendMessage.class.getName());
        }
        String rawMessage = parseString(serialized, "raw");
        ParseResult pr = parseBase(serialized);
        SendMessage message = new SendMessage(rawMessage, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown(),
                UUID.randomUUID());

        return clazz.cast(message);
    }

    @NotNullByDefault
    @AllArgsConstructor
    public class SendMessageCustomizer implements Menu<Component> {

        private final SendMessage action;

        @Override
        public String name() {
            return "SEND_MESSAGE_CUSTOMIZER";
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
                    .set(Slot.of(22), Button.clickable(ItemBuilder.of(OAK_HANGING_SIGN)
                                    .displayName(Msg.format(getRawMessage().isEmpty() ?
                                            "<dark_gray><i>" + Msg.translatedString(player.locale(), "messages.empty_string") : "<!i>" + getRawMessage()))
                                    .lore(Msg.get(player, "items.click_to_change"))
                                    .build(),
                            (_, event) -> {
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                Player p = (Player) event.getWhoClicked();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.MESSAGE);
                                event.setCancelled(true);
                                player.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            }))
                    .build();
        }
    }
}
