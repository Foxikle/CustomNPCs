package dev.foxikle.customnpcs.internal.translations;

import dev.foxikle.customnpcs.internal.utils.Msg;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@UtilityClass
public class Arg {

    public static TagResolver named(String name, Object value) {
        if (value instanceof ComponentLike comp) {
            value = Msg.toMini(comp.asComponent());
        }
        return Placeholder.parsed(name, String.valueOf(value));
    }

    public static TagResolver unparsed(Object value) {
        if (value instanceof ComponentLike comp) {
            value = Msg.toMini(comp.asComponent());
        }

        String type = switch (value) {
            case Integer _ -> "int";
            case Double _, Float _ -> "float";
            case String _ -> "str";
            case Boolean _ -> "bool";
            default -> "<unknown>";
        };


        return Placeholder.unparsed(type, String.valueOf(value));
    }

    public static TagResolver arg(Object value) {
        if (value instanceof ComponentLike comp) {
            value = Msg.toMini(comp.asComponent());
        }

        String type = switch (value) {
            case Integer _ -> "int";
            case Double _, Float _ -> "float";
            case String _ -> "str";
            case Boolean _ -> "bool";
            default -> "<unknown>";
        };


        return Placeholder.parsed(type, String.valueOf(value));
    }
}
