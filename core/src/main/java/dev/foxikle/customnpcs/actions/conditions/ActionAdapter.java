package dev.foxikle.customnpcs.actions.conditions;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A TypeAdapter for Gson
 */
public class ActionAdapter extends TypeAdapter<Action> {

    /**
     * Serializes the Action object to json
     * @param out The json writer
     * @param value the Java object to write. May be null.
     * @throws IOException if an IOException occours
     */
    @Override
    public void write(JsonWriter out, Action value) throws IOException {
        out.beginObject();
        out.name("actionType").value(value.getActionType().toString());

        out.name("args");
        out.beginArray();
        for (String s : value.getArgs()) {
            out.value(s);
        }
        out.endArray();

        out.name("delay").value(value.getDelay());
        out.name("mode").value(value.getMode().name());

        out.name("conditionals");
        out.beginArray();
        for (Conditional c : value.getConditionals()) {
            out.value(c.toJson());
        }
        out.endArray();
        out.endObject();
        out.close();
    }


    /**
     * deserializes an Action
     * @param in the object, in reader form
     * @return the deserialized action
     * @throws IOException if an error occoured
     */
    @Override
    public Action read(JsonReader in) throws IOException {
        in.beginObject();
        ActionType actionType = null;
        List<String> args = new ArrayList<>();
        int delay = 0;
        List<Conditional> conditions = new ArrayList<>();
        Conditional.SelectionMode selectionMode = null;


        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "actionType", "subCommand" -> actionType = ActionType.valueOf(in.nextString());
                case "args" -> {
                    in.beginArray();
                    while(in.hasNext()) {
                        args.add(in.nextString());
                    }
                    in.endArray();
                }
                case "delay" -> delay = in.nextInt();
                case "mode" -> selectionMode = Conditional.SelectionMode.valueOf(in.nextString());
                case "conditionals" -> {
                    in.beginArray();
                    while(in.hasNext()) {
                        try {
                            // if its stored as a string
                            conditions.add(Conditional.of(in.nextString()));
                        } catch (JsonSyntaxException | IllegalStateException ignored) {
                            // or as an object
                            ConditionalTypeAdapter conditionalTypeAdapter = new ConditionalTypeAdapter();
                            conditions.add(conditionalTypeAdapter.read(in));
                        }
                    }
                    in.endArray();
                }
                default -> in.skipValue(); // Ignore unknown properties
            }
        }

        in.endObject();

        return new Action(actionType, args, delay, selectionMode, conditions);
    }
}
