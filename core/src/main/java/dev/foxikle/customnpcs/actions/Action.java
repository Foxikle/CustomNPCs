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

package dev.foxikle.customnpcs.actions;

import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.Selector;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Utils;
import io.github.mqzen.menus.base.Menu;
import io.github.mqzen.menus.misc.button.Button;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public abstract class Action {

    @Deprecated(forRemoval = true)
    private static final Pattern SPLITTER = Pattern.compile("^([A-z])*(?=(\\{.*}))");


    public static final Codec<Action> CODEC = Codec.STRING.unionType("id", s -> {
        Class<? extends Action> clazz = CustomNPCs.ACTION_REGISTRY.getActionClass(s);
        if (clazz == null) throw new IllegalArgumentException("Invalid action type: " + s);
        try {
            return clazz.newInstance().getCodec();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to parse action: ", e);
        }
    }, Action::getId);

    private List<Condition> conditions;
    private int delay;
    private Selector selector;
    private int cooldown;
    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    public Action(int delay, Selector selector, List<Condition> conditions, int cooldown) {
        this.delay = delay;
        this.selector = selector;
        this.conditions = conditions;
        this.cooldown = cooldown;
    }

    public boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return false;
        Instant i = cooldowns.get(uuid);
        if (i.isBefore(Instant.now())) {
            cooldowns.remove(uuid);
            return false;
        }
        return true;
    }

    public void activateCooldown(UUID uuid) {
        if (cooldown == 0) return;
        cooldowns.put(uuid, Instant.now().plusMillis(50L * cooldown));
    }

    /**
     * A convenience method to add a condition to the action
     *
     * @param condition the condition to add
     */
    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public void removeCondition(Condition condition) {
        conditions.remove(condition);
    }

    /**
     * Contains the execution of the action
     *
     * @param npc    The NPC
     * @param menu   The menu
     * @param player The player
     */
    public abstract void perform(InternalNpc npc, Menu menu, Player player);


    /**
     * The item used as an icon in the NPC's action menu
     *
     * @param player the player who will see them
     * @return the ItemStack to use
     */
    public abstract ItemStack getFavicon(Player player);

    public abstract Menu getMenu();

    /**
     * Returns if the action should be processed. This takes the cooldown into account.
     *
     * @param player the player
     * @return if the action should be processed
     */
    public boolean processConditions(Player player) {
        if (isOnCooldown(player.getUniqueId())) return false;
        if (conditions == null || conditions.isEmpty()) return true; // no conditions
        Set<Boolean> results = new HashSet<>(conditions.size());
        conditions.forEach(conditional -> results.add(conditional.compute(player)));
        return (selector == Selector.ALL ? !results.contains(false) : results.contains(true));
    }


    /**
     * Provides a codec for the union type serializer.
     * You should include these elements in your implementation:
     * <pre>
     *     {@code
     *             "delay", Codec.INT, Action::getDelay,
     *             "selector", Codec.Enum(Selector.class), Action::getSelector,
     *             "conditions", Condition.CODEC.list(), Action::getConditions,
     *             "cooldown", Codec.INT, Action::getCooldown,
     *     }
     * </pre>
     * <p>
     * As a consequence of this, your action must have a public, no-args constructor for the codec to be accesible.
     * </p>
     *
     * @return the codec delegated to your action
     * @see <a href="https://mudstom.pages.dev/docs/feature/serialization/codecs">Codec Documentation</a>
     */
    public abstract StructCodec<? extends Action> getCodec();

    public abstract String getId();

    public abstract Button creationButton(Player player);

    public abstract Action clone();

    public boolean canEdit() {
        return true;
    }

    public boolean canDelay() {
        return true;
    }

    public boolean canDuplicate() {
        return true;
    }

    /**
     * @throws NoSuchMethodException if your custom Action classes don't implement a `deserialize` method.
     */
    @SneakyThrows
    @Nullable
    @Deprecated(forRemoval = true)
    public static Action parse(@NotNull String s) {
        Matcher matcher = SPLITTER.matcher(s);

        if (matcher.find()) {
            String type = matcher.group();

            Class<? extends Action> clazz = CustomNPCs.ACTION_REGISTRY.getActionClass(type);
            if (clazz == null) {
                CustomNPCs.getInstance().getLogger().severe("Unknown action class " + type);
                return null; // class doesn't exist in the registry
            }

            Class<?>[] parameterTypes = new Class<?>[]{String.class, Class.class};
            Method method = clazz.getMethod("deserialize", parameterTypes);
            Object result = method.invoke(null, s, clazz);
            return (Action) result;
        } else {
            return null;
        }
    }

    @Deprecated(forRemoval = true)
    protected static List<Condition> deserializeConditions(String json) {
        String data = parseArray(json, "conditions").replace("},]", "}]");
        return CustomNPCs.getGson().fromJson(data, Utils.CONDITIONS_LIST);
    }

    /**
     * Parses the base data required for every action. (SelectionMode, delay, and conditions)
     *
     * @param data The serialized action string
     * @return The parsed "base" data, required for every action.
     */
    @Deprecated(forRemoval = true)
    protected static ParseResult parseBase(String data) {
        int cooldown = parseInt(data, "cooldown");
        int delay = parseInt(data, "delay");
        Selector mode = parseEnum(data, "mode", Selector.class);
        List<Condition> conditions = deserializeConditions(parseString(data, "conditions"));
        return new ParseResult(delay, mode, conditions, cooldown);
    }

    /**
     * Parses an enum constant from the serialized action string
     *
     * @param data the raw, serialized data
     * @param key  The key supplied during the serialization
     * @param type the type of the Enum to parse
     * @param <T>  the enum type
     * @return The parsed enum constant, by name
     */
    @Deprecated(forRemoval = true)
    protected static <T extends Enum<T>> T parseEnum(String data, String key, Class<T> type) {
        String constantName = data.replaceAll(".*" + key + "=([A-Z_]+).*", "$1");
        return Enum.valueOf(type, constantName);
    }

    /**
     * Parses an integer from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `delay`
     * @return The parsed integer. 0, if the key is not found.
     */
    @Deprecated(forRemoval = true)
    protected static int parseInt(String data, String key) {
        try {
            return Integer.parseInt(data.replaceAll(".*" + key + "=(\\d+).*", "$1"));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Parses a String from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `raw`
     * @return The parsed string.
     */
    @Deprecated(forRemoval = true)
    protected static String parseString(String data, String key) {
        return data.replaceAll(".*" + key + "=`(.*?)`.*", "$1");
    }

    /**
     * Parses a String from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `raw`
     * @return The parsed string.
     */
    @Deprecated(forRemoval = true)
    protected static String parseArray(String data, String key) {
        return data.replaceAll(".*" + key + "=(\\[.*?]).*", "$1");
    }

    /**
     * Parses a boolean from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `asConsole`
     * @return The parsed boolean.
     */
    @Deprecated(forRemoval = true)
    protected static boolean parseBoolean(String data, String key) {
        return Boolean.parseBoolean(data.replaceAll(".*" + key + "=(true|false).*", "$1"));
    }

    /**
     * Parses a float from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `yaw`
     * @return The parsed float.
     */
    @Deprecated(forRemoval = true)
    protected static float parseFloat(String data, String key) {
        return Float.parseFloat(data.replaceAll(".*" + key + "=(-?\\d+\\.\\d+).*", "$1"));
    }

    /**
     * Parses a double from the serialized action String
     *
     * @param data The serialized action string
     * @param key  the key to search for, ie `x`
     * @return The parsed double.
     */
    @Deprecated(forRemoval = true)
    protected static double parseDouble(String data, String key) {
        return Double.parseDouble(data.replaceAll(".*" + key + "=(-?\\d+\\.\\d+).*", "$1"));
    }

    @Deprecated
    protected record ParseResult(int delay, Selector mode, List<Condition> conditions, int cooldown) {

    }
}
