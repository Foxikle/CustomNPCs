package dev.foxikle.customnpcs.internal.interfaces;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InternalNPC {


        /**
         * <p> Sets the NPC's loaction and rotation
         * </p>
         * @param location The location to set the NPC
         */
        void setPosRot(Location location);

        /**
         * <p> Creates the NPC and injects it into every player
         * </p>
         */
        void createNPC();

        /**
         * <p> Applies the skin to the NPC's GameProfile
         * </p>
         */
        void setSkin();

        /**
         * <p> Creates the NPC's name hologram
         * </p>
         * @param name The name to give the text display
         * @return the TextDisplay representing the NPC's nametag
         */
        TextDisplay setupHologram(String name);

        /**
         * <p> Creates the NPC's clickable hologram
         * </p>
         * @param name The name to give the text display
         * @return the TextDisplay representing the NPC's hologram
         */
        TextDisplay setupClickableHologram(String name);

        /**
         * <p> If the NPC is clickable
         * </p>
         * @return If the NPC is clickable
         */
        boolean isClickable();

        /**
         * <p> sets if the NPC is clickable
         * </p>
         * @param clickable if the NPC is clickable
         */
        void setClickable(boolean clickable);

        /**
         * <p> Gets the Item in the NPC's main hand
         * </p>
         * @return the Item in the NPC's main (right) hand
         */
        ItemStack getHandItem();

        /**
         * Gets the NPC's uuid
         * @return the NPC's uuid
         */
        UUID getUniqueID();


        /**
         * <p> Gets the item in the NPC's main hand
         * </p>
         * @param handItem The item to put in the NPC's hand
         */
        void setHandItem(ItemStack handItem);

        /**
         * <p> Gets the Item in the NPC's offhand
         * </p>
         * @return the Item in the NPC's offhand (left hand)
         */
        public ItemStack getItemInOffhand();

        /**
         * <p> Gets the Item on the NPC's head
         * </p>
         * @return the Item on the NPC's head
         */
        public ItemStack getHeadItem();

        /**
         * <p> Gets the item on the NPC's head
         * </p>
         * @param headItem The item to put on the NPC's head
         */
        public void setHeadItem(ItemStack headItem);

        /**
         * <p> Gets the Item the NPC is wearing on their chest
         * <p> Returns an empty item stack if the npc isn't wearing a chestplate
         * </p>
         * @return the Item the NPC is wearing on their chest
         */
        public ItemStack getChestItem();

        /**
         * <p> Sets the Item the NPC is wearing on their chest
         * </p>
         * @param chestItem The Item to put on the NPC's Chest
         */
        public void setChestItem(ItemStack chestItem);

        /**
         * <p> Gets the Item the NPC is wearing on their legs
         * </p>
         * @return the Item the NPC is wearing on their legs
         */
        public ItemStack getLegsItem();

        /**
         * <p> Sets the Item the NPC is wearing on their legs
         * </p>
         * @param legsItem The item to put on the NPC's legs
         */
        public void setLegsItem(ItemStack legsItem);

        /**
         * <p> Gets the Item the NPC is wearing on their feet
         * </p>
         * @return the Item the NPC is wearing on their feet
         */
        public ItemStack getBootsItem();

        /**
         * <p> Sets the Item the NPC is wearing on their feet
         * </p>
         * @param bootsItem The item to put on the NPC's feet
         */
        void setBootsItem(ItemStack bootsItem);

        /**
         * <p> If the NPC is resilient
         * </p>
         * @return If the NPC is persistent across server reloads/restarts
         */
        boolean isResilient();

        /**
         * <p> Sets the NPC's resiliency
         * </p>
         * @param resilient if the NPC should persist through server reloads/restarts
         */
        void setResilient(boolean resilient);

        /**
         * <p> Gets the NPC's CURRENT location
         * </p>
         * @return the place where the NPC is currently located
         */
        Location getCurrentLocation();

        /**
         * <p> Gets the NPC's spawnpoint is
         * </p>
         * @return the place where the NPC spawns
         */
        Location getSpawnLoc();

        /**
         * <p> Gets the Entity the NPC is targeting
         * </p>
         * @return the Item the NPC is wearing on their feet
         */
        Entity getTarget();

        /**
         * <p> Sets the NPC's target
         * </p>
         * @param target the Player the Entity should target
         */
        void setTarget(@Nullable Player target);

        /**
         * <p> Sets the Location where the NPC should spawn
         * </p>
         * @param spawnLoc The location to spawn
         */
        public void setSpawnLoc(Location spawnLoc);

        /**
         * <p> Gets the Name of the NPC
         * </p>
         * @return the name of the npc (in deserialized MiniMessage form)
         */
        public String getHologramName();

        /**
         * <p> Gets the text display representing the NPC nametag
         * </p>
         * @return the TextDisplay entity the NPC uses for their nametag
         */
        public TextDisplay getHologram();

        /**
         * <p> Gets the text display representing the NPC nametag
         * </p>
         * @return the TextDisplay entity the NPC uses for their clickable hologram
         */
        @Nullable
        public TextDisplay getClickableHologram();

        /**
         * <p> Gets the World the NPC is in
         * </p>
         * @return Gets the World the NPC is in
         */
        public World getWorld();

        /**
         * <p> Gets the list of Actions the NPC executes when interacted with
         * </p>
         * @return the list of Actions the NPC executes when interacted with
         */
        public List<Action> getActions();

        /**
         * <p> Adds an action to the NPC's actions
         * </p>
         * @param action The action to add
         */
        public void addAction(Action action);

        /**
         * <p> Removes an action from the NPC's actions
         * </p>
         * @param action The action to remove
         * @return if it was successfully removed
         */
        public boolean removeAction(Action action);

        /**
         * <p> Injects packets into the specified player's connection
         * </p>
         * @param p The player to inject
         */
        public void injectPlayer(Player p);


        /**
         * <p> Despawns the NPC
         * </p>
         */
        public void remove();

        /**
         * <p> Thes the Player to the specified Vec3
         * </p>
         * */
        public void moveTo(Location v);

        /**
         * <p> Gets the signature of the NPC's skin
         * </p>
         * @return the signature of the NPC's skin
         */
        public String getSignature();

        /**
         * <p> Sets the NPC's skin signature
         * </p>
         * @param signature the skin's signature
         */
        public void setSignature(String signature);

        /**
         * <p> Gets the value of the NPC's skin
         * </p>
         * @return the value of the NPC's skin
         */
        public String getValue();

        /**
         * <p> Sets the value of the NPC's skin
         * </p>
         * @param value The skin's value
         */
        public void setValue(String value);

        /**
         * Determines if the NPC should look at players
         * @return if the npc has tunnelvision
         */
        public boolean isTunnelVision();

        /**
         * Determines if the NPC should look at players
         */
        public void setTunnelVision(boolean tunnelVision);


        /**
         * <p> Sets the item in the NPC's offhand
         * </p>
         * @param offhandItem The item to put in the offhand
         */
        public void setOffhandItem(ItemStack offhandItem);

        /**
         * <p> Sets the NPC's display name
         * </p>
         * @param name The NPC's name
         */
        public void setName(String name);

        /**
         * <p> Gets the display name of the NPC's Skin
         * </p>
         * @return the signature of the NPC's skin
         */
        public String getSkinName();

        /**
         * <p> Sets the display name of the NPC's Skin
         * </p>
         * @param skinName The name of the skin
         */
        public void setSkinName(String skinName);

        /**
         * <p> Gets the direction the NPC is facing when there are no players within 5 blocks.
         * </p>
         * @return the NPC's heading
         */
        public double getFacingDirection();

        /**
         * <p> Sets the direction the NPC is facing when there are no players within 5 blocks.
         * </p>
         * @param direction the heading to face
         */
        public void setDirection(double direction);

        /**
         * <p> Permantanly deletes an NPC. Does NOT despawn it.
         * </p>
         */
        public void delete();

        /**
         * <p> Sets the actions executed when the NPC is interacted with.
         * </p>
         * @param actions The collection of actions
         */
        void setActions(Collection<Action> actions);

        void lookAt(LookAtAnchor anchor, Entity e);
        void lookAt(Location loc);

        void setYRotation(float rot);

        void updateSkin();
        void swingArm();
}
