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

package dev.foxikle.customnpcs.versions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.InjectionManager;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The object representing the NPC
 */
public class NPC_v1_20_R3 extends ServerPlayer implements InternalNpc {

    private final String MC_NAME;

    // reflection for data accessors
    private final EntityDataAccessor<net.minecraft.network.chat.Component> TEXT_DISPLAY_ACCESSOR;
    private final UUID uuid;
    private final CustomNPCs plugin;
    private final World world;
    private final Map<UUID, Integer> loops = new HashMap<>();
    private Settings settings;
    private Equipment equipment;
    private Location spawnLoc;
    private ArmorStand hideNametag;
    private TextDisplay clickableHologram;
    private TextDisplay hologram;
    private Player target;
    private List<Action> actionImpls;
    private String holoName = "ERROR";
    private String clickableName = "ERROR";
    private InjectionManager injectionManager;

    /**
     * <p> Gets a new NPC
     * </p>
     *
     * @param actionImpls The actions for the NPC to execute on interaction
     * @param plugin      The instance of the Main class
     * @param uuid        The UUID of the NPC (Should be the same as the gameprofile's uuid)
     * @param spawnLoc    The location to spawn the NPC
     * @param target      The Entity the NPC should follow
     * @param world       The world to create the NPC in
     * @param settings    The settings for the NPC
     * @param equipment   The NPC's equipment
     */
    public NPC_v1_20_R3(CustomNPCs plugin, World world, Location spawnLoc, Equipment equipment, Settings settings, UUID uuid, @Nullable Player target, List<Action> actionImpls) {
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) world).getHandle(), new GameProfile(uuid, uuid.toString().substring(0, 16)), ClientInformation.createDefault());
        this.spawnLoc = spawnLoc;
        this.equipment = equipment;
        this.settings = settings;
        this.world = spawnLoc.getWorld();
        this.uuid = uuid;
        this.target = target;
        this.actionImpls = actionImpls;
        super.connection = new FakeListener_v1_20_R3(((CraftServer) Bukkit.getServer()).getServer(), new FakeConnection_v1_20_R3(PacketFlow.CLIENTBOUND), this);
        this.plugin = plugin;

        // aM
        try {
            Field field = net.minecraft.world.entity.Display.TextDisplay.class.getDeclaredField("aM");
            field.setAccessible(true);
            TEXT_DISPLAY_ACCESSOR = (EntityDataAccessor<net.minecraft.network.chat.Component>) field.get(new EntityDataAccessor<>(0, EntityDataSerializers.COMPONENT));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        this.MC_NAME = uuid.toString().substring(0, 16);
    }

    /**
     * <p> Sets the NPC's loaction and rotation
     * </p>
     *
     * @param location The location to set the NPC
     */
    public void setPosRot(Location location) {
        this.setPos(location.getX(), location.getY(), location.getZ());
        this.setXRot(location.getPitch());
        this.setYRot(location.getYaw());
    }

    /**
     * <p> Creates the NPC and injects it into every player
     * </p>
     */
    public void createNPC() {
        if (plugin.npcs.containsKey(uuid)) {
            plugin.getNPCByID(uuid).remove();
            plugin.getNPCByID(uuid).delete();
        }

        Bukkit.getScheduler().runTask(plugin, () -> setupHologram(settings.getName()));
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (settings.isInteractable() && !settings.isHideClickableHologram()) {
                if (settings.getCustomInteractableHologram() == null || settings.getCustomInteractableHologram().isEmpty()) {
                    setupClickableHologram(plugin.getConfig().getString("ClickText"));
                } else {
                    setupClickableHologram(settings.getCustomInteractableHologram());
                }
            }
        });

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
        super.getBukkitEntity().addScoreboardTag("NPC");
        super.getBukkitEntity().setItemInHand(equipment.getHand());

        hideNametag = world.spawn(spawnLoc, ArmorStand.class);
        hideNametag.setVisible(false);
        hideNametag.setMarker(true);
        ((CraftArmorStand) hideNametag).getHandle().startRiding(this, true);

        if (settings.isResilient()) plugin.getFileManager().addNPC(this);
        plugin.addNPC(this, hologram);

        injectionManager = new InjectionManager(plugin, this);
        injectionManager.setup();
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
     *
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's nametag
     */
    public TextDisplay setupHologram(String name) {
        holoName = name;
        hologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(new Location(spawnLoc.getWorld(), spawnLoc.getX(), settings.isInteractable() && !settings.isHideClickableHologram() && plugin.getConfig().getBoolean("DisplayClickText") ? spawnLoc.getY() + 2.33 : spawnLoc.getY() + 2.05, spawnLoc.getZ()), EntityType.TEXT_DISPLAY);
        hologram.setInvulnerable(true);
        hologram.setBillboard(Display.Billboard.CENTER);
        hologram.text(Component.empty());
        hologram.addScoreboardTag("npcHologram");
        hologram.setTeleportDuration(settings.getInterpolationDuration());
        return hologram;
    }

    /**
     * <p> Creates the NPC's clickable hologram
     * </p>
     *
     * @param name The name to give the text display
     * @return the TextDisplay representing the NPC's hologram
     */
    public TextDisplay setupClickableHologram(String name) {
        clickableName = name;
        clickableHologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 2.05, spawnLoc.getZ()), EntityType.TEXT_DISPLAY);
        clickableHologram.setInvulnerable(true);
        clickableHologram.setBillboard(Display.Billboard.CENTER);
        clickableHologram.text(Component.empty());
        clickableHologram.addScoreboardTag("npcHologram");
        clickableHologram.setTeleportDuration(5);
        clickableHologram.setTeleportDuration(settings.getInterpolationDuration());
        return clickableHologram;
    }

    /**
     * <p> Gets the NPC's CURRENT location
     * </p>
     *
     * @return the place where the NPC is currently located
     */
    public Location getCurrentLocation() {
        return super.getBukkitEntity().getLocation();
    }

    /**
     * <p> Gets the NPC's spawnpoint is
     * </p>
     *
     * @return the place where the NPC spawns
     */
    public Location getSpawnLoc() {
        return spawnLoc;
    }

    /**
     * <p> Sets the Location where the NPC should spawn
     * </p>
     *
     * @param spawnLoc The location to spawn
     */
    public void setSpawnLoc(Location spawnLoc) {
        this.spawnLoc = spawnLoc;
    }

    /**
     * <p> Gets the Entity the NPC is targeting
     * </p>
     *
     * @return the Item the NPC is wearing on their feet
     */
    public org.bukkit.entity.Entity getTarget() {
        return target;
    }

    /**
     * <p> Sets the NPC's target
     * </p>
     *
     * @param target the Player the Entity should target
     */
    public void setTarget(@Nullable Player target) {
        if (target == null) {
            if (this.target != null)
                this.target.sendMessage(plugin.getMiniMessage().deserialize(settings.getName()).append(Component.text(" is no longer following you.", NamedTextColor.RED)));
            this.target = null;
        } else {
            this.target = target;
            this.target.sendMessage(plugin.getMiniMessage().deserialize(settings.getName()).append(Component.text(" is now following you.", NamedTextColor.GREEN)));
        }
    }

    /**
     * <p> Gets the text display representing the NPC nametag
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their nametag
     */
    public TextDisplay getHologram() {
        return hologram;
    }

    /**
     * <p> Gets the text display representing the NPC nametag
     * </p>
     *
     * @return the TextDisplay entity the NPC uses for their clickable hologram
     */
    @Nullable
    public TextDisplay getClickableHologram() {
        return clickableHologram;
    }

    /**
     * <p> Gets the World the NPC is in
     * </p>
     *
     * @return Gets the World the NPC is in
     */
    public @NotNull World getWorld() {
        return world;
    }

    /**
     * <p> Gets the list of Actions the NPC executes when interacted with
     * </p>
     *
     * @return the list of Actions the NPC executes when interacted with
     */
    public List<Action> getActions() {
        return actionImpls;
    }

    /**
     * <p> Sets the actions executed when the NPC is interacted with.
     * </p>
     *
     * @param actionImpls The collection of actions
     */
    public void setActions(List<Action> actionImpls) {
        this.actionImpls = actionImpls;
    }

    /**
     * <p> Adds an action to the NPC's actions
     * </p>
     *
     * @param actionImpl The action to add
     */
    public void addAction(Action actionImpl) {
        actionImpls.add(actionImpl);
    }

    /**
     * <p> Removes an action from the NPC's actions
     * </p>
     *
     * @param actionImpl The action to remove
     * @return if it was successfully removed
     */
    public boolean removeAction(Action actionImpl) {
        return actionImpls.remove(actionImpl);
    }

    /**
     * <p> Injects packets into the specified player's connection
     * </p>
     *
     * @param p The player to inject
     */
    public void injectPlayer(Player p) {
        if (p.getWorld() != spawnLoc.getWorld()) return;
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
        ClientboundSetPassengersPacket hideName = new ClientboundSetPassengersPacket(this);
        setSkin();
        ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
        connection.send(playerInfoAdd);
        connection.send(namedEntitySpawn);
        connection.send(equipmentPacket);
        connection.send(rotation);
        connection.send(hideName);

        if (plugin.isDebug()) {
            plugin.getLogger().info("[DEBUG] Injected npc '" + this.displayName + "' to player '" + p.getName() + "'");
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> connection.send(playerInforemove), 30);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40 | 0x80));


        // create them
        Bukkit.getScheduler().runTaskLater(plugin, () -> injectHolograms(p), 3);
        injectHolograms(p);
        // we only want to update them if the server is running placeholder API
        if (plugin.papi) {
            if (loops.containsKey(p.getUniqueId())) {
                Bukkit.getScheduler().cancelTask(loops.get(p.getUniqueId()));
            }

            loops.put(p.getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOnline()) {
                        this.cancel();
                        loops.remove(p.getUniqueId());
                    }
                    injectHolograms(p);
                }
            }.runTaskTimerAsynchronously(plugin, 0, plugin.getConfig().getInt("HologramUpdateInterval")).getTaskId());
        }
        setYRotation((float) settings.getDirection());
    }

    private void injectHolograms(Player p) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
        String hologramText = holoName;
        String clickableText = clickableName;
        if (plugin.papi) {
            hologramText = PlaceholderAPI.setPlaceholders(p, holoName);
            clickableText = PlaceholderAPI.setPlaceholders(p, clickableName);
        }

        if (hologram != null) {
            List<SynchedEntityData.DataValue<?>> meta = ((CraftTextDisplay) hologram).getHandle().getEntityData().getNonDefaultValues();
            net.minecraft.network.chat.Component hologramComponent = net.minecraft.network.chat.Component.Serializer.fromJson(JSONComponentSerializer.json().serialize(plugin.getMiniMessage().deserialize(hologramText)));
            meta.set(0, SynchedEntityData.DataValue.create(TEXT_DISPLAY_ACCESSOR, hologramComponent));
            ClientboundSetEntityDataPacket namePacket = new ClientboundSetEntityDataPacket(hologram.getEntityId(), meta);
            Bukkit.getScheduler().runTaskLater(plugin, () -> connection.send(namePacket), 5);
        }

        if (clickableHologram != null && settings.isInteractable() && !settings.isHideClickableHologram()) {
            List<SynchedEntityData.DataValue<?>> meta = ((CraftTextDisplay) clickableHologram).getHandle().getEntityData().getNonDefaultValues();
            net.minecraft.network.chat.Component clickableComponent = net.minecraft.network.chat.Component.Serializer.fromJson(JSONComponentSerializer.json().serialize(plugin.getMiniMessage().deserialize(clickableText)));
            meta.set(0, SynchedEntityData.DataValue.create(TEXT_DISPLAY_ACCESSOR, clickableComponent));

            ClientboundSetEntityDataPacket clickablePacket = new ClientboundSetEntityDataPacket(clickableHologram.getEntityId(), meta);
            Bukkit.getScheduler().runTaskLater(plugin, () -> connection.send(clickablePacket), 5);
        }
    }

    /**
     * <p> Despawns the NPC
     * </p>
     */
    public void remove() {
        injectionManager.shutDown();
        loops.forEach((uuid1, integer) -> Bukkit.getScheduler().cancelTask(integer));
        loops.clear();
        List<Packet<?>> packets = new ArrayList<>();
        if (hologram != null) {
            packets.add(new ClientboundRemoveEntitiesPacket(hologram.getEntityId()));
            hologram.remove();
        }
        if (clickableHologram != null) {
            packets.add(new ClientboundRemoveEntitiesPacket(clickableHologram.getEntityId()));
            clickableHologram.remove();
        }
        packets.add(new ClientboundRemoveEntitiesPacket(super.getId()));

        super.remove(RemovalReason.DISCARDED);
        super.setHealth(0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
            packets.forEach(connection::send);
        }
    }

    @Override
    public void moveTo(Location v) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.hologram.teleport(new Location(getWorld(), v.x(), settings.isInteractable() && !settings.isHideClickableHologram() ? v.y() + 2.33 : v.y() + 2.05, v.z())), 3);
        if (settings.isInteractable() && !settings.isHideClickableHologram())
            Bukkit.getScheduler().runTaskLater(plugin, () -> this.clickableHologram.teleport(new Location(getWorld(), v.x(), v.y() + 2.05, v.z())), 3);

        moveTo(v.x(), v.y(), v.z(), v.getYaw(), v.getPitch());
    }


    /**
     * <p> Permantanly deletes an NPC. Does NOT despawn it.
     * </p>
     */
    public void delete() {
        plugin.getFileManager().remove(this.uuid);
    }

    @Override
    public void lookAt(LookAtAnchor anchor, Entity e) {
        switch (anchor) {
            case HEAD ->
                    super.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftEntity) e).getHandle(), EntityAnchorArgument.Anchor.EYES);
            case FEET ->
                    super.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftEntity) e).getHandle(), EntityAnchorArgument.Anchor.FEET);
        }
    }

    public void lookAt(Location loc) {
        super.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(loc.x(), loc.y(), loc.z()));
    }

    @Override
    public void updateSkin() {
        setSkin();
    }

    @Override
    public void swingArm() {
        super.swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public Equipment getEquipment() {
        return equipment;
    }

    @Override
    public void setEquipment(Equipment e) {
        this.equipment = e;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(Settings s) {
        this.settings = s;
    }

    @Override
    public void reloadSettings() {
        if (hideNametag != null) hideNametag.remove();
        if (hologram != null)
            hologram.remove();
        if (clickableHologram != null)
            clickableHologram.remove();

        Bukkit.getScheduler().runTask(plugin, () -> {
            setupHologram(settings.getName());
            if (settings.isHideBackgroundHologram()) hologram.setBackgroundColor(null);
            if (settings.getHologramBackground() != null) {
                hologram.setBackgroundColor(settings.getHologramBackground());
            }
            if (settings.isInteractable() && !settings.isHideClickableHologram()) {
                if (settings.getCustomInteractableHologram().isEmpty()) {
                    setupClickableHologram(plugin.getConfig().getString("ClickText"));
                } else {
                    setupClickableHologram(settings.getCustomInteractableHologram());
                }
                if (settings.isHideBackgroundHologram()) clickableHologram.setBackgroundColor(null);
                if (settings.getHologramBackground() != null) {
                    clickableHologram.setBackgroundColor(settings.getHologramBackground());
                }
            }
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

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public void setYRotation(float f) {
        super.setXRot(spawnLoc.getPitch());
        super.setYRot(f);
        super.setYBodyRot(f);
        super.setYHeadRot(f);
        lookAt(Utils.calcLocation(this));
    }

    /**
     * @return Bukkit goobery
     */
    @Override
    public Particle spawnParticle() {
        return Particle.EXPLOSION_LARGE;
    }

    @Override
    public InternalNpc clone() {
        return new NPC_v1_20_R3(plugin, world, spawnLoc.clone(), equipment.clone(), settings.clone(), UUID.randomUUID(), target, new ArrayList<>(actionImpls));
    }
}

