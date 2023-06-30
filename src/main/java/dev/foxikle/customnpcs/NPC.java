package dev.foxikle.customnpcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import dev.foxikle.customnpcs.network.NetworkHandler;
import dev.foxikle.customnpcs.network.NetworkManager;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity ;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The object representing the NPC
 */
public class NPC extends ServerPlayer {
    private UUID uuid;
    private CustomNPCs plugin;
    private GameProfile profile;
    private ItemStack handItem;
    private ItemStack offhandItem;
    private ItemStack headItem;
    private ItemStack chestItem;
    private ItemStack legsItem;
    private ItemStack bootsItem;
    private boolean clickable;
    private Location spawnLoc;
    private String name;
    private World world;
    private TextDisplay hologram;
    private String signature;
    private String value;
    private boolean resilient;
    private String skinName;
    private double direction;
    private Player target;
    private ArrayList<String> actions;

    /**
     * <p> Gets a new NPC
     * </p>
     * @param name The name of the NPC
     * @param actions The actions for the NPC to execute on interaction
     * @param plugin The instance of the Main class
     * @param interactable If the NPC is interactable
     * @param value The encoded value of the NPC skin
     * @param signature The encoded signature of the NPC skin
     * @param gameProfile the GameProfile of the NPC
     * @param minecraftServer The NMS server to spawn the NPC in
     * @param worldServer The NMS world to spawn the NPC in
     * @param uuid The UUID of the NPC (Should be the same as the gameprofile's uuid)
     * @param resilient If the NPC should stay on server restart/reloads
     * @param spawnLoc The location to spawn the NPC
     * @param skinName The cosmetic name of the NPC skin
     * @param target The Entity the NPC should follow
     * @param direction The direction for the NPC to face
     * @param handItem The Item the NPC should hold in their hand
     * @param offhandItem The Item the NPC should hold in their offhand
     * @param bootsItem The Item the NPC should have in the boots slot
     * @param legsItem The Item the NPC should have in the leg slot
     * @param chestItem The Item the NPC should have in the chest slot
     * @param headItem The Item the NPC should have on their head
     */
    public NPC(CustomNPCs plugin, MinecraftServer minecraftServer, ServerLevel worldServer, GameProfile gameProfile, Location spawnLoc, ItemStack handItem, ItemStack offhandItem, ItemStack headItem, ItemStack chestItem, ItemStack legsItem, ItemStack bootsItem, boolean interactable, boolean resilient, String name, UUID uuid, String value, String signature, String skinName, double direction, @Nullable Player target, List<String> actions) {
        super(minecraftServer, worldServer, gameProfile);
        if(gameProfile.getId() != uuid) throw new IllegalArgumentException("The GameProfile's UUID MUST match the NPC's UUID!");
        this.spawnLoc = spawnLoc;
        this.offhandItem = offhandItem;
        this.headItem = headItem;
        this.chestItem = chestItem;
        this.legsItem = legsItem;
        this.bootsItem = bootsItem;
        this.clickable = interactable;
        this.handItem = handItem;
        this.name = name;
        this.profile = gameProfile;
        this.world = spawnLoc.getWorld();
        this.uuid = uuid;
        this.signature = signature;
        this.value = value;
        this.resilient = resilient;
        this.skinName = skinName;
        this.direction = direction;
        this.target = target;
        this.actions = new ArrayList<>(actions);
        super.connection = new NetworkHandler(minecraftServer, new NetworkManager(PacketFlow.CLIENTBOUND), this);
        this.plugin = plugin;
    }

    /**
     * <p> Sets the NPC's loaction and rotation
     * </p>
     * @param location The location to set the NPC
     */
    private void setPosRot(Location location) {
        this.setPos(location.getX(), location.getY(), location.getZ());
        this.setXRot(location.getYaw());
        this.setYRot(location.getPitch());
    }

