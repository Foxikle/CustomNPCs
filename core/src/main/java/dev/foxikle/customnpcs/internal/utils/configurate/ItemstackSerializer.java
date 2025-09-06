package dev.foxikle.customnpcs.internal.utils.configurate;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ItemstackSerializer implements TypeSerializer<ItemStack> {
    @Override
    @SuppressWarnings("unchecked")
    public ItemStack deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        byte[] data = node.get(byte[].class);
        assert data != null;
        if (data.length == 0) {
            return new ItemStack(Material.AIR);
        }
        return ItemStack.deserializeBytes(data);
    }


    @Override
    public void serialize(@NotNull Type type, @Nullable ItemStack obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) throw new SerializationException("Cannot serialize a null location!");
        if (obj.getType() == Material.AIR) {
            node.set(new byte[0]);
        } else node.set(obj.serializeAsBytes());
    }
}
