package dev.foxikle.customnpcs.internal.menu;

import me.flame.menus.builders.items.ItemBuilder;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.ActionResponse;
import org.bukkit.Material;


public class MenuItems {
    public static final MenuItem MENU_GLASS = ItemBuilder.of(Material.BLACK_STAINED_GLASS).buildItem((i, event) -> {
       event.setCancelled(true);
        return ActionResponse.DONE;
    });
}
