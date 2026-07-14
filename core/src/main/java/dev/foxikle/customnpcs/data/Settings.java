/*
 * Copyright (c) 2024-2026. Foxikle
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

package dev.foxikle.customnpcs.data;

import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.api.NPC;
import dev.foxikle.customnpcs.api.Pose;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.utils.Msg;
import dev.foxikle.customnpcs.internal.utils.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * A class holding the data for an NPC's settings
 */
@ToString
@Setter
@Getter
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class Settings {

    public static final Settings DEFAULT = new Settings(
            false, // interactable
            false, // tunnel vision
            true, // resilient
            CustomNPCs.INTERPOLATION_DURATION, // interpolation duration
            "", // skin value
            "", // skin signature
            Utils.list("An Unnamed NPC"),
            "", // skin name
            false, // hide interactable hologram
            "",
            Pose.STANDING,
            null, // custom hologram background
            false, // hide hologram background
            false // upside down
    );

    public static final Codec<Settings> CODEC = StructCodec.struct(
            "interactable", Codec.BOOLEAN, Settings::isInteractable,
            "tunnelvision", Codec.BOOLEAN, Settings::isTunnelvision,
            "resilient", Codec.BOOLEAN, Settings::isResilient,
            "interpolationDuration", Codec.INT.optional(CustomNPCs.INTERPOLATION_DURATION),
            Settings::getInterpolationDuration,
            "value", Codec.STRING.optional(""), Settings::getValue,
            "signature", Codec.STRING.optional(""), Settings::getSignature,
            "holograms", Codec.STRING.list(), Settings::getRawHolograms,
            "skinName", Codec.STRING, Settings::getSkinName,
            "hideClickableHologram", Codec.BOOLEAN, Settings::isHideClickableHologram,
            "customInteractionHologram", Codec.STRING.optional(""), Settings::getCustomInteractableHologram,
            "pose", Codec.Enum(Pose.class), Settings::getPose,
            "hologramBackground", Utils.COLOR_CODEC.optional(), Settings::getHologramBackground,
            "hideHologrambackground", Codec.BOOLEAN.optional(false), Settings::isHideBackgroundHologram,
            "upsideDown", Codec.BOOLEAN, Settings::isUpsideDown,
            Settings::new
    );


    boolean interactable;
    boolean tunnelvision;
    boolean resilient;
    int interpolationDuration;
    String value;
    String signature;
    @Getter(AccessLevel.NONE)
    List<String> holograms;
    String skinName;
    boolean hideClickableHologram;
    String customInteractableHologram;
    Pose pose;
    Color hologramBackground;
    boolean hideBackgroundHologram;
    boolean upsideDown;

    public Settings(boolean interactable, boolean tunnelvision, boolean resilient, int interpolationDuration,
                    String value, String signature, List<String> holograms, String skinName,
                    boolean hideClickableHologram, String customInteractableHologram, Pose pose,
                    Color hologramBackground, boolean hideBackgroundHologram, boolean upsideDown) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.interpolationDuration = interpolationDuration;
        this.value = value;
        this.signature = signature;
        this.holograms = new ArrayList<>(holograms);
        this.skinName = skinName;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.pose = pose;
        this.hologramBackground = hologramBackground;
        this.hideBackgroundHologram = hideBackgroundHologram;
        this.upsideDown = upsideDown;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "1.10")
    public Settings(boolean interactable, boolean tunnelvision, boolean resilient,
                    String value, String signature, String skinName, List<String> holograms,
                    String customInteractableHologram,

                    boolean hideClickableHologram, Pose pose,
                    boolean upsideDown) {
        this.interactable = interactable;
        this.tunnelvision = tunnelvision;
        this.resilient = resilient;
        this.interpolationDuration = DEFAULT.interpolationDuration;
        this.value = value;
        this.signature = signature;
        this.holograms = new ArrayList<>(holograms);
        this.skinName = skinName;
        this.hideClickableHologram = hideClickableHologram;
        this.customInteractableHologram = customInteractableHologram;
        this.pose = pose;
        this.hologramBackground = DEFAULT.hologramBackground;
        this.hideBackgroundHologram = DEFAULT.hideBackgroundHologram;
        this.upsideDown = upsideDown;
    }

    public static Builder builder() {
        return new Builder(DEFAULT);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public List<Component> getHolograms() {
        return holograms.stream().map(Msg::format).toList();
    }

    @ApiStatus.Internal
    public List<String> getRawHolograms() {
        return holograms;
    }

    public Settings setSkin(Player player) {
        String signature = null;
        String value = null;
        for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
            if (property.getName().equals("textures")) {
                signature = property.getSignature();
                value = property.getValue();
            }
        }
        if (signature == null || value == null) {
            throw new IllegalStateException("Player does not have a valid skin!");
        }
        this.signature = signature;
        this.value = value;
        this.skinName = player.getName() + "'s skin";
        return this;
    }

    public Settings setSkinData(String sig, String val, String name) {
        this.signature = sig;
        this.value = val;
        this.skinName = name;
        return this;
    }

    @SuppressWarnings("all")
    public Settings clone() {
        return new Settings(interactable, tunnelvision, resilient, interpolationDuration, value, signature, holograms,
                skinName, hideClickableHologram, customInteractableHologram, pose, hologramBackground,
                hideBackgroundHologram, upsideDown);
    }

    public static class Builder {

        private final Settings settings;

        private Builder(Settings root) {
            this.settings = root;
        }

        /**
         * Sets if this NPC should be interactable, and if the plugin should process actions, and show the clickable
         * hologram. Showing the hologram can be overridden with {@link Settings#setHideClickableHologram(boolean)}.
         *
         * @param interactable if this NPC should be interactable
         * @return this, for chaining.
         */
        public Builder interactable(boolean interactable) {
            settings.setInteractable(interactable);
            return this;
        }

        /**
         * Sets if this NPC has tunnel vision. If this NPC has tunnel vision, it will not look at nearby players.
         * To set the direction the NPC looks, there are 3 options.
         * <br>
         * {@link NPC#setFacing(float, float)} uses an arbitrary pitch and yaw
         * <br>
         * {@link NPC#lookAt(Location)} uses a point in the world to look at
         * <br>
         * {@link NPC#lookAt(Entity, boolean)} looks at some point on an entity. true corresponds
         * to the head of the entity, and false corresponds to the feet of the entity
         *
         * @param tunnelvision true to ignore nearby players, false otherwise.
         * @return this, for chaining.
         */
        public Builder tunnelvision(boolean tunnelvision) {
            settings.setTunnelvision(tunnelvision);
            return this;
        }

        /**
         * Sets if this NPC should persist on server restarts. It defaults to <strong>true</strong>. Setting this to
         * false
         * prevents the plugin from saving this npc.
         *
         * @param resilient true to save this npc, false to keep it as a temporary NPC.
         * @return this, for chaining.
         */
        public Builder resilient(boolean resilient) {
            settings.setResilient(resilient);
            return this;
        }

        /**
         * Sets the ticks this NPC should be interpolated for on move. This defaults to the value defined in the config,
         * but can be overridden for this NPC directly. The interpolation makes the nametags move more smoothly with the
         * NPC.
         *
         * @param duration the number of ticks to interpolate a movement over.
         * @return this, for chaining.
         */
        public Builder interpolationDuration(int duration) {
            settings.setInterpolationDuration(duration);
            return this;
        }

        /**
         * Sets if the "CLICK" hologram (default) should be hidden. If the NPC is not interactable, this setting
         * has no effect. This defaults to false.
         *
         * @param hideClickableHologram true to hide, false to show (default)
         * @return this, for chaining
         */
        public Builder hideClickableHologram(boolean hideClickableHologram) {
            settings.setHideClickableHologram(hideClickableHologram);
            return this;
        }

        /**
         * Allows the "CLICK" hologram (default) to be overridden from the value defined in the config.yml.
         *
         * @param customHologram the minimessage encoded component to replace the default with
         * @return this, for chaining
         */
        public Builder customInteractableHologram(String customHologram) {
            settings.setCustomInteractableHologram(customHologram);
            return this;
        }

        /**
         * Allows the "CLICK" hologram (default) to be overridden from the value defined in the config.yml.
         *
         * @param customHologram the component to replace the default with
         * @return this, for chaining
         */
        public Builder customInteractableHologram(Component customHologram) {
            settings.setCustomInteractableHologram(Msg.toMini(customHologram));
            return this;
        }

        /**
         * Allows the NPC's pose to be changed. Please note that some poses, like {@link Pose#SLEEPING} and
         * {@link Pose#SWIMMING} have very small hitboxes, and are hard and/or annoying to interact with.
         *
         * @param pose the {@link Pose} to use
         * @return this, for chaining.
         */
        public Builder pose(Pose pose) {
            settings.setPose(pose);
            return this;
        }

        @ApiStatus.Experimental
        public Builder hologramBackground(Color color) {
            settings.setHologramBackground(color);
            return this;
        }

        @ApiStatus.Experimental
        public Builder hideBackgroundHologram(boolean hideBackgroundHologram) {
            settings.setHideBackgroundHologram(hideBackgroundHologram);
            return this;
        }

        /**
         * Allows for setting the NPC's model upside down as if it was named "Dinnerbone" or "Grumm". Defaults to false.
         *
         * @param upsideDown true to make the model upsidedown, false for regular.
         * @return this, for chaining.
         */
        public Builder upsideDown(boolean upsideDown) {
            settings.setUpsideDown(upsideDown);
            return this;
        }

        /**
         * Sets the NPC's holograms. These components will be serialized into strings following the MiniMessage format
         * for storage. Index 0 corresponds to the top (first) line.
         *
         * @param holograms the collection of components
         */
        public Builder holograms(Component... holograms) {
            List<String> mini = new ArrayList<>();
            for (Component hologram : holograms) {
                mini.add(Msg.toMini(hologram));
            }
            settings.setHolograms(mini);
            return this;
        }

        /**
         * Sets the NPC's holograms. The plugin will parse these strings into components following the MiniMessage
         * format.
         * Index 0 corresponds to the top (first) line.
         *
         * @param holograms the collection of strings, optionally in minimessage format
         */
        public Builder rawHolograms(String... holograms) {
            settings.setHolograms(Utils.list(holograms));
            return this;
        }

        /**
         * Sets the skin data
         *
         * @param signature the skin signature
         * @param value     the skin value
         * @param skinName  the cosmetic name
         */
        public Builder skinData(String signature, String value, String skinName) {
            settings.setValue(value);
            settings.setSignature(signature);
            settings.setSkinName(skinName);
            return this;
        }

        /**
         * Sets the skin data
         *
         * @param signature the skin signature
         * @param value     the skin value
         */
        public Builder skinData(String signature, String value) {
            settings.setValue(value);
            settings.setSignature(signature);
            settings.setSkinName("API Skin");
            return this;
        }

        public Builder skin(Player player) {
            settings.setSkin(player);
            return this;
        }

        public Settings build() {
            return settings;
        }
    }
}
