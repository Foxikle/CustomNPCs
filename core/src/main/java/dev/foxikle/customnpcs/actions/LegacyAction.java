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
import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The object to represent what should be done after an NPC interaction
 *
 * @deprecated
 */
@Getter
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.9.0")
public class LegacyAction {


    private final ActionType actionType;
    private final List<String> args;
    private final List<Condition> conditionals;
    @Setter
    private int delay;
    @Setter
    private Condition.SelectionMode mode;

    /**
     * <p> Creates a new Action
     * </p>
     *
     * @param actionType   The type of action to be performed
     * @param args         The arguments for the Action
     * @param delay        The amount of ticks to delay an action
     * @param matchAll     If all the conditions must be met, or one
     * @param conditionals The conditions to apply to this action
     */
    public LegacyAction(ActionType actionType, List<String> args, int delay, Condition.SelectionMode matchAll, List<Condition> conditionals) {
        this.actionType = actionType;
        this.args = args;
        this.delay = delay;
        this.mode = matchAll;
        this.conditionals = conditionals;
    }

    private LegacyAction(String subCommand, ArrayList<String> args, int delay) {
        this.actionType = ActionType.valueOf(subCommand);
        this.args = args;
        this.delay = delay;
        this.mode = Condition.SelectionMode.ONE;
        this.conditionals = new ArrayList<>();
    }

    /**
     * <p> Gets the action from a serialized string
     * </p>
     *
     * @param string The string to deserialize.
     * @return the action that was serialized.
     * @throws NumberFormatException          If the string was formatted improperly
     * @throws ArrayIndexOutOfBoundsException if the action was formatted improperly
     */
    public static LegacyAction of(String string) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        if (string.contains("%::%")) {
            ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
            String sub = split.get(0);
            split.remove(0);
            int delay = Integer.parseInt(split.get(0));
            split.remove(0);
            return new LegacyAction(sub, split, delay); // doesn't support conditionals
        } else {
            return CustomNPCs.getGson().fromJson(string, LegacyAction.class);
        }
    }

    /**
     * <p> Gets a copy of the arguments of an action
     * </p>
     *
     * @return A copy of the list of arguments for the actions
     */
    public List<String> getArgsCopy() {
        return new ArrayList<>(args);
    }

    /**
     * Adds a condition to the action
     *
     * @param conditional the conditional to add
     * @return if the conditional was successfully added
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean addConditional(Condition conditional) {
        return conditionals.add(conditional);
    }

    /**
     * Removes a condition from the action
     *
     * @param conditional conditional to remove
     * @return if the condition was successfully removed
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean removeConditional(Condition conditional) {
        return conditionals.remove(conditional);
    }

    /**
     * Gets the command that is run to initiate the action.
     *
     * @param player the action is targeted at
     * @return String representing the command. The arguments are: player's uuid, sub command, delay (in ticks), command specific arguments
     */
    public String getCommand(@NotNull Player player) {
        if (processConditions(player)) {
            return "npcaction " + player.getUniqueId() + " " + actionType.name() + " " + delay + " " + String.join(" ", args);
        } else {
            return "npcaction";
        }
    }

    private boolean processConditions(Player player) {
        if (conditionals == null || conditionals.isEmpty()) return true; // no conditions

        Set<Boolean> results = new HashSet<>(2);
        conditionals.forEach(conditional -> results.add(conditional.compute(player)));
        return (mode == Condition.SelectionMode.ALL ? !results.contains(false) : results.contains(true));
    }

    /**
     * <p> Gets the json equivalent of this action
     * </p>
     *
     * @return the serialized version of the action (in json)
     */
    public String toJson() {
        return CustomNPCs.getGson().toJson(this);
    }

    @Override
    @SuppressWarnings("all")
    public LegacyAction clone() {
        return new LegacyAction(actionType, args, delay, mode, conditionals);
    }


    /**
     * A method that converts legacy actions to the new system
     *
     * @return
     */
    @Nullable
    public Action toAction() {
        return switch (actionType) {
            case ACTION_BAR -> new ActionBar(String.join(" ", args), delay, mode, conditionals, 0);
            case SEND_MESSAGE -> new SendMessage(String.join(" ", args), delay, mode, conditionals, 0);
            case DISPLAY_TITLE ->
                    new DisplayTitle(String.join(" ", args.subList(3, args.size() - 1)), "", Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)), delay, mode, conditionals, 0);
            case RUN_COMMAND -> new RunCommand(String.join(" ", args), false, delay, mode, conditionals, 0);
            case TELEPORT ->
                    new Teleport(Double.parseDouble(args.get(0)), Double.parseDouble(args.get(1)), Double.parseDouble(args.get(2)), Float.parseFloat(args.get(3)), Float.parseFloat(args.get(4)), delay, mode, conditionals, 0);
            case GIVE_EXP ->
                    new GiveXP(Integer.parseInt(args.get(0)), Boolean.parseBoolean(args.get(1)), delay, mode, conditionals, 0);
            case ADD_EFFECT ->
                    new GiveEffect(args.get(3), Integer.parseInt(args.get(0)), Integer.parseInt(args.get(1)), Boolean.parseBoolean(args.get(2)), delay, mode, conditionals, 0);
            case PLAY_SOUND ->
                    new PlaySound(args.get(2), Float.parseFloat(args.get(1)), Float.parseFloat(args.get(0)), delay, mode, conditionals, 0);
            case REMOVE_EXP ->
                    new RemoveXP(Integer.parseInt(args.get(0)), Boolean.parseBoolean(args.get(1)), delay, mode, conditionals, 0);
            case REMOVE_EFFECT -> new RemoveEffect(args.get(0), delay, mode, conditionals, 0);
            case SEND_TO_SERVER -> new SendServer(String.join(" ", args), delay, mode, conditionals, 0);
            case TOGGLE_FOLLOWING -> throw new IllegalArgumentException("Toggle following is no longer supported");
        };
    }
}
