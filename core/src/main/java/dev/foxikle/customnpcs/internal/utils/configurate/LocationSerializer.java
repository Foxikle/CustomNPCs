package dev.foxikle.customnpcs.internal.utils.configurate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class LocationSerializer implements TypeSerializer<Location> {
    @Override
    public Location deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        String world = node.node("world").getString();
        double x = node.node("x").getDouble();
        double y = node.node("y").getDouble();
        double z = node.node("z").getDouble();
        float pitch = node.node("pitch").getFloat();
        float yaw = node.node("yaw").getFloat();

        return new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
    }


    @Override
    public void serialize(@NotNull Type type, @Nullable Location location, @NotNull ConfigurationNode node) throws SerializationException {
        if (location == null) throw new SerializationException("Cannot serialize a null location!");
        node.node("world").set(location.getWorld().getName());
        node.node("x").set(location.getX());
        node.node("y").set(location.getY());
        node.node("z").set(location.getZ());
        node.node("pitch").set(location.getPitch());
        node.node("yaw").set(location.getYaw());
    }
}
