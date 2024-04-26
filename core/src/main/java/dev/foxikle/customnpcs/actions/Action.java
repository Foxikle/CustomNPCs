package dev.foxikle.customnpcs.actions;

import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The object to represent what should be done after an NPC interaction
 */
@Getter
public class Action {


    private final ActionType actionType;
    private final List<String> args;
    private final List<Conditional> conditionals;
    @Setter
    private int delay;
    @Setter
    private Conditional.SelectionMode mode;

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
    public Action(ActionType actionType, List<String> args, int delay, Conditional.SelectionMode matchAll, List<Conditional> conditionals) {
        this.actionType = actionType;
        this.args = args;
        this.delay = delay;
        this.mode = matchAll;
        this.conditionals = conditionals;
    }

    private Action(String subCommand, ArrayList<String> args, int delay) {
        this.actionType = ActionType.valueOf(subCommand);
        this.args = args;
        this.delay = delay;
        this.mode = Conditional.SelectionMode.ONE;
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
    public static Action of(String string) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        if (string.contains("%::%")) {
            ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
            String sub = split.get(0);
            split.remove(0);
            int delay = Integer.parseInt(split.get(0));
            split.remove(0);
            return new Action(sub, split, delay); // doesn't support conditionals
        } else {
            return CustomNPCs.getGson().fromJson(string, Action.class);
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
    public boolean addConditional(Conditional conditional) {
        return conditionals.add(conditional);
    }

    /**
     * Removes a condition from the action
     *
     * @param conditional conditional to remove
     * @return if the condition was successfully removed
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean removeConditional(Conditional conditional) {
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
        return (mode == Conditional.SelectionMode.ALL ? !results.contains(false) : results.contains(true));
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
    public Action clone() {
        return new Action(actionType, args, delay, mode, conditionals);
    }
}
