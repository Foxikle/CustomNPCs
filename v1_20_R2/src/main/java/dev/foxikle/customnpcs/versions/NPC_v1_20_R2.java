package dev.foxikle.customnpcs.versions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * The object representing the NPC
 */
public class NPC_v1_20_R2 extends ServerPlayer implements InternalNPC {
    private final UUID uuid;
    private final CustomNPCs plugin;
    private final Settings settings;
    private final Equipment equipment;
    private Location spawnLoc;
    private final World world;
    private TextDisplay clickableHologram;
    private TextDisplay hologram;
    private Player target;
    private ArrayList<String> actions;

    /**
     * <p> Gets a new NPC
     * </p>
     * @param actions The actions for the NPC to execute on interaction
     * @param plugin The instance of the Main class
     * @param uuid The UUID of the NPC (Should be the same as the gameprofile's uuid)
     * @param spawnLoc The location to spawn the NPC
     * @param target The Entity the NPC should follow
     */
    public NPC_v1_20_R2(CustomNPCs plugin, World world, Location spawnLoc, Equipment equipment, Settings settings, UUID uuid, @Nullable Player target, List<String> actions) {
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) world).getHandle(), new GameProfile(uuid, uuid.toString().substring(0, 16)), ClientInformation.createDefault());
        this.spawnLoc = spawnLoc;
        this.settings = settings;
        this.equipment = equipment;
        this.world = spawnLoc.getWorld();
        this.uuid = uuid;
        this.target = target;
        this.actions = new ArrayList<>(actions);
        super.connection = new FakeListener_v1_20_R2(((CraftServer) Bukkit.getServer()).getServer(), new FakeConnection_v1_20_R2(PacketFlow.CLIENTBOUND), this);
        this.plugin = plugin;
    }

    /**
     * <p> Sets the NPC's loaction and rotation
     * </p>
     * @param location The location to set the NPC
     */
    public void setPosRot(Location location) {
        this.setPos(location.getX(), location.getY(), location.getZ());
        this.setXRot(location.getYaw());
        this.setYRot(location.getPitch());
    }

    /**
     * <p> Creates the NPC and injects it into every player
     * </p>
     */
    public void createNPC() {
        Bukkit.getScheduler().runTask(plugin, () -> this.hologram = setupHologram(settings.getName()));
        Bukkit.getScheduler().runTask(plugin, () -> {
            if(settings.isInteractable())
                this.clickableHologram = setupClickableHologram(plugin.getConfig().getString("ClickText"));
        });
        if (plugin.npcs.containsKey(uuid)) {
            plugin.getNPCByID(uuid).remove();
            plugin.getNPCByID(uuid).delete();
        }
        setSkin();
        setPosRot(spawnLoc);
        this.getBukkitEntity().setInvulnerable(true);
        this.getBukkitEntity().setNoDamageTicks(Integer.MAX_VALUE);
        super.getCommandSenderWorld().addFreshEntity(this);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HAND, equipment.getHand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.OFF_HAND, equipment.getOffhand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HEAD, equipment.getHead(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.CHEST, equipment.getChest(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.LEGS, equipment.getLegs(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.FEET, equipment.getBoots(), true);
        super.getBukkitEntity().setCustomNameVisible(settings.isInteractable());
        super.getBukkitEntity().addScoreboardTag("NPC");
        super.getBukkitEntity().setItemInHand(equipment.getHand());
        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getScoreboardManager().getMainScoreboard().getTeam("npc").addEntry(uuid.toString().substring(0, 16)), 1);

        if (settings.isResilient()) plugin.getFileManager().addNPC(this);
        plugin.addNPC(this, hologram);


        //TODO: change this maybe V
        Bukkit.getOnlinePlayers().forEach(this::injectPlayer);
    }

    /**
     * <p> Applies the skin to the NPC's GameProfile
     * </p>
     */
    public void setSkin() {
        super.getGameProfile().getProperties().removeAll("textures");
        super.getGameProfile().getProperties().put("textures", new Property("textures", settings.getValue(), settings.getSignature()));
        byte bitmask = (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, bitmask);
    }

    /**
     * <p> Creates the NPC's name hologram
     * </p>
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's nametag
     */
    public TextDisplay setupHologram(String name) {
        TextDisplay hologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(new Location(spawnLoc.getWorld(), spawnLoc.getX(), settings.isInteractable() && plugin.getConfig().getBoolean("DisplayClickText") ? spawnLoc.getY() + 2.33 : spawnLoc.getY() + 2.05, spawnLoc.getZ()), EntityType.TEXT_DISPLAY);
        hologram.setInvulnerable(true);
        hologram.setBillboard(Display.Billboard.CENTER);
        hologram.text(plugin.getMiniMessage().deserialize(name));
        hologram.addScoreboardTag("npcHologram");
        return hologram;
    }

    /**
     * <p> Creates the NPC's clickable hologram
     * </p>
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's hologram
     */
    public TextDisplay setupClickableHologram(String name) {
        TextDisplay hologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 2.05, spawnLoc.getZ()), EntityType.TEXT_DISPLAY);
        hologram.setInvulnerable(true);
        hologram.setBillboard(Display.Billboard.CENTER);
        hologram.text(plugin.getMiniMessage().deserialize(name));
        hologram.addScoreboardTag("npcHologram");
        return hologram;
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
                this.target.sendMessage(plugin.getMiniMessage().deserialize(settings.getName()).append(Component.text(" is no longer following you.", NamedTextColor.RED)));
            this.target = null;
        } else {
            this.target = target;
            this.target.sendMessage(plugin.getMiniMessage().deserialize(settings.getName()).append(Component.text(" is now following you.", NamedTextColor.GREEN)));
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
     * <p> Gets the text display representing the NPC nametag
     * </p>
     * @return the TextDisplay entity the NPC uses for their nametag
     */
    public TextDisplay getHologram() {
        return hologram;
    }

    /**
     * <p> Gets the text display representing the NPC nametag
     * </p>
     * @return the TextDisplay entity the NPC uses for their clickable hologram
     */
    @Nullable
    public TextDisplay getClickableHologram() {
        return clickableHologram;
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
        actions.add(action.toJson());
    }

    /**
     * <p> Removes an action from the NPC's actions
     * </p>
     * @param action The action to remove
     * @return if it was successfully removed
     */
    public boolean removeAction(Action action){
        return actions.remove(action.toJson());
    }

    /**
     * <p> Injects packets into the specified player's connection
     * </p>
     * @param p The player to inject
     */
    public void injectPlayer(Player p) {

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> stuffs = new ArrayList<>();
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(equipment.getHand())));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(equipment.getOffhand())));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(equipment.getHead())));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(equipment.getChest())));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(equipment.getLegs())));
        stuffs.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.FEET, CraftItemStack.asNMSCopy(equipment.getBoots())));

        ClientboundPlayerInfoUpdatePacket playerInfoAdd = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this);
        ClientboundAddEntityPacket namedEntitySpawn = new ClientboundAddEntityPacket(this);
        ClientboundPlayerInfoRemovePacket playerInforemove = new ClientboundPlayerInfoRemovePacket(Collections.singletonList(super.getUUID()));
        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(super.getId(), stuffs);
        ClientboundMoveEntityPacket rotation = new ClientboundMoveEntityPacket.Rot(this.getBukkitEntity().getEntityId(), (byte) (settings.getDirection() * 256 / 360), (byte) (0 / 360), true);
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
     * <p> Despawns the NPC
     * </p>
     */
    public void remove() {
        hologram.remove();
        if(settings.isInteractable() && clickableHologram != null)
            clickableHologram.remove();
        super.remove(RemovalReason.DISCARDED);
        super.setHealth(0);
        for (Player p: Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
            ClientboundRemoveEntitiesPacket playerInforemove = new ClientboundRemoveEntitiesPacket(super.getId());
            connection.send(playerInforemove);
        }
    }

    @Override
    public void moveTo(Location v) {

    }

    /**
     * <p> Thes the Player to the specified Vec3
     * </p>
     * */
    @Override
    public void moveTo(@NotNull Vec3 v){
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.hologram.teleport(new Location(getWorld(), v.x(), settings.isInteractable() ? v.y() + 2.33 :v.y() + 2.05, v.z())), 3);
        if(settings.isInteractable())
            Bukkit.getScheduler().runTaskLater(plugin, () -> this.clickableHologram.teleport(new Location(getWorld(), v.x(), v.y() + 2.05, v.z())), 3);

        moveTo(v.x(), v.y(), v.z());
    }

    /**
     * <p> Permantanly deletes an NPC. Does NOT despawn it.
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
        actions.forEach(action -> strs.add(action.toJson()));
        this.actions = new ArrayList<>(strs);
    }

    @Override
    public void lookAt(LookAtAnchor anchor, Entity e) {
        switch (anchor) {
            case HEAD -> super.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftEntity) e).getHandle(), EntityAnchorArgument.Anchor.EYES);
            case FEET -> super.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftEntity) e).getHandle(), EntityAnchorArgument.Anchor.FEET);
        }
    }

    public void lookAt(Location loc) {
        super.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(loc.x(), loc.y(), loc.z()));
    }

    @Override
    public void updateSkin(){
        setSkin();
    }

    @Override
    public void swingArm() {
        super.swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public UUID getUniqueID(){
        return uuid;
    }

    @Override
    public void setYRotation(float f){
        super.setYRot(f);
        super.setYBodyRot(f);
        super.setYHeadRot(f);
    }

    @Override
    public Equipment getEquipment() {
        return equipment;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void reloadSettings(){
        hologram.remove();
        if(settings.isInteractable())
            clickableHologram.remove();

        Bukkit.getScheduler().runTask(plugin, () -> this.hologram = setupHologram(settings.getName()));
        Bukkit.getScheduler().runTask(plugin, () -> {
            if(settings.isInteractable())
                this.clickableHologram = setupClickableHologram(plugin.getConfig().getString("ClickText"));
        });

        setSkin();
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HAND, equipment.getHand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.OFF_HAND, equipment.getOffhand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HEAD, equipment.getHead(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.CHEST, equipment.getChest(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.LEGS, equipment.getLegs(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.FEET, equipment.getBoots(), true);
        super.getBukkitEntity().setItemInHand(equipment.getHand());
    }
}

