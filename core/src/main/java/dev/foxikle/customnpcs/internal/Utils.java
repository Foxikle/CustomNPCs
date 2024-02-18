package dev.foxikle.customnpcs.internal;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Utils {
    @SafeVarargs
    public static <E> List<E> list(E... vararg) {
        return new ArrayList<>(List.of(vararg));
    }

    public static String style(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
