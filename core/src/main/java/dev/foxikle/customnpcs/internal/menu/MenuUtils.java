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

package dev.foxikle.customnpcs.internal.menu;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.translations.Arg;
import dev.foxikle.customnpcs.internal.utils.ItemBuilder;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import studio.mevera.lotus.api.button.Button;
import studio.mevera.lotus.api.button.ClickAction;
import studio.mevera.lotus.api.content.Content;
import studio.mevera.lotus.api.content.ContentBuilder;
import studio.mevera.lotus.api.menu.MenuView;
import studio.mevera.lotus.api.slot.Capacity;
import studio.mevera.lotus.api.slot.Slot;
import studio.mevera.lotus.api.slot.SlotMask;
import studio.mevera.lotus.paper.api.pagination.Pagination;
import studio.mevera.lotus.paper.api.pagination.PaperPageLayout;

import java.util.*;

import static org.bukkit.Material.ARROW;
import static org.bukkit.Material.PLAYER_HEAD;


public class MenuUtils {

    public static final String NPC_DELETE = "npc_delete";
    public static final String NPC_DELETE_LINE = "npc_delete_line";
    public static final String NPC_MAIN = "npc_main";
    public static final String NPC_EXTRA_SETTINGS = "npc_extra_settings";
    public static final String NPC_ACTIONS = "npc_actions";
    public static final String NPC_NEW_ACTION = "npc_new_action";
    public static final String NPC_POSE = "npc_pose";
    public static final String NPC_EQUIPMENT = "npc_equipment";
    public static final String NPC_CONDITION_CUSTOMIZER = "npc_condition_customizer";
    public static final String NPC_SKIN_CATALOG = "npc_skin_catalog";
    public static final String NPC_NEW_CONDITION = "npc_new_condition";
    public static final String NPC_CONDITIONS = "npc_conditions";
    public static final String NPC_SKIN = "npc_skin";
    public static final String NPC_HOLOGRAMS = "npc_holograms";
    public static final String NPC_DELETE_ACTION = "npc_action_delete";
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;
    private final Map<String, Pagination<SkinIcon>> catalog = new HashMap<>();

    /**
     * <p> The constructor for the MenuUtils class
     * </p>
     *
     * @param plugin The instance of the Main class
     */
    public MenuUtils(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public static ContentBuilder actionBase(Action action, Player player) {
        return Content.builder(Capacity.ofRows(5))
                .fillAll(MenuItems.MENU_GLASS)
                .set(Slot.of(0), MenuItems.decrementDelay(action, player))
                .set(Slot.of(1), MenuItems.delayDisplay(action, player))
                .set(Slot.of(2), MenuItems.incrementDelay(action, player))
                .set(Slot.of(6), MenuItems.decrementCooldown(action, player))
                .set(Slot.of(7), MenuItems.cooldownDisplay(action, player))
                .set(Slot.of(8), MenuItems.incrementCooldown(action, player))
                .set(Slot.of(36), MenuItems.toAction(player))
                .set(Slot.of(40), MenuItems.saveAction(action, player))
                .set(Slot.of(44), MenuItems.editConditions(player));
    }

    public static Content invalidNpc(MenuView<Component, ?> view) {
        return Content.builder(view.capacity())
                .set(Slot.of(22), Button.clickable(
                        ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                                .displayName(Msg.get(view.viewer(), "menus.main.error.no_npc"))
                                .lore(Msg.lore(view.viewer().locale(), "menus.main.error.no_npc.lore"))
                                .build(),
                        (v, _) -> v.viewer().closeInventory()
                ))
                .build();
    }

    public String getValue(String name) {
        return plugin.getConfig().getConfigurationSection("Skins").getString(name + ".value");
    }

    public Pagination<SkinIcon> getSkinCatalogue(Locale locale) {
        String lang = locale.getLanguage();
        if (catalog.containsKey(lang)) {
            return catalog.get(lang);
        }

        Capacity capacity = Capacity.ofRows(6);

        PaperPageLayout<SkinIcon> layout = PaperPageLayout.<SkinIcon>builder(capacity)
                .title(_ -> Msg.get(locale, "menus.skin_catalog.title"))
                .nextButton(_ -> Button.of(ItemBuilder.of(ARROW).displayName(Msg.get(locale, "items.next_page")).build()))
                .previousButton(_ -> Button.of(ItemBuilder.of(ARROW).displayName(Msg.get(locale, "items.prev_page")).build()))
                .decorations(_ -> Content.builder(capacity)
                        .fillBorder(MenuItems.MENU_GLASS)
                        .set(Slot.of(49), MenuItems.toMain(locale))
                        .build())
                .fillMask(SlotMask.range(capacity, Slot.at(2, 2, capacity), Slot.at(5, 8, capacity)))
                .build();

        Pagination<SkinIcon> pag = Pagination.<SkinIcon>builder(NPC_SKIN_CATALOG)
                .layout(layout)
                .source(viewer -> makeIcons(locale))
                .renderer((icon, context) -> Button.clickable(icon.toItem(), icon.onClick()))
                .build();

        catalog.put(lang, pag);
        return catalog.get(lang);
    }

    public Pagination<SkinIcon> refreshCatalog(Locale locale) {
        catalog.remove(locale.getLanguage());
        return getSkinCatalogue(locale);
    }

    private List<SkinIcon> makeIcons(Locale locale) {
        final FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Skins");
        Set<String> names = section.getKeys(false);
        List<SkinIcon> buttons = new ArrayList<>();
        for (String str : names) {
            String value = section.getString(str + ".value");
            buttons.add(new SkinIcon(value, section.getString(str + ".signature"), str.replace("_", " "), plugin, locale));
        }
        return buttons;
    }

    public static class SkinIcon {
        private final String value;
        private final String signature;
        private final String name;
        private final CustomNPCs plugin;
        private final Locale locale;

        public SkinIcon(String value, String signature, String name, CustomNPCs plugin, Locale player) {
            this.value = value;
            this.signature = signature;
            this.name = name;
            this.plugin = plugin;
            this.locale = player;
        }

        public ItemStack toItem() {
            return ItemBuilder.of(PLAYER_HEAD)
                    .displayName(Msg.format("<yellow>" + name))
                    .lore(
                            Component.empty(),
                            Msg.get(locale, "items.click_to_select")
                    ).editMeta(SkullMeta.class, skullMeta -> {
                        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                        profile.setProperty(new ProfileProperty("textures", value));
                        skullMeta.setPlayerProfile(profile);
                    }).build();
        }


        public ClickAction onClick() {
            return (v, event) -> {
                Player player = (Player) event.getWhoClicked();
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                InternalNpc npc = plugin.getEditingNPCs().getIfPresent(player.getUniqueId());
                player.closeInventory();
                if (npc == null) {
                    player.sendMessage(Msg.get(player, "error.npc-menu-expired"));
                    return;
                }

                event.setCancelled(true);
                npc.getSettings().setSkinData(signature, value, name);
                player.sendMessage(Msg.get(player, "skins.changed_with_catalog", Arg.arg(name)));
                plugin.getLotus().openMenu(player, NPC_MAIN);
            };
        }
    }
}
