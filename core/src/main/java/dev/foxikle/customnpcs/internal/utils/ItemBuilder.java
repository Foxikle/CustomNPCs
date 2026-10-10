package dev.foxikle.customnpcs.internal.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ItemBuilder {
    private final ItemStack item;

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material, 1);
    }

    public static ItemBuilder of(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public ItemBuilder(ItemStack stack) {
        this.item = stack;
    }


    public ItemBuilder(Material material, int amount) {
        Objects.requireNonNull(material, "material");

        if (!material.isItem()) {
            throw new IllegalArgumentException(material + " is not an item material");
        }
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }

        this.item = new ItemStack(material, amount);
    }

    public ItemBuilder amount(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder displayName(Component name) {
        return editMeta(meta -> meta.displayName(name));
    }

    public ItemBuilder lore(Component... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(List<Component> lines) {
        List<Component> copy = new ArrayList<>(lines);
        return editMeta(meta -> meta.lore(copy));
    }

    public ItemBuilder addLore(Component... lines) {
        return editMeta(meta -> {
            List<Component> lore = meta.lore() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(meta.lore());

            lore.addAll(Arrays.asList(lines));
            meta.lore(lore);
        });
    }

    public ItemBuilder clearLore() {
        return editMeta(meta -> meta.lore(null));
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        return editMeta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        return editMeta(meta -> meta.removeItemFlags(flags));
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> editor) {
        Objects.requireNonNull(editor, "editor");

        ItemMeta meta = item.getItemMeta();
        editor.accept(meta);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder glowing(@Nullable boolean state) {
        return editMeta(m -> m.setEnchantmentGlintOverride(state));
    }

    /** Returns a copy, so later builder changes won't affect this result. */
    public ItemStack build() {
        return item.clone();
    }

    public <M extends ItemMeta> ItemBuilder editMeta(
            Class<M> metaType,
            Consumer<M> editor
    ) {
        Objects.requireNonNull(metaType, "metaType");
        Objects.requireNonNull(editor, "editor");

        ItemMeta meta = item.getItemMeta();
        if (!metaType.isInstance(meta)) {
            throw new IllegalArgumentException(
                    "Expected " + metaType.getSimpleName()
                            + " but item has " + meta.getClass().getSimpleName()
            );
        }

        editor.accept(metaType.cast(meta));
        item.setItemMeta(meta);
        return this;
    }
}
