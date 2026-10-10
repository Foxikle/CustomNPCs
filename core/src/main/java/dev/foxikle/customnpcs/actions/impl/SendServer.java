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

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

import static org.bukkit.Material.GRASS_BLOCK;
import static org.bukkit.Material.OAK_HANGING_SIGN;

@Getter
@Setter
@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class SendServer extends Action {

    public static final StructCodec<SendServer> CODEC = StructCodec.struct(
            "server", Codec.STRING, SendServer::getServer,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            SendServer::new
    );

    private String server;


    public SendServer(String server, int delay, Selector mode, List<Condition> conditionals, int cooldown,
                      @Nullable UUID uuid) {
        super(delay, mode, conditionals, cooldown, uuid);
        this.server = server;
    }

    public Button creationButton(Player player) {
        return
                Button.clickable(ItemBuilder.of(GRASS_BLOCK)
                                .displayName(Msg.get(player, "favicons.server"))
                                .lore(Msg.lore(player.locale(), "favicons.server.description"))
                                .build(),
                        (menuView, event) -> {
                            event.setCancelled(true);
                            Player p = (Player) event.getWhoClicked();
                            p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            //todo: watch out for duplications

                            SendServer actionImpl = new SendServer("server", 0, Selector.ONE, new ArrayList<>(), 0,
                                    UUID.randomUUID());
                            CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), actionImpl);
                            menuView.lotus().openMenu(p, actionImpl.getMenu());
                        });
    }


    @Override
    public void perform(InternalNpc npc, Player player) {
        if (!processConditions(player)) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(player.getName());
        out.writeUTF(server);
        player.sendPluginMessage(CustomNPCs.getInstance(), "BungeeCord", out.toByteArray());
        activateCooldown(player.getUniqueId());
    }


    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(GRASS_BLOCK).displayName(Msg.get(player, "favicons.server"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())), Msg.format(""),
                        Msg.get(player, "favicons.server.target", Arg.arg(server)),
                        Msg.format(""),
                        Msg.get(player, "favicons.edit"),
                        Msg.get(player, "favicons.remove")
                ).build();
    }

    @Override
    public Menu<Component> getMenu() {
        return new SendServerCustomizer(this);
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "SendServer";
    }

    @Override
    public Action clone() {
        return new SendServer(server, getDelay(), getSelector(), getConditions(), getCooldown(), getUuid());
    }

    @Deprecated(forRemoval = true)
    public static <T extends Action> T deserialize(String serialized, Class<T> clazz) {
        if (!clazz.equals(SendServer.class)) {
            throw new IllegalArgumentException("Cannot deserialize " + clazz.getName() + " to " + SendServer.class.getName());
        }
        String server = parseString(serialized, "server");
        ParseResult pr = parseBase(serialized);

        SendServer message = new SendServer(server, pr.delay(), pr.mode(), pr.conditions(), pr.cooldown(),
                UUID.randomUUID());

        return clazz.cast(message);
    }

    @NotNullByDefault
    public class SendServerCustomizer implements Menu<Component> {

        private final SendServer action;

        public SendServerCustomizer(SendServer action) {
            this.action = action;
        }

        @Override
        public String name() {
            return "SEND_SERVER_CUSTOMIZER";
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
            return MenuUtils.actionBase(action, view.viewer())
                    .set(Slot.of(22), Button.clickable(ItemBuilder.of(OAK_HANGING_SIGN)
                                    .displayName(Component.text(getServer()))
                                    .lore(Msg.get(view.viewer(), "items.click_to_change"))
                                    .build(),
                            (_, event) -> {
                                CustomNPCs plugin = CustomNPCs.getInstance();
                                Player p = (Player) event.getWhoClicked();
                                p.closeInventory();
                                plugin.wait(p, WaitingType.SERVER);
                                event.setCancelled(true);
                                p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                            }))
                    .build();
        }
    }
}
