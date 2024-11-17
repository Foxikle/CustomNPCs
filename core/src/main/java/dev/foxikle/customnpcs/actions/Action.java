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
import dev.foxikle.customnpcs.actions.defaultImpl.*;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import io.github.mqzen.menus.base.Menu;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public abstract class Action {

    private static final Pattern SPLITTER = Pattern.compile("^([A-z])*(?=(\\{.*}))");

    private final List<Condition> conditions = new ArrayList<>();
    private int delay = 0;
    private Condition.SelectionMode mode = Condition.SelectionMode.ONE;


    /**
     * Default constructor
     */
    public Action() {
    }

    public Action(int delay, Condition.SelectionMode mode, List<Condition> conditionals) {
        this.delay = delay;
        this.mode = mode;
        this.conditions.addAll(conditionals);
    }

    protected static List<Condition> deserializeConditions(String json) {
        List<Condition> conditions = new ArrayList<>();
        if (!json.isEmpty()) {
            String[] conditionArray = json.split("},");
            for (String conditionStr : conditionArray) {
                conditionStr = conditionStr.endsWith("}") ? conditionStr : conditionStr + "}";
                Condition condition = Condition.of(conditionStr);
                conditions.add(condition);
            }
        }
        return conditions;
    }

    @Nullable
    public static Action parse(@NotNull String s) {
        Matcher matcher = SPLITTER.matcher(s);

        if (matcher.find()) {
            String type = matcher.group();
            return switch (type) {
                case "ActionBar" -> ActionBar.deserialize(s, ActionBar.class);
                case "DisplayTitle" -> DisplayTitle.deserialize(s, DisplayTitle.class);
                case "GiveEffect" -> GiveEffect.deserialize(s, GiveEffect.class);
                case "GiveXP" -> GiveXP.deserialize(s, GiveXP.class);
                case "PlaySound" -> PlaySound.deserialize(s, PlaySound.class);
                case "RemoveEffect" -> RemoveEffect.deserialize(s, RemoveEffect.class);
                case "RemoveXP" -> RemoveXP.deserialize(s, RemoveXP.class);
                case "RunCommand" -> RunCommand.deserialize(s, RunCommand.class);
                case "SendMessage" -> SendMessage.deserialize(s, SendMessage.class);
                case "SendServer" -> SendServer.deserialize(s, SendServer.class);
                case "Teleport" -> Teleport.deserialize(s, Teleport.class);
                default ->
                        throw new IllegalStateException("Unexpected value: '" + type + "'; Original String: '" + s + "'");
            };
        } else {
            return null;
        }
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
     * Returns if the action should be processed
     *
     * @param player the player
     * @return if the action should be processed
     */
    public boolean processConditions(Player player) {
        if (conditions == null || conditions.isEmpty()) return true; // no conditions

        Set<Boolean> results = new HashSet<>(conditions.size());
        conditions.forEach(conditional -> results.add(conditional.compute(player)));
        return (mode == Condition.SelectionMode.ALL ? !results.contains(false) : results.contains(true));
    }

    protected String getConditionSerialized() {
        StringBuilder conditions = new StringBuilder();
        conditions.append("[");
        for (Condition condition : getConditions()) {
            conditions.append(condition.toJson()).append(",");
        }

        if (conditions.length() > 1 && conditions.substring(conditions.length() - 2).equals(","))
            conditions = new StringBuilder(conditions.substring(0, conditions.length() - 1));
        conditions.append("]");
        return conditions.toString();
    }

    public abstract Action clone();
}
