package dev.foxikle.customnpcs.data;

import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * The class representing the NPC's items
 */
public class Equipment {


    /**
     * The item on the NPC's head
     */
    private ItemStack head = new ItemStack(Material.AIR);

    /**
     * The item on the NPC's chest
     */
    private ItemStack chest = new ItemStack(Material.AIR);

    /**
     * The item on the NPC's legs
     */
    private ItemStack legs = new ItemStack(Material.AIR);

    /**
     * The item on the NPC's feet
     */
    private ItemStack boots = new ItemStack(Material.AIR);

    /**
     * The item in the NPC's main hand
     */
    private ItemStack hand = new ItemStack(Material.AIR);

    /**
     * The item in the NPC's offhand
     */
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
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.hand = hand;
        this.offhand = offhand;
    }

    /**
     * A constructor to create an equipment object with air as all items
     */
    public Equipment() {
        // defualt constructor
    }

    /**
     * Gets the ItemStack the npc is wearing for boots
     * @return the boots
     */
    public ItemStack getBoots() {
        return boots;
    }

    /**
     * Gets the ItemStack the npc is wearing for legs
     * @return the leggings
     */
    public ItemStack getLegs() {
        return legs;
    }

    /**
     * Gets the ItemStack the npc is wearing for a chestplate
     * @return the chestplate
     */
    public ItemStack getChest() {
        return chest;
    }

    /**
     * Gets the ItemStack the npc is wearing for a helmet
     * @return the helmet
     */
    public ItemStack getHead() {
        return head;
    }

    /**
     * Gets the ItemStack the npc is holding
     * @return the main hand item
     */
    public ItemStack getHand() {
        return hand;
    }

    /**
     * Gets the ItemStack the npc is holding in their offhand
     * @return the offhand item
     */
    public ItemStack getOffhand() {
        return offhand;
    }

    /**
     * Sets the NPC's boots
     * @param boots the boots to wear
     */
    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    /**
     * Sets the NPC's leggings
     * @param legs the leggings to wear
     */
    public void setLegs(ItemStack legs) {
        this.legs = legs;
    }

    /**
     * Sets the NPC's chestplate
     * @param chest the chestplate to wear
     */
    public void setChest(ItemStack chest) {
        this.chest = chest;
    }

    /**
     * Sets the NPC's helmet
     * @param head the helmet to wear
     */
    public void setHead(ItemStack head) {
        this.head = head;
    }

    /**
     * Sets the NPC's hand
     * @param hand the item to hold in their main hand
     */
    public void setHand(ItemStack hand) {
        this.hand = hand;
    }

    /**
     * Sets the NPC's hand
     * @param offhand the item to hold in their offhand
     */
    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }

    /**
     * Imports the relevant items from an EntityEquipment Object
     * @param e The entity equipment to pull items from.
     */
    public void importFromEntityEquipment(EntityEquipment e) {
        this.head = e.getHelmet();
        this.chest = e.getChestplate();
        this.legs = e.getLeggings();
        this.boots = e.getBoots();
        this.hand = e.getItemInMainHand();
        this.offhand = e.getItemInOffHand();
    }
}
