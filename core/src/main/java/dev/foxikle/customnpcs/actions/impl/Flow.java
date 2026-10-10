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
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class Flow extends Action {

    // the first integer should always be 0
    private final Map<Action, Integer> actions;

    public static final StructCodec<Flow> CODEC = StructCodec.struct(
            "actions", Action.CODEC.mapValue(StructCodec.INT), Flow::getActions,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            Flow::new
    );

    public Flow(Map<Action, Integer> actions, int delay, Selector selector, List<Condition> conditions, int cooldown, UUID uuid) {
        super(delay, selector, conditions, cooldown, uuid);
        this.actions = actions;
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        AtomicInteger offset = new AtomicInteger(1);
        actions.forEach((action, integer) -> {
            player.getScheduler().runDelayed(CustomNPCs.getInstance(),
                    _ -> action.perform(npc, player),
                    () -> {}, offset.get());
            offset.addAndGet(integer);
        });
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(Material.GLOW_ITEM_FRAME)
                .displayName(Msg.get(player, "favicons.flow"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())),
                        Component.empty(),
                        Msg.get(player, "favicons.flow.description")
                )
                .build();
    }

    @Override
    public Menu<Component> getMenu() {
        return null;
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
                (v, e) -> {
                    e.setCancelled(true);
                    Player p = (Player) e.getWhoClicked();
                    p.playSound(e.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);

                    Flow flow = new Flow(new HashMap<>(), 0, Selector.ONE, new ArrayList<>(), 0, UUID.randomUUID());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), flow);
                    v.lotus().openMenu(p, flow.getMenu());
                }
        );
    }

    @Override
    public Action clone() {
        return new Flow(new HashMap<>(actions), getDelay(), getSelector(),
                getConditions(), getCooldown(), getUuid());
    }

    @NotNullByDefault
    @AllArgsConstructor
    public class FlowCustomizer implements Menu<Component> {

        private final Flow flow;

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
            return MenuUtils.actionBase(flow, view.viewer())

                    .build();
        }
    }

}
