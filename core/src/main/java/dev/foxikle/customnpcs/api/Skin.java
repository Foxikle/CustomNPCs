package dev.foxikle.customnpcs.api;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record Skin(String signature, String value, String name) {

    public static final Skin EMPTY = new Skin("", "", "A Default Skin");

    public Skin(String signature, String value) {
        this(signature, value, "An API Skin");
    }

    public static final Codec<Skin> CODEC = StructCodec.struct(
            "signature", Codec.STRING, Skin::signature,
            "value", Codec.STRING, Skin::value,
            "name", Codec.STRING, Skin::name,
            Skin::new
    );
}
