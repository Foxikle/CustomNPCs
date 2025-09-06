package dev.foxikle.customnpcs.internal.utils.configurate;

import io.leangen.geantyref.TypeToken;
import org.bukkit.Color;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Map;

public class ColorSerializer implements TypeSerializer<Color> {
    @Override
    public Color deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        return Color.fromARGB(node.getInt());
    }


    @Override
    public void serialize(@NotNull Type type, @Nullable Color obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) throw new SerializationException("Cannot serialize a null location!");
        node.set(obj.asARGB());
    }
}
