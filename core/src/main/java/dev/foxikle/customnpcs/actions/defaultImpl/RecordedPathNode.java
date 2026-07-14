/*
 * Copyright (c) 2026. Foxikle
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

package dev.foxikle.customnpcs.actions.defaultImpl;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;

public record RecordedPathNode(int index, int dx, int dy, int dz, float yaw,
                               float pitch) implements Serializable {

    private static final long DX_MASK = 0x1FFFFFL; // 21 bits
    private static final long DY_MASK = 0x3FFFFFL; // 22 bits
    private static final long DZ_MASK = 0x1FFFFFL; // 21 bits

    public static final Codec<RecordedPathNode> CODEC = StructCodec.struct(
            "index", Codec.INT, RecordedPathNode::index,
            "pos", Codec.LONG, RecordedPathNode::packedPosition,
            "rot", Codec.INT, RecordedPathNode::packedRotation,
            RecordedPathNode::new
    );

    public RecordedPathNode(int timestamp, Location current, Location previous) {
        Location l = current.clone().subtract(previous);
        this(timestamp, (int) (l.x() * 1000), (int) (l.y() * 1000), (int) (l.z() * 1000), l.getYaw(), l.getPitch());
    }

    public RecordedPathNode(int time, long packedPos, int packedRot) {
        this(time, unpackX(packedPos), unpackY(packedPos), unpackZ(packedPos),
                unpackYaw(packedRot), unpackPitch(packedRot));
    }

    private static int unpackX(long packed) {
        return (int) (packed >> 43);
    }

    private static int unpackY(long packed) {
        return (int) (packed << 21 >> 42);
    }

    private static int unpackZ(long packed) {
        return (int) (packed << 43 >> 43);
    }

    public static float unpackYaw(int packed) {
        return ((packed >>> 16) & 0xFFFF) * 360f / 65535f;
    }

    public static float unpackPitch(int packed) {
        return ((packed & 0xFFFF) * 180f / 65535f) - 90f;
    }

    private static void checkRange(int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    value + " outside range [" + min + ", " + max + "]");
        }
    }

    public long packedPosition() {
        checkRange(dx, -(1 << 20), (1 << 20) - 1);
        checkRange(dy, -(1 << 21), (1 << 21) - 1);
        checkRange(dz, -(1 << 20), (1 << 20) - 1);

        return ((long) dx & DX_MASK) << 43 | ((long) dy & DY_MASK) << 21 | ((long) dz & DZ_MASK);
    }

    public int packedRotation() {
        int y = Math.round(((yaw % 360f + 360f) % 360f) * 65535f / 360f);
        int p = Math.round((pitch + 90f) * 65535f / 180f);
        return (y << 16) | (p & 0xFFFF);
    }

    public Location getDelta(World world) {
        return new Location(world, dx / 1000.0, dy / 1000.0, dz / 1000.0, yaw, pitch);
    }

    @Override
    public @NonNull String toString() {
        return String.format("Node{t=%d, dx=%.2f, dy=%.2f, dz=%.2f, yaw=%.1f, pitch=%.1f}", index,
                dx / 1000.0, dy / 1000.0, dz / 1000.0, yaw, pitch);
    }
}