    /**
     * <p> Creates the NPC and injects it into every player
     * </p>
     */
    public void createNPC() {
        Bukkit.getScheduler().runTask(plugin, () -> this.hologram = setupHologram(name));
        if (plugin.npcs.containsKey(uuid)) {
            plugin.getNPCByID(uuid).remove();
            plugin.getNPCByID(uuid).delete();
        }
        setSkin();
        setPosRot(spawnLoc);
        this.getBukkitEntity().setInvulnerable(true);
        this.getBukkitEntity().setNoDamageTicks(Integer.MAX_VALUE);
        super.getCommandSenderWorld().addFreshEntity(this);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HAND, handItem, true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.OFF_HAND, offhandItem, true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HEAD, headItem, true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.CHEST, chestItem, true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.LEGS, legsItem, true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.FEET, bootsItem, true);
        super.getBukkitEntity().setCustomNameVisible(clickable);
        super.getBukkitEntity().addScoreboardTag("NPC");
        super.getBukkitEntity().setItemInHand(handItem);
        super.detectEquipmentUpdates();
        if(!clickable){
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").addPlayer(this.getBukkitEntity());
        }

        GameProfile gameProfile = super.getGameProfile();
        try {
            Field field = gameProfile.getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(gameProfile, clickable ? "§e§lClick" : "nothing");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (resilient) plugin.getFileManager().addNPC(this);
        plugin.addNPC(this, hologram);


        //TODO: change this V
        Bukkit.getOnlinePlayers().forEach(this::injectPlayer);
    }

    /**
     * <p> Applies the skin to the NPC's GameProfile
     * </p>
     */
    private void setSkin() {
        super.getGameProfile().getProperties().removeAll("textures");
        super.getGameProfile().getProperties().put("textures", new Property("textures", value, signature));
        byte bitmask = (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, bitmask);
    }

    /**
     * <p> Gets the page the player is in.
     * </p>
     * @param name The name to give the text display
     * * @return the TextDisplay representing the NPC's nametag
     */
    private TextDisplay setupHologram(String name) {
        TextDisplay hologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(new Location(spawnLoc.getWorld(), spawnLoc.getX(), clickable ? spawnLoc.getY() + 2.33 : spawnLoc.getY() + 2.05, spawnLoc.getZ()), EntityType.TEXT_DISPLAY);
        hologram.setInvulnerable(true);
        hologram.setBillboard(Display.Billboard.CENTER);
        hologram.setText(ChatColor.translateAlternateColorCodes('&', name));
        hologram.addScoreboardTag("npcHologram");
        return hologram;
    }

    /**
     * <p> If the NPC is clickable
     * </p>
     * @return If the NPC is clickable
     */
    public boolean isClickable() {
        return clickable;
    }

    /**
     * <p> sets if the NPC is clickable
     * </p>
     * @param clickable if the NPC is clickable
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
        GameProfile gameProfile = super.getGameProfile();
        try {
            Field field = gameProfile.getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(gameProfile, clickable ? "§e§lClick" : "nothing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <p> Gets the NPC's GameProfile
     * </p>
     * @return the profile of the NPC
     */
    public GameProfile getProfile() {
        return profile;
    }

    /**
     * <p> Gets the Item in the NPC's main hand
     * </p>
     * @return the Item in the NPC's main (right) hand
     */
    public ItemStack getHandItem() {
        return handItem;
    }

    /**
     * <p> Gets the item in the NPC's main hand
     * </p>
     * @param handItem The item to put in the NPC's hand
     */
    public void setHandItem(ItemStack handItem) {
        this.handItem = handItem;
    }

    /**
     * <p> Gets the Item in the NPC's offhand
     * </p>
     * @return the Item in the NPC's offhand (left hand)
     */
    public ItemStack getItemInOffhand() {
        return offhandItem;
    }

    /**
     * <p> Gets the Item on the NPC's head
     * </p>
     * @return the Item on the NPC's head
     */
    public ItemStack getHeadItem() {
        return headItem;
    }

    /**
     * <p> Gets the item on the NPC's head
     * </p>
     * @param headItem The item to put on the NPC's head
     */
    public void setHeadItem(ItemStack headItem) {
        this.headItem = headItem;
    }

    /**
     * <p> Gets the Item the NPC is wearing on their chest
     * </p>
     * @return the Item the NPC is wearing on their chest
     */
    public ItemStack getChestItem() {
        return chestItem;
    }

    /**
     * <p> Sets the Item the NPC is wearing on their chest
     * </p>
     * @param chestItem The Item to put on the NPC's Chest
     */
    public void setChestItem(ItemStack chestItem) {
        this.chestItem = chestItem;
    }

