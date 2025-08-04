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

package dev.foxikle.customnpcs.actions.conditions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.foxikle.customnpcs.actions.conditions.Condition.Comparator;
import dev.foxikle.customnpcs.actions.conditions.Condition.Type;
import dev.foxikle.customnpcs.actions.conditions.Condition.Value;

import java.io.IOException;

/**
 * The object allowing Gson to parse conditional objects
 */
public class ConditionalTypeAdapter extends TypeAdapter<Condition> {

    /**
     *
     * @param out the data write out to
     * @param conditional the Java object to write. May be null.
     * @throws IOException if an exception occurs
     */
    @Override
    public void write(JsonWriter out, Condition conditional) throws IOException {
        out.beginObject();
        out.name("type").value(conditional.getType().toString());
        out.name("value").value(conditional.getValue().toString());
        out.name("comparator").value(conditional.getComparator().toString());
        out.name("target").value(conditional.getTarget());
        out.endObject();
    }

    /**
     *
     * @param in the data to parse
     * @return the deserialized Conditional
     * @throws IOException if an error occurs reading
     */
    @Override
    public Condition read(JsonReader in) throws IOException {
        in.beginObject();
        Type type = null;
        Value value = null;
        Comparator comparator = null;
        String targetValue = null;

        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "type" -> type = Type.valueOf(in.nextString());
                case "value" -> value = Value.valueOf(in.nextString());
                case "comparator" -> comparator = Comparator.valueOf(in.nextString());
                case "target" -> targetValue = in.nextString();
                default -> in.skipValue(); // Ignore unknown properties
            }
        }

        in.endObject();

        Condition conditional = null;

        if (type != null && value != null && comparator != null && targetValue != null) {
            if(type == Type.NUMERIC) {
                conditional = new NumericCondition(comparator, value, Double.parseDouble(targetValue));
            } else if (type == Type.LOGICAL) {
                conditional = new LogicalCondition(comparator, value, targetValue);
            }
        }

        return conditional;
    }
}

