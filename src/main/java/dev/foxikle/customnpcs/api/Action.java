package dev.foxikle.customnpcs.api;

import dev.foxikle.customnpcs.api.conditions.Conditional;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The object to represent what should be done after an NPC interaction
 */
public class Action {

    private String subCommand;
    private ArrayList<String> args;
    private int delay;

    private Conditional.SelectionMode mode;

    private final List<String> conditionals;

    /**
     * <p> Creates a new Action
     * </p>
     *
     * @param actionType The type of action to be performed
     * @param args       The arguments for the Action
     * @param delay      The amount of ticks to delay an action
     * @param matchAll If all the conditions must be met, or one
     * @param conditionals The conditions to apply to this action
     */
    public Action(ActionType actionType, ArrayList<String> args, int delay, Conditional.SelectionMode matchAll, List<String> conditionals){
        this.subCommand = actionType.name();
        this.args = args;
        this.delay = delay;
        this.mode = matchAll;
        this.conditionals = conditionals;
    }

    private Action(String subCommand, ArrayList<String> args, int delay){
        this.subCommand = subCommand;
        this.args = args;
        this.delay = delay;
        this.mode = Conditional.SelectionMode.ONE;
        this.conditionals = new ArrayList<>();
    }

    /**
     * <p> Gets a copy of the arguments of an action
     * </p>
     * @return A copy of the list of arguments for the actions
     */
    public List<String> getArgsCopy() {
        if(!args.isEmpty())
            return new ArrayList<>(args);
        else
            return new ArrayList<>();
    }

    /**
     * <p> Gets the arguments of an action
     * </p>
     * @return A list of arguments for the actions
     */
    public ArrayList<String> getArgs() {
        return args;
    }

    /**
     * <p> Gets the strinified action type
     * </p>
     * @return the action type as a string
     */
    public String getSubCommand() {
        return subCommand;
    }

    /**
     * <p> Gets the action type of this action
     * </p>
     * @return the action type of this action
     */
    public ActionType getActionType(){
        return ActionType.valueOf(subCommand);
    }

    /**
     * <p> Gets the delay of an action
     * </p>
     * @return the amount of ticks an action is delayed after interacting with an NPC
     */
    public int getDelay(){
        return delay;
    }

    /**
     * <p> sets the delay of an action (in ticks)
     * </p>
     * @param delay The amount of ticks to delay the action
     */
    public void setDelay(int delay){
        this.delay = delay;
    }

    /**
     * If all conditions must be met to execute the action
     * @return if all condition must be met
     */
    public Conditional.SelectionMode getMode() {
        return mode;
    }

    /**
     * Gets the list of conditions for the actio
     * @return the list of condtitions
     */
    public List<Conditional> getConditionals() {
        List<Conditional> tmp = new ArrayList<>();
        conditionals.forEach(s -> tmp.add(Conditional.of(s)));
        return tmp;
    }

    /**
     * Sets if all conditions should be met
     * @param mode if all conditions should be met
     */
    public void setMode(Conditional.SelectionMode mode) {
        this.mode = mode;
    }

    /**
     *  Adds a condition to the action
     * @param conditional the conditional to add
     * @return if the conditional was successfully added
     */
    public boolean addConditional(Conditional conditional) {
        return conditionals.add(conditional.toJson());
    }

    /**
     * Removes a condition from the action
     * @param conditional conditional to remove
     * @return if the condition was successfully removed
     */
    public boolean removeConditional(Conditional conditional) {
        return conditionals.remove(conditional.toJson());
    }

    /**
     * Gets the command that is run to initiate the action.
     * @param player the action is targeted at
     * @return String representing the command. The arguments are: player's uuid, sub command, delay (in ticks), command specific arguments
     */
    public String getCommand(@NotNull Player player) {
        if(processConditions(player)) {
            return "npcaction " + player.getUniqueId() + " " + subCommand + " " + delay + " " + String.join(" ", args);
        } else {
            return "npcaction";
        }
    }

    private boolean processConditions(Player player) {
        if(conditionals == null || conditionals.isEmpty())
            return true; // no conditions
        List<Boolean> computedActions = new ArrayList<>();
        conditionals.forEach(conditional -> computedActions.add(Conditional.of(conditional).compute(player)));
        if(mode == Conditional.SelectionMode.ALL) {
            return !computedActions.contains(false); // not all true
        } else {
            return computedActions.contains(true); // if any are true
        }
    }

    /**
     * <p> Gets the action from a serialized string
     * </p>
     * @param string The string to deserialize.
     * @return the action that was serialized.
     * @throws NumberFormatException If the string was formatted improperly
     * @throws ArrayIndexOutOfBoundsException if the action was formatted impropperly
     */
    public static Action of(String string) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        if(string.contains("%::%")) {
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
     * <p> Gets the json equivalent of this action
     * </p>
     * @return the serialized version of the action (in json)
     */
    public String toJson(){
        return CustomNPCs.getGson().toJson(this);
    }
}