    /**
     * <p> Gets the Item the NPC is wearing on their legs
     * </p>
     * @return the Item the NPC is wearing on their legs
     */
    public ItemStack getLegsItem() {
        return legsItem;
    }

    /**
     * <p> Sets the Item the NPC is wearing on their legs
     * </p>
     * @param legsItem The item to put on the NPC's legs
     */
    public void setLegsItem(ItemStack legsItem) {
        this.legsItem = legsItem;
    }

    /**
     * <p> Gets the Item the NPC is wearing on their feet
     * </p>
     * @return the Item the NPC is wearing on their feet
     */
    public ItemStack getBootsItem() {
        return bootsItem;
    }

    /**
     * <p> Sets the Item the NPC is wearing on their feet
     * </p>
     * @param bootsItem The item to put on the NPC's feet
     */
    public void setBootsItem(ItemStack bootsItem) {
        this.bootsItem = bootsItem;
    }

    /**
     * <p> If the NPC is resilient
     * </p>
     * @return If the NPC is persistent across server reloads/restarts
     */
    public boolean isResilient() {
        return resilient;
    }

    /**
     * <p> Sets the NPC's resiliency
     * </p>
     * @param resilient if the NPC should persist through server reloads/restarts
     */
    public void setResilient(boolean resilient) {
        this.resilient = resilient;
    }

    /**
     * <p> Gets the NPC's CURRENT location
     * </p>
     * @return the place where the NPC is currently located
     */
    public Location getCurrentLocation() {
        return super.getBukkitEntity().getLocation();
    }

    /**
     * <p> Gets the NPC's spawnpoint is
     * </p>
     * @return the place where the NPC spawns
     */
    public Location getSpawnLoc(){
        return spawnLoc;
    }

    /**
     * <p> Gets the Entity the NPC is targeting
     * </p>
     * @return the Item the NPC is wearing on their feet
     */    public org.bukkit.entity.Entity getTarget() {
        return target;
    }

    /**
     * <p> Sets the NPC's target
     * </p>
     * @param target the Player the Entity should target
     */
    public void setTarget(@Nullable Player target) {
        if(target == null){
            if(this.target != null)
                this.target.sendMessage(ChatColor.translateAlternateColorCodes('&', getHologramName()) + ChatColor.RESET + ChatColor.GREEN + " is no longer following you.");
            this.target = null;
        } else {
            this.target = target;
            this.target.sendMessage(ChatColor.translateAlternateColorCodes('&', getHologramName()) + ChatColor.RESET + ChatColor.GREEN + " is now following you.");
        }
    }

    /**
     * <p> Sets the Location where the NPC should spawn
     * </p>
     * @param spawnLoc The location to spawn
     */
    public void setSpawnLoc(Location spawnLoc) {
        this.spawnLoc = spawnLoc;
    }

    /**
     * <p> Gets the Name of the NPC
     * </p>
     * @return the Item the NPC is wearing on their feet
     */
    public String getHologramName() {
        return name;
    }

    /**
     * <p> Gets the text display representing the NPC nametag
     * </p>
     * @return the Item the NPC is wearing on their feet
     */
    public TextDisplay getHologram() {
        return hologram;
    }

    /**
     * <p> Gets the World the NPC is in
     * </p>
     * @return Gets the World the NPC is in
     */
    public World getWorld() {
        return world;
    }

    /**
     * <p> Gets the list of Actions the NPC executes when interacted with
     * </p>
     * @return the list of Actions the NPC executes when interacted with
     */
    public List<Action> getActions(){
        List<Action> actionList = new ArrayList<>();
        actions.forEach(s -> actionList.add(Action.of(s)));
        return actionList;
    }

    /**
     * <p> Adds an action to the NPC's actions
     * </p>
     * @param action The action to add
     */
    public void addAction(Action action){
        actions.add(action.serialize());
    }

    /**
     * <p> Removes an action from the NPC's actions
     * </p>
     * @param action The action to remove
     * @return if it was successfully removed
     */
    public boolean removeAction(Action action){
        return actions.remove(action.serialize());
    }

