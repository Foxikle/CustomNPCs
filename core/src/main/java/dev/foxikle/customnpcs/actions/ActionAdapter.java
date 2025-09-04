/*
 * Copyright (c) 2024-2025. Foxikle
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

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.conditions.ConditionalTypeAdapter;
import dev.foxikle.customnpcs.conditions.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A TypeAdapter for Gson
 */
public class ActionAdapter extends TypeAdapter<LegacyAction> {

    /**
     * Serializes the Action object to json
     * @param out The json writer
     * @param value the Java object to write. May be null.
     * @throws IOException if an IOException occurs
     */
    @Override
    public void write(JsonWriter out, LegacyAction value) throws IOException {
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
        for (Condition c : value.getConditionals()) {
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
     * @throws IOException if an error occurred
     */
    @Override
    public LegacyAction read(JsonReader in) throws IOException {
        in.beginObject();
        ActionType actionType = null;
        List<String> args = new ArrayList<>();
        int delay = 0;
        List<Condition> conditions = new ArrayList<>();
        Selector selectionMode = null;


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
                case "mode" -> selectionMode = Selector.valueOf(in.nextString());
                case "conditionals" -> {
                    in.beginArray();
                    while(in.hasNext()) {
                        try {
                            // if its stored as a string
                            conditions.add(Condition.of(in.nextString()));
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

        return new LegacyAction(actionType, args, delay, selectionMode, conditions);
    }
}
