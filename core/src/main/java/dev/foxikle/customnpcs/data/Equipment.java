/*
 * Copyright (c) 2024. Foxikle
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

import dev.foxikle.customnpcs.internal.utils.Msg;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * The class representing the NPC's items
 */
@Setter
@Getter
public class Equipment {
    private ItemStack head = new ItemStack(Material.AIR);
    private ItemStack chest = new ItemStack(Material.AIR);
    private ItemStack legs = new ItemStack(Material.AIR);
    private ItemStack boots = new ItemStack(Material.AIR);
    private ItemStack hand = new ItemStack(Material.AIR);
    private ItemStack offhand = new ItemStack(Material.AIR);


    /**
     * The constructor to create the equipment object
     *
     * @param head    The item on the NPC's head
     * @param chest   The item on the NPC's chest
     * @param legs    The item on the NPC's legs
     * @param boots   The item on the NPC's feet
     * @param hand    The item in the NPC's hand
     * @param offhand The item in the NPC's offhand
     */
    public Equipment(ItemStack head, ItemStack chest, ItemStack legs, ItemStack boots, ItemStack hand, ItemStack offhand) {
        this.head = head == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : head;
        this.chest = chest == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : chest;
        this.legs = legs == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : legs;
        this.boots = boots == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : boots;
        this.hand = hand == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : hand;
        this.offhand = offhand == null ? ItemBuilder.modern(Material.BEDROCK).setDisplay(Msg.format("<RED><B>ERROR!")).build() : offhand;
    }

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
    public void importFromEntityEquipment(EntityEquipment e) {
        this.head = e.getHelmet() != null ? e.getHelmet().clone() : new ItemStack(Material.AIR);
        this.chest = e.getChestplate() != null ? e.getChestplate().clone() : new ItemStack(Material.AIR);
        this.legs = e.getLeggings() != null ? e.getLeggings().clone() : new ItemStack(Material.AIR);
        this.boots = e.getBoots() != null ? e.getBoots().clone() : new ItemStack(Material.AIR);
        this.hand = e.getItemInMainHand().clone();
        this.offhand = e.getItemInOffHand().clone();
    }

    @SuppressWarnings("all")
    public Equipment clone() {
        return new Equipment(head.clone(), chest.clone(), legs.clone(), boots.clone(), hand.clone(), offhand.clone());
    }
}
