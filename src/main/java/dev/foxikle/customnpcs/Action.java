package dev.foxikle.customnpcs;

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

    /**
     * <p> Creates a new Action
     * </p>
     * @param args The arguments for the Action
     * @param actionType The type of action to be performed
     * @param delay The amount of ticks to delay an action
     */
    public Action(ActionType actionType, ArrayList<String> args, int delay){
        this.subCommand = actionType.name();
        this.args = args;
        this.delay = delay;
    }

    private Action(String subCommand, ArrayList<String> args, int delay){
        this.subCommand = subCommand;
        this.args = args;
        this.delay = delay;
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
     * Gets the command that is run to initiate the action.
     * @param player the action is targeted at
     * @return String representing the command. The arguments are: player's uuid, sub command, delay (in ticks), command specific arguments
     */
    public String getCommand(@NotNull Player player){
        return "npcaction " + player.getUniqueId() + " " + subCommand + " " + delay + " " + String.join(" ", args);
    }

    /**
     * <p> Gets the action from a serialized string
     * </p>
     * @param string The string to deserialize.
     * @return the action that was serialized.
     * @throws NumberFormatException If the string was formatted improperly
     * @throws ArrayIndexOutOfBoundsException if the action was formatted impropperly
     */
    public static Action of(String string) throws NumberFormatException, ArrayIndexOutOfBoundsException{
        ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
        String sub = split.get(0);
        split.remove(0);
        int delay = Integer.parseInt(split.get(0));
        split.remove(0);
        return new Action(sub, split, delay);
    }

    /**
     * <p> Gets the serialezed version of this action
     * </p>
     * @return the serialized version of the action
     */
    public String serialize(){
        return subCommand + "%::%" + delay + "%::%" + String.join("%::%", args);
    }
}
