package dev.foxikle.customnpcs;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action {

    private String subCommand;
    private ArrayList<String> args;
    private int delay;

    public Action(String subCommand, ArrayList<String> args, int delay){
        this.subCommand = subCommand;
        this.args = args;
        this.delay = delay;
    }

    public List<String> getArgsCopy() {
        if(!args.isEmpty())
            return new ArrayList<>(args);
        else
            return new ArrayList<>();
    }

    public ArrayList<String> getArgs() {
        return args;
    }

    public String getSubCommand() {
        return subCommand;
    }

    public int getDelay(){
        return delay;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }

    public String getCommand(Player player){
        return "npcaction " + player.getUniqueId() + " " + subCommand + " " + delay + " " + String.join(" ", args);
    }

    public static Action of(String string){
        ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
        String sub = split.get(0);
        split.remove(0);
        int delay = Integer.parseInt(split.get(0));
        split.remove(0);
        return new Action(sub, split, delay);
    }

    public String serialize(){
        return subCommand + "%::%" + delay + "%::%" + String.join("%::%", args);
    }
}
