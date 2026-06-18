/*
 * Copyright (c) 2024-2026. Foxikle
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

package dev.foxikle.customnpcs.data;

import dev.foxikle.customnpcs.internal.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The class representing the NPC's items
 */
@Getter
@Setter
@SuppressWarnings("UnusedReturnValue")
@AllArgsConstructor
public class Equipment {

    public static final Codec<Equipment> CODEC = StructCodec.struct(
            "head", Utils.ITEM_CODEC.optional(), Equipment::getHead,
            "chest", Utils.ITEM_CODEC.optional(), Equipment::getChest,
            "legs", Utils.ITEM_CODEC.optional(), Equipment::getLegs,
            "boots", Utils.ITEM_CODEC.optional(), Equipment::getBoots,
            "hand", Utils.ITEM_CODEC.optional(), Equipment::getHand,
            "offhand", Utils.ITEM_CODEC.optional(), Equipment::getOffhand,
            Equipment::new
    );

    @Nullable
    private ItemStack head = null;
    @Nullable
    private ItemStack chest = null;
    @Nullable
    private ItemStack legs = null;
    @Nullable
    private ItemStack boots = null;
    @Nullable
    private ItemStack hand = null;
    @Nullable
    private ItemStack offhand = null;

    /**
     * A constructor to create an equipment object with air as all items
     */
    public Equipment() {
        // default constructor
    }

    /**
     * Imports the relevant items from an EntityEquipment Object
     *
     * @param e The entity equipment to pull items from.
     */
    public Equipment importFromEntityEquipment(EntityEquipment e) {
        this.head = e.getHelmet() == null || e.getHelmet().isEmpty() ? null : e.getHelmet().clone();
        this.chest = e.getChestplate() == null || e.getChestplate().isEmpty() ? null : e.getChestplate().clone();
        this.legs = e.getLeggings() == null || e.getLeggings().isEmpty() ? null : e.getLeggings().clone();
        this.boots = e.getBoots() == null || e.getBoots().isEmpty() ? null : e.getBoots().clone();
        this.hand = e.getItemInMainHand().isEmpty() ? null : e.getItemInMainHand().clone();
        this.offhand = e.getItemInOffHand().isEmpty() ? null : e.getItemInOffHand().clone();
        return this;
    }

    @SuppressWarnings("all")
    public Equipment clone() {
        return new Equipment(
                head != null ? head.clone() : null,
                chest != null ? chest.clone() : null,
                legs != null ? legs.clone() : null,
                boots != null ? boots.clone() : null,
                hand != null ? hand.clone() : null,
                offhand != null ? offhand.clone() : null
        );
    }

    /**
     * Sets the items on the NPC's head
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setHead(@Nullable ItemStack itemStack) {
        head = itemStack;
        return this;
    }

    /**
     * Sets the items on the NPC's chest
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setChest(@Nullable ItemStack itemStack) {
        chest = itemStack;
        return this;
    }

    /**
     * Sets the items on the NPC's legs
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setLegs(@Nullable ItemStack itemStack) {
        legs = itemStack;
        return this;
    }

    /**
     * Sets the items on the NPC's feet
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setBoots(@Nullable ItemStack itemStack) {
        boots = itemStack;
        return this;
    }

    /**
     * Sets the items on the NPC's offhand
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setOffhand(@Nullable ItemStack itemStack) {
        offhand = itemStack;
        return this;
    }

    /**
     * Sets the items on the NPC's main hand
     *
     * @param itemStack the item to use
     * @return this, for chaining
     */
    public Equipment setHand(@Nullable ItemStack itemStack) {
        hand = itemStack;
        return this;
    }
}
