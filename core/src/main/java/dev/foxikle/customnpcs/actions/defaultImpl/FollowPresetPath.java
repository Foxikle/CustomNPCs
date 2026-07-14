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

package dev.foxikle.customnpcs.actions.defaultImpl;

import com.google.common.reflect.TypeToken;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.runnables.RecordingRunnable;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
public class FollowPresetPath extends Action {

    public static final StructCodec<FollowPresetPath> CODEC = StructCodec.struct(
            "nodes", RecordedPathNode.CODEC.list(), FollowPresetPath::getPath,
            "loop", Codec.BOOLEAN, FollowPresetPath::isLoop,
            "delay", Codec.INT, Action::getDelay,
            "selector", Codec.Enum(Selector.class), Action::getSelector,
            "conditions", Condition.CODEC.list(), Action::getConditions,
            "cooldown", Codec.INT, Action::getCooldown,
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
                            List<Condition> conditions, int cooldown) {
        super(delay, mode, conditions, cooldown);
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
        new RecordingRunnable(player, CustomNPCs.getInstance()).runTaskTimer(CustomNPCs.getInstance(), 0, 10);
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
                result.cooldown());
    }

    public Button creationButton(Player player) {
        return Button.clickable(ItemBuilder.modern(Material.RAIL)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.favicon"))
                        .setLore(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.description"))
                        .build(),
                ButtonClickAction.plain((menuView, event) -> {
                    Player p = (Player) event.getWhoClicked();
                    event.setCancelled(true);
                    p.playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
                    FollowPresetPath action = new FollowPresetPath(new ArrayList<>(), false, 0,
                            Selector.ONE, new ArrayList<>(), 0);
                    CustomNPCs.getInstance().editingActions.put(p.getUniqueId(), action);
                    menuView.getAPI().openMenu(p, action.getMenu());
                }));
    }

    @Override
    public void perform(InternalNpc npc, Menu menu, Player player) {
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
        return ItemBuilder.modern(Material.RAIL)
                .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.favicon"))
                .setLore(
                        Msg.translate(player.locale(), "customnpcs.favicons.delay", getDelay()),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.nodes", path.size()),
                        Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.looped", loop),
                        Msg.format(""),
                        Msg.translate(player.locale(), "customnpcs.favicons.edit"),
                        Msg.translate(player.locale(), "customnpcs.favicons.remove")
                )
                .build();
    }

    @Override
    public Menu getMenu() {
        return new FollowPathCustomizer(this);
    }

    @Override
    public Action clone() {
        return new FollowPresetPath(new ArrayList<>(path), loop, getDelay(), getSelector(),
                new ArrayList<>(getConditions()), getCooldown());
    }

    @Override
    public StructCodec<? extends Action> getCodec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return "FollowPresetPath";
    }

    private class FollowPathCustomizer implements Menu {
        private final FollowPresetPath action;

        public FollowPathCustomizer(FollowPresetPath action) {
            this.action = action;
        }

        @Override
        public String getName() {
            return "follow_path_customizer";
        }

        @Override
        public @NotNull MenuTitle getTitle(DataRegistry dataRegistry, Player player) {
            return MenuTitles.createModern(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.title"));
        }

        @Override
        public @NotNull Capacity getCapacity(DataRegistry dataRegistry, Player player) {
            return Capacity.ofRows(5);
        }

        @Override
        public @NotNull Content getContent(DataRegistry dataRegistry, Player player, Capacity capacity) {
            return MenuUtils.actionBase(action, player)
                    .setButton(20, button(player))
                    .setButton(24, candle(player))
                    .build();
        }

        private Button candle(Player player) {
            return Button.clickable(ItemBuilder.modern(action.loop ? Material.GREEN_CANDLE : Material.RED_CANDLE)
                    .setDisplay(Msg.translate(player.locale(), action.loop ? "customnpcs.menus.action.follow_path" +
                                                                             ".loop.true" : "customnpcs.menus.action" +
                                                                                            ".follow_path.loop.false"))
                    .setLore(Msg.lore(player.locale(), action.loop ? "customnpcs.menus.action.follow_path.loop.true" +
                                                                     ".description" : "customnpcs.menus.action" +
                                                                                      ".follow_path.loop.false" +
                                                                                      ".description"))
                    .build(), ButtonClickAction.plain((m, e) -> {
                        e.setCancelled(true);
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                        action.loop = !action.loop;
                        m.updateButton(24, button -> button.setItem(candle(player).getItem()));
                        //todo: autostart
                    }
            ));
        }

        private Button button(Player player) {
            if (action.path == null || action.path.isEmpty()) {
                return Button.clickable(ItemBuilder.modern(Material.PLAYER_HEAD)
                        .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.record"))
                        .setLore(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.record.lore"))
                        .build(), ButtonClickAction.plain((menuView, event) -> {
                    player.closeInventory();
                    startRecording(player);
                }));
            }

            return Button.clickable(ItemBuilder.modern(Material.PLAYER_HEAD)
                    .setDisplay(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.rerecord"))
                    .setLore(Msg.translate(player.locale(), "customnpcs.menus.action.follow_path.rerecord.lore",
                            path.size()))
                    .build(), ButtonClickAction.plain((menuView, event) -> {
                player.closeInventory();
                startRecording(player);
            }));
        }
    }
}
