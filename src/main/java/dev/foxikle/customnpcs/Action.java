package dev.foxikle.customnpcs;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Action {

    private String subCommand;
    private ArrayList<String> args;

    public Action(String subCommand, ArrayList<String> args){
        this.subCommand = subCommand;
        this.args = args;
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

    public String getCommand(Player player){
        return "npcaction " + player.getUniqueId() + " " + subCommand + " " + String.join(" ", args);
    }

    public static Action of(String string){
        ArrayList<String> split = new ArrayList<>(Arrays.stream(string.split("%::%")).toList());
        String sub = split.get(0);
        split.remove(0);
        return new Action(sub, split);
    }

    public String serialize(){
        return subCommand + "%::%" + String.join("%::%", args);
    }
}
