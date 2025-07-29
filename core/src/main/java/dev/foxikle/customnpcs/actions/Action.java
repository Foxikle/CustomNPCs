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

package dev.foxikle.customnpcs.actions;

import dev.foxikle.customnpcs.actions.conditions.Condition;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Utils;
import io.github.mqzen.menus.base.Menu;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
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
public abstract class Action {

    private static final Pattern SPLITTER = Pattern.compile("^([A-z])*(?=(\\{.*}))");

    private List<Condition> conditions = new ArrayList<>();
    private int delay = 0;
    private Condition.SelectionMode mode = Condition.SelectionMode.ONE;
    private int cooldown = 0;
    private Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public Action() {

    }

    public Action(int delay, Condition.SelectionMode mode, List<Condition> conditions, int cooldown) {
        this.delay = delay;
        this.mode = mode;
        this.conditions = conditions;
        this.cooldown = cooldown;
    }


    /**
     * Deprecated, use {@link Action#Action(int, Condition.SelectionMode, List, int)}  Action}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
    public Action(int delay, Condition.SelectionMode mode, List<Condition> conditions) {
        this(delay, mode, conditions, 0);
    }

    protected static List<Condition> deserializeConditions(String json) {
        String data = parseArray(json, "conditions").replace("},]", "}]");
        return CustomNPCs.getGson().fromJson(data, Utils.CONDITIONS_LIST);
    }

    /**
     * @throws NoSuchMethodException if your custom Action classes don't implement a `deserialize` method.
     */
    @SneakyThrows
    @Nullable
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

    /**
     * Parses the base data required for every action. (SelectionMode, delay, and conditions)
     *
     * @param data The serialized action string
     * @return The parsed "base" data, required for every action.
     */
    protected static ParseResult parseBase(String data) {
        int cooldown = parseInt(data, "cooldown");
        int delay = parseInt(data, "delay");
        Condition.SelectionMode mode = parseEnum(data, "mode", Condition.SelectionMode.class);
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
    protected static double parseDouble(String data, String key) {
        return Double.parseDouble(data.replaceAll(".*" + key + "=(-?\\d+\\.\\d+).*", "$1"));
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
     * Serializes the action to a string
     */
    public abstract String serialize();

    public abstract ItemStack getFavicon(Player player);

    @Override
    public String toString() {
        return serialize();
    }

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
        return (mode == Condition.SelectionMode.ALL ? !results.contains(false) : results.contains(true));
    }

    private String getConditionSerialized() {
        return CustomNPCs.getGson().toJson(conditions, Utils.CONDITIONS_LIST);
    }

    /**
     * Generates the serialized string for storing Actions.
     * <p>
     * This correctly serializes `int`, `double`, `float`, `boolean`, {@link String}, {@link Enum<>}
     *
     * @param id     The id of the action ID{params...}
     * @param params The parameters of the action. **DON'T INCLUDE THE DELAY, CONDITIONS, OR SELECTION MODE**
     * @return The serialized strings
     */
    protected String generateSerializedString(String id, Map<String, Object> params) {
        Map<String, Object> base = new HashMap<>(); // get around Map.of() immutability
        base.put("delay", delay);
        base.put("mode", mode);
        base.put("conditions", getConditionSerialized());
        base.put("cooldown", cooldown);

        StringBuilder builder = new StringBuilder(id);
        builder.append("{");

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            builder.append(key).append("=");
            if (value instanceof String) {
                builder.append("`").append(value).append("`");
            } else {
                builder.append(value);
            }
            builder.append(", ");
        }

        for (Map.Entry<String, Object> entry : base.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            builder.append(key).append("=");
            if (value instanceof String) {
                builder.append("`").append(value).append("`");
            } else {
                builder.append(value);
            }
            builder.append(", ");
        }

        builder.deleteCharAt(builder.length() - 1); // delete the last comma
        builder.deleteCharAt(builder.length() - 1); // delete the last comma


        builder.append("}");

        return builder.toString();
    }

    public abstract Action clone();

    protected record ParseResult(int delay, Condition.SelectionMode mode, List<Condition> conditions, int cooldown) {
        /**
         * @deprecated Use {@link ParseResult#ParseResult(int, Condition.SelectionMode, List, int)}}
         */
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "1.9")
        public ParseResult(int delay, Condition.SelectionMode mode, List<Condition> conditions) {
            this(delay, mode, conditions, 0);
        }
    }
}
