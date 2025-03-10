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

package dev.foxikle.customnpcs.internal.proto;

import com.google.protobuf.ByteString;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.data.Settings;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.exceptions.IllegalWorldException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
@ApiStatus.Internal
public class ProtoWrapper {

    @ApiStatus.Internal
    public static byte[] toProto(InternalNpc internalNpc) {
        return toProtoNpc(internalNpc).toByteArray();
    }

    @ApiStatus.Internal
    public static NpcOuterClass.Npc toProtoNpc(InternalNpc npc) {
        return NpcOuterClass.Npc.newBuilder()
                .addAllActions(npc.getActions().stream().map(ProtoWrapper::toProtoAction).collect(Collectors.toList()))
                .setEquipment(toProtoEquipment(npc.getEquipment()))
                .setLocation(toProtoLocation(npc.getSpawnLoc()))
                .setSettings(toProtoSettings(npc.getSettings()))
                .setUuid(npc.getUniqueID().toString()).build();
    }

    // may throw runtime exception
    @ApiStatus.Internal
    public static InternalNpc fromProtoNpc(NpcOuterClass.Npc npc) {
        Location loc = fromProtoLocation(npc.getLocation());
        return CustomNPCs.getInstance().createNPC(
                loc.getWorld(),
                loc,
                fromProtoEquipment(npc.getEquipment()),
                fromProtoSettings(npc.getSettings()),
                UUID.fromString(npc.getUuid()),
                null,
                npc.getActionsList().stream().map(ProtoWrapper::fromProtoAction).toList()
        );
    }

    @ApiStatus.Internal
    @SneakyThrows
    public static InternalNpc fromProto(byte[] bytes) {
        return fromProtoNpc(NpcOuterClass.Npc.parseFrom(bytes));
    }

    @ApiStatus.Internal
    public static SettingsOuterClass.Settings toProtoSettings(Settings settings) {
        return SettingsOuterClass.Settings.newBuilder()
                .setClickable(settings.isInteractable())
                .setTunnelVision(settings.isTunnelvision())
                .setResilient(settings.isResilient())
                .setName(settings.getName())
                .setCustomHologram(settings.getCustomInteractableHologram())
                .setHideClickHologram(settings.isHideClickableHologram())
                .setSignature(settings.getSignature())
                .setValue(settings.getValue())
                .setSkinName(settings.getSkinName())
                .build();
    }

    @ApiStatus.Internal
    public static Settings fromProtoSettings(SettingsOuterClass.Settings settings) {
        return new Settings(
                settings.getClickable(),
                settings.getTunnelVision(),
                settings.getResilient(),
                settings.getValue(),
                settings.getSignature(),
                settings.getSkinName(),
                settings.getName(),
                settings.getCustomHologram(),
                settings.getHideClickHologram()
        );
    }

    @ApiStatus.Internal
    public static ItemOuterClass.Item toProtoItem(ItemStack item) {
        byte[] data;
        if (item == null || item.getType() == Material.AIR) {
            data = new byte[0];
        } else {
            data = item.ensureServerConversions().serializeAsBytes();
        }
        return ItemOuterClass.Item.newBuilder()
                .setSerialized(ByteString.copyFrom(data))
                .build();
    }

    @ApiStatus.Internal
    public static ItemStack fromProtoItem(ItemOuterClass.Item item) {
        if (item.getSerialized().toByteArray().length == 0) {
            return new ItemStack(Material.AIR);
        }
        return ItemStack.deserializeBytes(item.getSerialized().toByteArray());
    }

    @ApiStatus.Internal
    public static EquipmentOuterClass.Equipment toProtoEquipment(Equipment equipment) {
        return EquipmentOuterClass.Equipment.newBuilder().
                setMainHand(toProtoItem(equipment.getHand()))
                .setOffHand(toProtoItem(equipment.getOffhand()))
                .setHead(toProtoItem(equipment.getHead()))
                .setChest(toProtoItem(equipment.getChest()))
                .setLegs(toProtoItem(equipment.getLegs()))
                .setFeet(toProtoItem(equipment.getBoots()))
                .build();
    }

    @ApiStatus.Internal
    public static Equipment fromProtoEquipment(EquipmentOuterClass.Equipment equipment) {
        return new Equipment(
                fromProtoItem(equipment.getHead()),
                fromProtoItem(equipment.getChest()),
                fromProtoItem(equipment.getLegs()),
                fromProtoItem(equipment.getFeet()),
                fromProtoItem(equipment.getMainHand()),
                fromProtoItem(equipment.getOffHand())
        );
    }

    @ApiStatus.Internal
    public static ActionOuterClass.Action toProtoAction(Action action) {
        return ActionOuterClass.Action.newBuilder()
                .setSerializedData(action.serialize())
                .build();
    }

    @ApiStatus.Internal
    public static Action fromProtoAction(ActionOuterClass.Action action) {
        return Action.parse(action.getSerializedData());
    }

    @ApiStatus.Internal
    public static LocationOuterClass.Location toProtoLocation(Location location) {
        return LocationOuterClass.Location.newBuilder()
                .setWorld(location.getWorld().getName())
                .setX(location.getX())
                .setY(location.getY())
                .setZ(location.getZ())
                .setPitch(location.getPitch())
                .setYaw(location.getYaw())
                .build();
    }

    @ApiStatus.Internal
    public static Location fromProtoLocation(LocationOuterClass.Location location) {
        //todo: Test this :)
        if (Bukkit.getWorld(location.getWorld()) == null) {
            throw new IllegalWorldException("Invalid world: " + location.getWorld());
        }
        return new Location(
                Bukkit.getWorld(location.getWorld()),
                location.getX(), location.getY(),
                location.getZ(), location.getPitch(),
                location.getYaw()
        );
    }

    @ApiStatus.Internal
    public static byte[] serializeList(List<InternalNpc> list) {
        List<NpcOuterClass.Npc> npcs = list.stream().map(ProtoWrapper::toProtoNpc).toList();
        return NpcListOuterClass.NpcList.newBuilder()
                .addAllNpcs(npcs)
                .build().toByteArray();
    }

    @ApiStatus.Internal
    public static byte[] serializeProtoList(List<NpcOuterClass.Npc> list) {
        return NpcListOuterClass.NpcList.newBuilder()
                .addAllNpcs(list)
                .build().toByteArray();
    }

    @ApiStatus.Internal
    @SneakyThrows
    public static List<InternalNpc> deserializeList(byte[] bytes) {
        return deserializeProtoList(bytes).stream().map(ProtoWrapper::fromProtoNpc).collect(Collectors.toList());
    }

    @ApiStatus.Internal
    @SneakyThrows
    public static List<NpcOuterClass.Npc> deserializeProtoList(byte[] bytes) {
        NpcListOuterClass.NpcList list = NpcListOuterClass.NpcList.parseFrom(bytes);
        List<NpcOuterClass.Npc> npcs = list.getNpcsList();
        return new ArrayList<>(npcs);
    }
}
