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

package dev.foxikle.customnpcs.internal.menu;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.utils.Msg;
import io.github.mqzen.menus.base.Content;
import io.github.mqzen.menus.base.pagination.PageComponent;
import io.github.mqzen.menus.base.pagination.PageView;
import io.github.mqzen.menus.base.pagination.Pagination;
import io.github.mqzen.menus.misc.Capacity;
import io.github.mqzen.menus.misc.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Material.PLAYER_HEAD;

/**
 * Provides menu utilities
 */
public class MenuUtils {

    public static final String NPC_DELETE = "npc_delete";
    public static final String NPC_MAIN = "npc_main";
    public static final String NPC_EXTRA_SETTINGS = "npc_extra_settings";
    public static final String NPC_ACTIONS = "npc_actions";
    public static final String NPC_NEW_ACTION = "npc_new_action";
    public static final String NPC_EQUIPMENT = "npc_equipment";
    public static final String NPC_ACTION_CUSTOMIZER = "npc_action_customizer";
    public static final String NPC_CONDITION_CUSTOMIZER = "npc_condition_customizer";
    public static final String NPC_SKIN_CATALOG = "npc_skin_catalog";
    public static final String NPC_NEW_CONDITION = "npc_new_condition";
    public static final String NPC_CONDITIONS = "npc_conditions";
    public static final String NPC_SKIN = "npc_skin";
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;
    private Pagination catalog;

    /**
     * <p> The constructor for the MenuUtils class
     * </p>
     *
     * @param plugin The instance of the Main class
     */
    public MenuUtils(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public static Content.Builder actionBase(Action action) {
        return Content.builder(Capacity.ofRows(5))
                .apply(content -> content.fill(MenuItems.MENU_GLASS))
                .setButton(3, MenuItems.decrementDelay(action))
                .setButton(4, MenuItems.delayDisplay(action))
                .setButton(5, MenuItems.incrementDelay(action))
                .setButton(36, MenuItems.toAction())
                .setButton(40, MenuItems.saveAction(action))
                .setButton(44, MenuItems.editConditions());
    }

    /**
     * <p> Gets the Value of a stored Skin
     * </p>
     *
     * @param name The name of the skin to get the value from.
     * @return The encoded value of a skin
     */
    public String getValue(String name) {
        return plugin.getConfig().getConfigurationSection("Skins").getString(name + ".value");
    }

    /**
     * <p> Gets the list of inventories that display all of the available skins in the config.
     * </p>
     *
     * @return The list of inventories displaying the skin options
     */
    public Pagination getSkinCatalogue() {
        if (catalog != null) return catalog;
        catalog = Pagination.auto(plugin.getLotus())
                .creator(new SkinCatalog())
                .componentProvider(this::makeIcons)
                .build();
        return catalog;
    }

    /**
     * Refreshes the skin catalog
     *
     * @return {@summary A refreshed skin catalog}
     */
    public Pagination refreshCatalog() {
        catalog = null;
        return getSkinCatalogue();
    }

    /**
     * <p> Gets the items that represent skins
     * </p>
     *
     * @return The list of skins to put into an inventory
     */
    private List<PageComponent> makeIcons() {
        final FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Skins");
        Set<String> names = section.getKeys(false);
        List<PageComponent> buttons = new ArrayList<>();
        for (String str : names) {
            String value = section.getString(str + ".value");
            buttons.add(new SkinIcon(value, section.getString(str + ".signature"), str.replace("_", " "), plugin));
        }
        return buttons;
    }


    public static class SkinIcon implements PageComponent {
        private final String value;
        private final String signature;
        private final String name;
        private final CustomNPCs plugin;

        public SkinIcon(String value, String signature, String name, CustomNPCs plugin) {
            this.value = value;
            this.signature = signature;
            this.name = name;
            this.plugin = plugin;
        }

        @Override
        public ItemStack toItem() {
            return ItemBuilder.modern(PLAYER_HEAD).setDisplay(Component.text(name))
                    .setLore(
                            Msg.translate("customnpcs.menus.skin_catalog.items.icon.lore", name),
                            Component.empty(),
                            Msg.translated("customnpcs.items.click_to_select")
                    ).modifyMeta(SkullMeta.class, skullMeta -> {
                        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                        profile.setProperty(new ProfileProperty("textures", value));
                        skullMeta.setPlayerProfile(profile);
                    }).build();
        }

        @Override
        public void onClick(PageView pageView, InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
            if (npc == null) {
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                player.sendMessage(Msg.translated("customnpcs.error.npc-menu-expired"));
                return;
            }

            event.setCancelled(true);
            npc.getSettings().setSkinData(signature, value, name);
            player.sendMessage(Msg.translate("customnpcs.skins.changed_with_catalog", name));
            plugin.getLotus().openMenu(player, NPC_MAIN);
        }
    }
}
