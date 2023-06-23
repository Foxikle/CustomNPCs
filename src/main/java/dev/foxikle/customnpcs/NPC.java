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

public class NPC extends ServerPlayer {
    private UUID uuid;
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

    public NPC(MinecraftServer minecraftServer, ServerLevel worldServer, GameProfile gameProfile, Location spawnLoc, ItemStack handItem, ItemStack offhandItem, ItemStack headItem, ItemStack chestItem, ItemStack legsItem, ItemStack bootsItem, boolean clickable, boolean resilient, String name, UUID uuid, String value, String signature, String skinName, double direction, @Nullable Player target, List<String> actions) {
        super(minecraftServer, worldServer, gameProfile);
        this.spawnLoc = spawnLoc;
        this.offhandItem = offhandItem;
        this.headItem = headItem;
        this.chestItem = chestItem;
        this.legsItem = legsItem;
        this.bootsItem = bootsItem;
        this.clickable = clickable;
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
    }

    private static void setPosRot(ServerPlayer test, Location location) {
        test.setPos(location.getX(), location.getY(), location.getZ());
        test.setXRot(location.getYaw());
        test.setYRot(location.getPitch());
    }

    public void createNPC() {
        Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), () -> this.hologram = setupHologram(this.getSpawnLoc(), name));
        if (CustomNPCs.getInstance().npcs.containsKey(uuid)) {
            CustomNPCs.getInstance().getNPCByID(uuid).remove();
            CustomNPCs.getInstance().getNPCByID(uuid).delete();
        }
        setSkin();
        setPosRot(this, spawnLoc);
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

        if (resilient) CustomNPCs.getInstance().getFileManager().addNPC(this);
        CustomNPCs.getInstance().addNPC(this, hologram);
        Bukkit.getOnlinePlayers().forEach(this::injectPlayer);
    }

    private void setSkin() {
        super.getGameProfile().getProperties().removeAll("textures");
        super.getGameProfile().getProperties().put("textures", new Property("textures", value, signature));
        byte bitmask = (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, bitmask);
    }

    private TextDisplay setupHologram(Location loc, String name) {
        TextDisplay hologram = (TextDisplay) loc.getWorld().spawnEntity(new Location(loc.getWorld(), loc.getX(), clickable ? loc.getY() + 2.33 : loc.getY() + 2.05, loc.getZ()), EntityType.TEXT_DISPLAY);
        hologram.setInvulnerable(true);
        hologram.setBillboard(Display.Billboard.CENTER);
        hologram.setText(ChatColor.translateAlternateColorCodes('&', name));
        hologram.addScoreboardTag("npcHologram");
        return hologram;
    }

    public boolean isClickable() {
        return clickable;
    }

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

    public GameProfile getProfile() {
        return profile;
    }

    public void setProfile(GameProfile profile) {
        this.profile = profile;
    }

    public ItemStack getHandItem() {
        return handItem;
    }

    public void setHandItem(ItemStack handItem) {
        this.handItem = handItem;
    }

    public ItemStack getItemInOffhand() {
        return offhandItem;
    }

    public ItemStack getHeadItem() {
        return headItem;
    }

    public void setHeadItem(ItemStack headItem) {
        this.headItem = headItem;
    }

    public ItemStack getChestItem() {
        return chestItem;
    }

    public void setChestItem(ItemStack chestItem) {
        this.chestItem = chestItem;
    }

    public ItemStack getLegsItem() {
        return legsItem;
    }

    public void setLegsItem(ItemStack legsItem) {
        this.legsItem = legsItem;
    }

    public ItemStack getBootsItem() {
        return bootsItem;
    }

    public void setBootsItem(ItemStack bootsItem) {
        this.bootsItem = bootsItem;
    }

    public boolean isResilient() {
        return resilient;
    }

    public void setResilient(boolean resilient) {
        this.resilient = resilient;
    }

    public Location getCurrentLocation() {
        return super.getBukkitEntity().getLocation();
    }

    public Location getSpawnLoc(){
        return spawnLoc;
    }

    public org.bukkit.entity.Entity getTarget() {
        return target;
    }

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

    public void setSpawnLoc(Location spawnLoc) {
        this.spawnLoc = spawnLoc;
    }

    public String getHologramName() {
        return name;
    }

    public TextDisplay getHologram() {
        return hologram;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public List<Action> getActions(){
        List<Action> actionList = new ArrayList<>();
        actions.forEach(s -> actionList.add(Action.of(s)));
        return actionList;
    }

    public void addAction(Action action){
        actions.add(action.serialize());
    }

    public boolean removeAction(Action action){
        return actions.remove(action.serialize());
    }


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

        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomNPCs.getInstance(), () -> connection.send(playerInforemove),30);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40 | 0x80));
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor argumentanchor_anchor, Entity entity, EntityAnchorArgument.Anchor argumentanchor_anchor1) {
        super.lookAt(argumentanchor_anchor, entity, argumentanchor_anchor1);
    }

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

    @Override
    public void moveTo(Vec3 v){
        Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () -> this.hologram.teleport(new Location(getWorld(), v.x(), clickable ? v.y() + 2.33 :v.y() + 2.05, v.z())), 3);
        moveTo(v.x(), v.y(), v.z());
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ServerPlayer getPlayer() {
        return super.connection.getPlayer();
    }

    public void setOffhandItem(ItemStack offhandItem) {
        this.offhandItem = offhandItem;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkinName() {
        return this.skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public double getFacingDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public void delete(){
        CustomNPCs.getInstance().getFileManager().remove(this.uuid);
    }
}
