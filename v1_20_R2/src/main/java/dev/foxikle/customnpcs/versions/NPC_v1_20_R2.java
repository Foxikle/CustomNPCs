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
import dev.foxikle.customnpcs.api.Pose;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.InjectionManager;
import dev.foxikle.customnpcs.internal.LookAtAnchor;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftTextDisplay;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * The object representing the NPC
 */
public class NPC_v1_20_R2 extends ServerPlayer implements InternalNpc {

    @Getter
    private final UUID uniqueID;
    @Getter
    private final Particle spawnParticle = Particle.EXPLOSION_LARGE;
    private final CustomNPCs plugin;
    @Getter
    private final World world;
    private final EntityDataAccessor<net.minecraft.network.chat.Component> TEXT_DISPLAY_ACCESSOR;
    private final Map<UUID, Integer> loops = new HashMap<>();
    @Getter
    @Setter
    private Settings settings;
    @Getter
    @Setter
    private Equipment equipment;
    @Getter
    @Setter
    private Location spawnLoc;
    private @Nullable ArmorStand seat;
    @Getter
    private TextDisplay clickableHologram;
    @Getter
    private List<TextDisplay> holograms;
    @Getter
    @Setter
    private Player target;
    @Getter
    @Setter
    private List<Action> actions;
    private String clickableName = "ERROR";
    private InjectionManager injectionManager;

