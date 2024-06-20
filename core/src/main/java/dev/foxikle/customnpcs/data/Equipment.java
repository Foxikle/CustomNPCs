package dev.foxikle.customnpcs.data;

import dev.foxikle.customnpcs.internal.menu.MenuItems;
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
     * @param head  The item on the NPC's head
     * @param chest The item on the NPC's chest
     * @param legs The item on the NPC's legs
     * @param boots The item on the NPC's feet
     * @param hand The item in the NPC's hand
     * @param offhand The item in the NPC's offhand
     */
    public Equipment(ItemStack head, ItemStack chest, ItemStack legs, ItemStack boots, ItemStack hand, ItemStack offhand) {
        this.head = head == null ? MenuItems.ERROR_EQUIPMENT : head;
        this.chest = chest == null ? MenuItems.ERROR_EQUIPMENT : chest;
        this.legs = legs == null ? MenuItems.ERROR_EQUIPMENT : legs;
        this.boots = boots == null ? MenuItems.ERROR_EQUIPMENT : boots;
        this.hand = hand == null ? MenuItems.ERROR_EQUIPMENT : hand;
        this.offhand = offhand == null ? MenuItems.ERROR_EQUIPMENT : offhand;
    }

    /**
     * A constructor to create an equipment object with air as all items
     */
    public Equipment() {
        // default constructor
    }

    /**
     * Imports the relevant items from an EntityEquipment Object
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
    public Equipment clone(){
        return new Equipment(head.clone(), chest.clone(), legs.clone(), boots.clone(), hand.clone(), offhand.clone());
    }
}
