package dev.foxikle.customnpcs.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Equipment {

    private ItemStack head = new ItemStack(Material.AIR);
    private ItemStack chest = new ItemStack(Material.AIR);
    private ItemStack legs = new ItemStack(Material.AIR);
    private ItemStack boots = new ItemStack(Material.AIR);
    private ItemStack hand = new ItemStack(Material.AIR);
    private ItemStack offhand = new ItemStack(Material.AIR);


    public Equipment(ItemStack head, ItemStack chest, ItemStack legs, ItemStack boots, ItemStack hand, ItemStack offhand) {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.hand = hand;
        this.offhand = offhand;
    }

    public Equipment() {
        // defualt constructor
    }

    public ItemStack getBoots() {
        return boots;
    }

    public ItemStack getLegs() {
        return legs;
    }

    public ItemStack getChest() {
        return chest;
    }

    public ItemStack getHead() {
        return head;
    }

    public ItemStack getHand() {
        return hand;
    }

    public ItemStack getOffhand() {
        return offhand;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public void setLegs(ItemStack legs) {
        this.legs = legs;
    }

    public void setChest(ItemStack chest) {
        this.chest = chest;
    }

    public void setHead(ItemStack head) {
        this.head = head;
    }

    public void setHand(ItemStack hand) {
        this.hand = hand;
    }

    public void setOffhand(ItemStack offhand) {
        this.offhand = offhand;
    }
}