    public NPC_v1_20_R2(CustomNPCs plugin, World world, Location spawnLoc, Equipment equipment, Settings settings, UUID uniqueID, @Nullable Player target, List<Action> actions) {
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) world).getHandle(), new GameProfile(uniqueID, Utils.getNpcName(settings, uniqueID)), ClientInformation.createDefault());
        this.spawnLoc = spawnLoc;
        this.settings = settings;
        this.equipment = equipment;
        this.world = spawnLoc.getWorld();
        this.uniqueID = uniqueID;
        this.target = target;
        this.actions = new ArrayList<>(actions);
        super.connection = new FakeListener_v1_20_R2(((CraftServer) Bukkit.getServer()).getServer(), new FakeConnection_v1_20_R2(PacketFlow.CLIENTBOUND), this);
        this.plugin = plugin;

        //aM
        try {
            Field field = net.minecraft.world.entity.Display.TextDisplay.class.getDeclaredField("aM");
            field.setAccessible(true);
            TEXT_DISPLAY_ACCESSOR = (EntityDataAccessor<net.minecraft.network.chat.Component>) field.get(new EntityDataAccessor<>(0, EntityDataSerializers.COMPONENT));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPosRot(Location location) {
        this.setPos(location.getX(), location.getY(), location.getZ());
        this.setXRot(location.getPitch());
        this.setYRot(location.getYaw());
    }

    public void createNPC() {
        if (plugin.npcs.containsKey(uniqueID)) {
            plugin.getNPCByID(uniqueID).remove();
            plugin.getNPCByID(uniqueID).delete();
        }

        if (isRemoved()) {
            unsetRemoved();
        }

        setupHolograms();
        if (settings.isInteractable() && !settings.isHideClickableHologram()) {
            setupClickableHologram(settings.getCustomInteractableHologram() == null || settings.getCustomInteractableHologram().isEmpty() ? plugin.getConfig().getString("ClickText") : settings.getCustomInteractableHologram());
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
        super.getBukkitEntity().addScoreboardTag("NPC");
        super.getBukkitEntity().setItemInHand(equipment.getHand());
        setPose(setupPose(settings.getPose()));

        if (settings.isResilient()) plugin.getFileManager().addNPC(this);
        plugin.addNPC(this, holograms);

        injectionManager = new InjectionManager(plugin, this);
        injectionManager.setup();
    }

    public void setSkin() {
        super.getGameProfile().getProperties().removeAll("textures");
        super.getGameProfile().getProperties().put("textures", new Property("textures", settings.getValue(), settings.getSignature()));
        byte bitmask = (byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40);
        super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, bitmask);
    }

    public void setupHolograms() {
        final double space = 0.28;
        double startingOffset = getPoseOffset(settings.getPose());
        boolean displayClickable = settings.isInteractable() && !settings.isHideClickableHologram() && plugin.getConfig().getBoolean("DisplayClickText");
        if (displayClickable) {
            startingOffset += space;
        }
        List<TextDisplay> holograms = new ArrayList<>();
        for (int i = 0; i < settings.getRawHolograms().length; i++) {
            double y = startingOffset + (i * space);
            TextDisplay hologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY);
            hologram.setInvulnerable(true);
            hologram.setBillboard(Display.Billboard.CENTER);
            hologram.addScoreboardTag("npcHologram");
            hologram.setTeleportDuration(settings.getInterpolationDuration());
            hologram.setTransformation(new Transformation(
                    new Vector3f(0, (float) y, 0),
                    hologram.getTransformation().getLeftRotation(),
                    hologram.getTransformation().getScale(),
                    hologram.getTransformation().getRightRotation()
            ));
            holograms.add(hologram);
            ((CraftTextDisplay) hologram).getHandle().startRiding(this, true);
        }
        this.holograms = holograms.reversed();
    }

    public void setupClickableHologram(String name) {
        clickableName = name;
        clickableHologram = (TextDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY);
        clickableHologram.setInvulnerable(true);
        clickableHologram.setBillboard(Display.Billboard.CENTER);
        clickableHologram.text(Component.empty());
        clickableHologram.addScoreboardTag("npcHologram");
        clickableHologram.setTeleportDuration(settings.getInterpolationDuration());

        clickableHologram.setTransformation(new Transformation(
                new Vector3f(0, (float) getPoseOffset(settings.getPose()), 0),
                clickableHologram.getTransformation().getLeftRotation(),
                clickableHologram.getTransformation().getScale(),
                clickableHologram.getTransformation().getRightRotation()
        ));
        ((CraftTextDisplay) clickableHologram).getHandle().startRiding(this, true);
    }

    public Location getCurrentLocation() {
        if (seat != null) {
            return seat.getLocation();
        }
        return super.getBukkitEntity().getLocation();
    }

    @Override
    public void addAction(Action actionImpl) {
        actions.add(actionImpl);
    }

    @Override
    public boolean removeAction(Action actionImpl) {
        return actions.remove(actionImpl);
    }

    @Override
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
        ClientboundMoveEntityPacket rotation = new ClientboundMoveEntityPacket.Rot(this.getBukkitEntity().getEntityId(), (byte) (getYaw() * 256 / 360), (byte) (0 / 360), true);
        ClientboundSetPassengersPacket hideName = new ClientboundSetPassengersPacket(this);
        setSkin();

        ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
        connection.send(playerInfoAdd);
        connection.send(namedEntitySpawn);
        connection.send(equipmentPacket);
        connection.send(rotation);
        connection.send(hideName);
        super.getEntityData().refresh(((CraftPlayer) p).getHandle());

        if (seat != null) {
            connection.send(new ClientboundSetPassengersPacket(((CraftArmorStand) seat).getHandle()));
        }

        if (plugin.isDebug()) {
            plugin.getLogger().info("[DEBUG] Injected npc '" + this.displayName + "' to player '" + p.getName() + "'");
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> connection.send(playerInforemove), 30);
        if (!settings.isUpsideDown()) {
            super.getEntityData().set(net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) (0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40 | 0x80));
        }

        if (loops.containsKey(p.getUniqueId())) {
            Bukkit.getScheduler().cancelTask(loops.get(p.getUniqueId()));
        }

        // create them
        injectHolograms(p);

        loops.put(p.getUniqueId(),
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!p.isOnline()) this.cancel();
                        injectHolograms(p);
                    }
                }.runTaskTimerAsynchronously(plugin, 0, plugin.getConfig().getInt("HologramUpdateInterval"))
                        .getTaskId());
    }

    private void injectHolograms(Player p) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
        String[] hologramText = new String[settings.getRawHolograms().length];
        String clickableText = clickableName;
        if (plugin.papi) {
            for (int i = 0; i < settings.getRawHolograms().length; i++) {
                hologramText[i] = PlaceholderAPI.setPlaceholders(p, settings.getRawHolograms()[i]);
            }
            clickableText = PlaceholderAPI.setPlaceholders(p, clickableName);
        } else {
            for (int i = 0; i < settings.getRawHolograms().length; i++) {
                hologramText[i] = settings.getRawHolograms()[i];
            }
        }
        List<Packet<?>> packets = new ArrayList<>();

        for (int i = 0; i < holograms.size(); i++) {
            TextDisplay hologram = holograms.get(i);
            packets.add(createMojComponent(hologramText[i], hologram));
        }


        if (clickableHologram != null && settings.isInteractable() && !settings.isHideClickableHologram()) {
            packets.add(createMojComponent(clickableText, clickableHologram));
        }
        packets.forEach(connection::send);
        Bukkit.getScheduler().runTaskLater(plugin, () -> packets.forEach(connection::send), 5);
    }

    private Packet<?> createMojComponent(String clickableText, TextDisplay clickableHologram) {
        List<SynchedEntityData.DataValue<?>> meta = ((CraftTextDisplay) clickableHologram).getHandle().getEntityData().getNonDefaultValues();
        String serialized_component = JSONComponentSerializer.json().serialize(plugin.getMiniMessage().deserialize(clickableText));
        net.minecraft.network.chat.Component clickableComponent = net.minecraft.network.chat.Component.Serializer.fromJson(serialized_component);
        meta.set(0, SynchedEntityData.DataValue.create(TEXT_DISPLAY_ACCESSOR, clickableComponent));

        return new ClientboundSetEntityDataPacket(clickableHologram.getEntityId(), meta);
    }

    @Override
    public void remove() {
        injectionManager.shutDown();
        loops.forEach((uuid1, integer) -> Bukkit.getScheduler().cancelTask(integer));
        loops.clear();

        List<Packet<?>> packets = new ArrayList<>();
        if (holograms != null) {
            for (TextDisplay hologram : holograms) {
                packets.add(new ClientboundRemoveEntitiesPacket(hologram.getEntityId()));
                hologram.remove();
            }
        }
        if (seat != null) {
            seat.remove();
        }
        if (clickableHologram != null) {
            packets.add(new ClientboundRemoveEntitiesPacket(clickableHologram.getEntityId()));
            clickableHologram.remove();
        }
        packets.add(new ClientboundRemoveEntitiesPacket(super.getId()));

        super.remove(RemovalReason.DISCARDED);
        for (Player p : Bukkit.getOnlinePlayers()) {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) p).getHandle().connection;
            packets.forEach(connection::send);
        }
    }

    @Override
    public void moveTo(Vector v) {
        if (isRemoved()) return;
        if (seat != null) {
            ((CraftArmorStand) seat).getHandle().move(MoverType.PLAYER, new Vec3(v.getX(), v.getY(), v.getZ()));
            spawnLoc = seat.getLocation();
        } else {
            super.move(MoverType.PLAYER, new Vec3(v.getX(), v.getY(), v.getZ()));
            spawnLoc = getCurrentLocation();
        }
    }

    @Override
    public void teleport(Location loc) {
        teleportTo(loc.x(), loc.y(), loc.z());
        spawnLoc = loc;
    }

    public void delete() {
        plugin.getFileManager().remove(this.uniqueID);
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
    public void setYRotation(float f) {
        super.setYRot(f);
        super.setYBodyRot(f);
        super.setYHeadRot(f);
        lookAt(Utils.calcLocation(this));
    }

    @Override
    public void setXRotation(float f) {
        super.setXRot(f);
        lookAt(Utils.calcLocation(this));
    }

    @Override
    public void reloadSettings() {
        if (seat != null) {
            seat.remove();
            seat = null;
        }

        if (holograms != null) {
            for (TextDisplay hologram : holograms) {
                Bukkit.getScheduler().runTask(plugin, hologram::remove);
                hologram.remove();
            }
            holograms.clear();
        }

        if (clickableHologram != null)
            clickableHologram.remove();

        setPose(setupPose(settings.getPose()));


        setupHolograms();
        for (TextDisplay hologram : holograms) {
            hologram.setBackgroundColor(settings.isHideBackgroundHologram() ? null : settings.getHologramBackground());
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


        setSkin();
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HAND, equipment.getHand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.OFF_HAND, equipment.getOffhand(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.HEAD, equipment.getHead(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.CHEST, equipment.getChest(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.LEGS, equipment.getLegs(), true);
        super.getBukkitEntity().getEquipment().setItem(EquipmentSlot.FEET, equipment.getBoots(), true);
        super.getBukkitEntity().setItemInHand(equipment.getHand());
    }

    private net.minecraft.world.entity.Pose setupPose(Pose pose) {
        return switch (pose) {
            case SLEEPING -> net.minecraft.world.entity.Pose.SLEEPING;
            case SWIMMING -> net.minecraft.world.entity.Pose.SWIMMING;
            case CROUCHING -> net.minecraft.world.entity.Pose.CROUCHING;
            case SITTING -> {
                seat = world.spawn(new Location(world, 0, 0, 0), ArmorStand.class);
                seat.setMarker(true);
                seat.setVisible(false);
                seat.teleport(spawnLoc);
                startRiding(((CraftArmorStand) seat).getHandle(), true);
                yield net.minecraft.world.entity.Pose.STANDING;
            }
            case DYING -> {
                setHealth(0.0F); // looks like dying
                yield net.minecraft.world.entity.Pose.DYING;
            }
            default -> net.minecraft.world.entity.Pose.STANDING;
        };
    }

    public double getPoseOffset(Pose pose) {
        return switch (pose) {
            case STANDING -> 0.20D;
            case SITTING -> 0.20D;
            case CROUCHING -> 0.175D;
            case SWIMMING -> 0.14D;
            case DYING -> 0.05D;
            case SLEEPING -> 0.10D;
        };
    }

    @Override
    public InternalNpc clone() {
        return new NPC_v1_20_R2(plugin, world, spawnLoc.clone(), equipment.clone(), settings.clone(), UUID.randomUUID(), target, new ArrayList<>(actions));
    }


    @Override
    public float getYaw() {
        return getYRot();
    }

    @Override
    public float getPitch() {
        return getXRot();
    }
}

