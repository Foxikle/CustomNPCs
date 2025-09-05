package dev.foxikle.customnpcs.internal.utils.configurate;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.conditions.Condition;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ConditionSerializer implements TypeSerializer<Condition> {
    @Override
    public Condition deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String raw = node.getString();
        if (raw == null) throw new SerializationException("Cannot deserialize a null action!");
        return Condition.of(raw);
    }


    @Override
    public void serialize(@NotNull Type type, @Nullable Condition obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) throw new SerializationException("Cannot serialize a null action!");
        node.set(CustomNPCs.getGson().toJson(obj));
    }
}