    /**
     * <p> Injects packets into the specified player's connection
     * </p>
     * @param p The player to inject
     */
    public void injectPlayer(Player p) {

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> stuffs = new ArrayList<>();
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(handItem)));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(offhandItem)));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(headItem)));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(chestItem)));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(legsItem)));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.FEET, CraftItemStack.asNMSCopy(bootsItem)));

        ClientboundPlayerInfoUpdatePacket playerInfoAdd = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this);
        ClientboundAddPlayerPacket namedEntitySpawn = new ClientboundAddPlayerPacket(this);
        ClientboundPlayerInfoRemovePacket playerInforemove = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(super.getUUID()));
        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(super.getId(), stuffs);
        ClientboundMoveEntityPacket rotation = new ClientboundMoveEntityPacket.Rot(this.getBukkitEntity().getEntityId(), (byte) (getFacingDirection() * 256 / 360), (byte) (0 / 360), true);
        super.detectEquipmentUpdates();
        setSkin();
        ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
        connection.send(playerInfoAdd);
        connection.send(namedEntitySpawn);
        connection.send(equipmentPacket);
        connection.send(rotation);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> connection.send(playerInforemove),30);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40 | 0x80));
    }

    /**
     * <p> Makes the NPC look at an entity
     * </p>
     */
    @Override
    public void lookAt(EntityAnchorArgument.Anchor argumentanchor_anchor, Entity entity, EntityAnchorArgument.Anchor argumentanchor_anchor1) {
        super.lookAt(argumentanchor_anchor, entity, argumentanchor_anchor1);
    }

    /**
     * <p> Despawns the NPC
     * </p>
     */
    public void remove() {
        hologram.remove();
        super.remove(RemovalReason.DISCARDED);
        super.setHealth(0);
        for (Player p: Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
            ClientboundRemoveEntitiesPacket playerInforemove = new ClientboundRemoveEntitiesPacket(super.getId());
            connection.send(playerInforemove);
        }
    }

    /**
     * <p> Thes the Player to the specified Vec3
     * </p>
     * */
    @Override
    public void moveTo(Vec3 v){
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.hologram.teleport(new Location(getWorld(), v.x(), clickable ? v.y() + 2.33 :v.y() + 2.05, v.z())), 3);
        moveTo(v.x(), v.y(), v.z());
    }

    /**
     * <p> Gets the signature of the NPC's skin
     * </p>
     * @return the signature of the NPC's skin
     */
    public String getSignature() {
        return signature;
    }

    /**
     * <p> Sets the NPC's skin signature
     * </p>
     * @param signature the skin's signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * <p> Gets the value of the NPC's skin
     * </p>
     * @return the value of the NPC's skin
     */
    public String getValue() {
        return value;
    }

    /**
     * <p> Sets the value of the NPC's skin
     * </p>
     * @param value The skin's value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * <p> Gets the Player object associated with the NPC
     * </p>
     * @return the player object
     */
    public ServerPlayer getPlayer() {
        return super.connection.getPlayer();
    }

    /**
     * <p> Sets the item in the NPC's offhand
     * </p>
     * @param offhandItem The item to put in the offhand
     */
    public void setOffhandItem(ItemStack offhandItem) {
        this.offhandItem = offhandItem;
    }

    /**
     * <p> Sets the NPC's display name
     * </p>
     * @param name The NPC's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p> Gets the display name of the NPC's Skin
     * </p>
     * @return the signature of the NPC's skin
     */
    public String getSkinName() {
        return this.skinName;
    }

    /**
     * <p> Sets the display name of the NPC's Skin
     * </p>
     * @param skinName The name of the skin
     */
    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    /**
     * <p> Gets the direction the NPC is facing when there are no players within 5 blocks.
     * </p>
     * @return the NPC's heading
     */
    public double getFacingDirection() {
        return direction;
    }

    /**
     * <p> Sets the direction the NPC is facing when there are no players within 5 blocks.
     * </p>
     * @param direction the heading to face
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }

    /**
     * <p> Permantanly deletes an NPC. Does not despawn it.
     * </p>
     */
    public void delete(){
        plugin.getFileManager().remove(this.uuid);
    }

    /**
     * <p> Sets the actions executed when the NPC is interacted with.
     * </p>
     * @param actions The collection of actions
     */
    public void setActions(Collection<Action> actions) {
        List<String> strs = new ArrayList<>();
        actions.forEach(action -> strs.add(action.serialize()));
        this.actions = new ArrayList<>(strs);
    }
}
