package dev.foxikle.customnpcs.internal.conditions;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.foxikle.customnpcs.internal.conditions.Conditional.Comparator;
import dev.foxikle.customnpcs.internal.conditions.Conditional.Type;
import dev.foxikle.customnpcs.internal.conditions.Conditional.Value;

import java.io.IOException;

public class ConditionalTypeAdapter extends TypeAdapter<Conditional> {

    @Override
    public void write(JsonWriter out, Conditional conditional) throws IOException {
        out.beginObject();
        out.name("type").value(conditional.getType().toString());
        out.name("value").value(conditional.getValue().toString());
        out.name("comparator").value(conditional.getComparator().toString());
        out.name("target").value(conditional.getTarget());
        out.endObject();
    }

    @Override
    public Conditional read(JsonReader in) throws IOException {
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

        Conditional conditional = null;

        if (type != null && value != null && comparator != null && targetValue != null) {
            if(type == Type.NUMERIC) {
                conditional = new NumericConditional(comparator, value, Double.parseDouble(targetValue));
            } else if (type == Type.LOGICAL) {
                conditional = new LogicalConditional(comparator, value, targetValue);
            }
        }

        return conditional;
    }
}

