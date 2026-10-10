/*
 * Copyright (c) 2026. Foxikle
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

import com.google.common.reflect.TypeToken;
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
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.button.ClickAction;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(onConstructor_ = {@ApiStatus.Internal})
public class FollowPresetPath extends Action {

    public static final StructCodec<FollowPresetPath> CODEC = StructCodec.struct(
            "nodes", RecordedPathNode.CODEC.list(), FollowPresetPath::getPath,
            "loop", Codec.BOOLEAN, FollowPresetPath::isLoop,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
            "uuid", Codec.UUID_STRING.optional(), Action::getUuid,
            FollowPresetPath::new
    );

    public static final Map<UUID, Location> lastRecordedPos = new ConcurrentHashMap<>();
    private static final Map<UUID, List<RecordedPathNode>> recordingPaths = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> viewPaths = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> recordingTasks = new ConcurrentHashMap<>();
    private static final Map<InternalNpc, BukkitTask> activePlaybacks = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private List<RecordedPathNode> path;

    @Getter
    @Setter
    private boolean loop;

    public FollowPresetPath(List<RecordedPathNode> path, boolean loop, int delay, Selector mode,
                            List<Condition> conditions, int cooldown, @Nullable UUID uuid) {
        super(delay, mode, conditions, cooldown, uuid);
        this.path = path;
        this.loop = loop;
    }

    public static void startRecording(final Player player) {
        final UUID uuid = player.getUniqueId();
        recordingPaths.put(uuid, new ArrayList<>());
        lastRecordedPos.put(uuid, player.getLocation());
        viewPaths.put(uuid, Bukkit.getScheduler().runTaskTimer(CustomNPCs.getInstance(), () -> {
            Location loc = new Location(player.getWorld(), 0, 0, 0);
            for (RecordedPathNode n : recordingPaths.get(uuid)) {
                loc = loc.add(n.getDelta(loc.getWorld()));
                player.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
            }

        }, 5, 5));
        recordingTasks.put(uuid, Bukkit.getScheduler().runTaskTimer(CustomNPCs.getInstance(), () -> {
            recordMovement(player);
        }, 1, 1));

        CustomNPCs.getInstance().wait(player, WaitingType.RECORDING);
    }

    public static List<RecordedPathNode> stopRecording(Player player) {
        UUID uuid = player.getUniqueId();
        viewPaths.remove(uuid).cancel();
        recordingTasks.remove(uuid).cancel();
        lastRecordedPos.remove(uuid);
        return recordingPaths.remove(uuid);
    }

    // Better recordMovement
    public static void recordMovement(Player player) {
        final Location loc = player.getLocation().clone();
        UUID uuid = player.getUniqueId();
        List<RecordedPathNode> currentPath = recordingPaths.get(uuid);
        if (currentPath == null) return;

        if (currentPath.isEmpty()) {
            lastRecordedPos.put(uuid, loc);
            currentPath.add(new RecordedPathNode(0, loc, new Location(loc.getWorld(), 0, 0, 0)));
            return;
        }

        currentPath.add(new RecordedPathNode(currentPath.size() + 1, player.getLocation(), lastRecordedPos.get(uuid)));
        lastRecordedPos.put(uuid, player.getLocation());
    }

    @Deprecated(forRemoval = true)
    public static FollowPresetPath deserialize(String serialized, Class<? extends Action> clazz) {
        if (clazz != FollowPresetPath.class)
            throw new IllegalArgumentException("This deserialize method only supports the FollowPresetPathAction");
        ParseResult result = parseBase(serialized);
        String pathData = parseString(serialized, "path");
        List<RecordedPathNode> path = CustomNPCs.getGson().fromJson(pathData, new TypeToken<List<RecordedPathNode>>() {
        }.getType());
        boolean loop = parseBoolean(pathData, "loop");
        return new FollowPresetPath(path, loop, result.delay(), result.mode(), result.conditions(),
                result.cooldown(), UUID.randomUUID());
    }

    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.of(Material.RAIL)
                        .displayName(Msg.get(player, "menus.action.follow_path.favicon"))
                        .lore(Msg.get(player, "menus.action.follow_path.description"))
                        .build(),
                (menuView, event) -> {
                    Player p = (Player) event.getWhoClicked();
                    event.setCancelled(true);
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    FollowPresetPath action = new FollowPresetPath(new ArrayList<>(), false, 0,
                            Selector.ONE, new ArrayList<>(), 0, UUID.randomUUID());
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), action);
                    menuView.lotus().openMenu(p, action.getMenu());
                });
    }

    @Override
    public void perform(InternalNpc npc, Player player) {
        if (path == null || path.isEmpty()) return;
        if (activePlaybacks.containsKey(npc)) {
            if (loop) return;
            activePlaybacks.get(npc).cancel();
        }
        final Location returnTo = npc.getCurrentLocation();
        final Location first = path.getFirst().getDelta(npc.getWorld());
        npc.teleport(first);

        final int[] currentIndex = {1};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(CustomNPCs.getInstance(), () -> {
            if (currentIndex[0] >= path.size()) {
                if (loop) {
                    currentIndex[0] = 1;
                    npc.teleport(first);
                } else {
                    BukkitTask t = activePlaybacks.remove(npc);
                    if (t != null) t.cancel();
                    Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> npc.teleport(returnTo), 1L);
                }
                return;
            }

            RecordedPathNode node = path.get(currentIndex[0]);
            Location loc = node.getDelta(npc.getWorld());
            npc.setYRotation(loc.getYaw());
            npc.setXRotation(loc.getPitch());
            npc.moveTo(loc.toVector());
            currentIndex[0]++;
        }, 0, 1);


        activePlaybacks.put(npc, task);
    }

    @Override
    public ItemStack getFavicon(Player player) {
        return ItemBuilder.of(Material.RAIL)
                .displayName(Msg.get(player, "menus.action.follow_path.favicon"))
                .lore(
                        Msg.get(player, "favicons.delay", Arg.arg(getDelay())),
                        Msg.format(""),
                        Msg.get(player, "menus.action.follow_path.nodes", Arg.arg(path.size())),
                        Msg.get(player, "menus.action.follow_path.looped", Arg.arg(loop)),
                        Msg.format(""),
                        Msg.get(player, "favicons.edit"),
                        Msg.get(player, "favicons.remove")
                )
                .build();
    }

    @Override
    public Menu<Component> getMenu() {
        return new FollowPathCustomizer(this);
    }

    @Override
    public Action clone() {
        return new FollowPresetPath(new ArrayList<>(path), loop, getDelay(), getSelector(),
                new ArrayList<>(getConditions()), getCooldown(), getUuid());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "FollowPresetPath";
    }

    @AllArgsConstructor
    @NotNullByDefault
    private class FollowPathCustomizer implements Menu<Component> {
        private final FollowPresetPath action;

        @Override
        public String name() {
            return "follow_path_customizer";
        }

        @Override
        public Component title(MenuView<Component, ?> view) {
            return Msg.get(view.viewer(), "menus.action.follow_path.title");
        }

        @Override
        public Capacity capacity(MenuView<Component, ?> view) {
            return Capacity.ofRows(5);
        }

        @Override
        public Content content(MenuView<Component, ?> view) {
            return MenuUtils.actionBase(action, view.viewer())
                    .set(Slot.of(20), button(view.viewer()))
                    .set(Slot.of(24), candle(view.viewer()))
                    .build();
        }

        private Button candle(Player player) {
            return Button.clickable(ItemBuilder.of(action.loop ? Material.GREEN_CANDLE : Material.RED_CANDLE)
                            .displayName(Msg.get(player, "menus.action.follow_path.loop." + action.loop))
                            .lore(Msg.lore(player.locale(), "menus.action.follow_path.loop." + action.loop + ".description"))
                            .build(), (m, e) -> {
                        e.setCancelled(true);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                        action.loop = !action.loop;
                        m.content().set(Slot.of(24), candle(player));
                        //todo: autostart
                    }
            );
        }

        private Button button(Player player) {
            ClickAction click = (_, _) -> {
                player.closeInventory();
                startRecording(player);
            };
            if (action.path == null || action.path.isEmpty()) {
                return Button.clickable(ItemBuilder.of(Material.PLAYER_HEAD)
                        .displayName(Msg.get(player, "menus.action.follow_path.record"))
                        .lore(Msg.get(player, "menus.action.follow_path.record.lore"))
                        .build(), click);
            }

            return Button.clickable(ItemBuilder.of(Material.PLAYER_HEAD)
                    .displayName(Msg.get(player, "menus.action.follow_path.rerecord"))
                    .lore(Msg.get(player, "menus.action.follow_path.rerecord.lore", Arg.arg(path.size())))
                    .build(), click);
        }
    }
}
