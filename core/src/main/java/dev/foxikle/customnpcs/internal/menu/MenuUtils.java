package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import me.flame.menus.builders.items.ItemBuilder;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.ActionResponse;
import me.flame.menus.menu.Menu;
import me.flame.menus.menu.PaginatedMenu;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides menu utilites
 */
public class MenuUtils {

    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;

    /**
     * <p> The constructor for the MenuUtils class
     * </p>
     *
     * @param plugin The instance of the Main class
     */
    public MenuUtils(CustomNPCs plugin) {
        this.plugin = plugin;
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
     * <p> Gets the Signature of a stored Skin
     * </p>
     *
     * @param name The name of the skin to get the value from.
     * @return The encoded signature of a skin
     */
    public String getSignature(String name) {
        return plugin.getConfig().getConfigurationSection("Skins").getString(name + ".signature");
    }

    /**
     * <p> Gets the list of inventories that display all of the available skins in the config.
     * </p>
     *
     * @return The list of inventories displaying the skin options
     */
    public PaginatedMenu getSkinCatalogue() {
        PaginatedMenu menu = Menu.builder().title("Select A Skin").rows(6).addAllModifiers()
                .nextPageItem(53, ItemBuilder.of(Material.ARROW).setName(ChatColor.YELLOW + "Next Page").buildItem())
                .previousPageItem(45, ItemBuilder.of(Material.ARROW).setName(ChatColor.YELLOW + "Next Page").buildItem())
                .pagination();
        menu.setDynamicSizing(true);
        menu.setItem(49, ItemBuilder.of(Material.BARRIER).setName(ChatColor.RED + "Go Back").buildItem((i, event) -> {
            Player player = event.getPlayer();
            plugin.menuCores.get(player).getMainMenu().open(player);
            return ActionResponse.DONE;
        }));

        menu.addItem(makeIcons());
        menu.getPageDecorator().fillBorders(MenuItems.MENU_GLASS);

        return menu;
    }

    /**
     * <p> Gets the items that represent skins
     * </p>
     *
     * @return The list of skins to put into an inventory
     */
    private List<MenuItem> makeIcons() {
        final FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Skins");
        Set<String> names = section.getKeys(false);
        List<MenuItem> returnme = new ArrayList<>();
        NamespacedKey key = new NamespacedKey(plugin, "SkinButton");
        for (String str : names) {
            String value = section.getString(str + ".value");
            returnme.add(getSkinIcon(key, str, str.replace("_", " "), ChatColor.AQUA, ChatColor.YELLOW, "The " + str.replace("_", " ") + " Skin", "", "Click to select!", value, section.getString(str + ".signature")));
        }
        return returnme;
    }

    /**
     * <p> Gets the icon for a skin
     * </p>
     *
     * @param key       the namespaced key to add data to the item
     * @param keyValue  The data to associate with the key
     * @param name      The name of the item
     * @param texture   The texture of the skull
     * @param loreColor The color to make the lore
     * @param nameColor The color to make the name
     * @param Ll1       The lore line 1
     * @param Ll2       The lore line 2
     * @param Ll3       The lore line 3
     * @return The encoded value of a skin
     */

    public MenuItem getSkinIcon(NamespacedKey key, String keyValue, String name, ChatColor nameColor, ChatColor loreColor, String Ll1, String Ll2, String Ll3, String... texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, keyValue);
        headMeta.setDisplayName(nameColor + name);
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4"), name);
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(getUrlFromBase64(texture[0]));
        } catch (MalformedURLException e) {
            plugin.getLogger().severe("An error occured whilst fetching player skin icon");
            e.printStackTrace();
        }
        profile.setTextures(textures);
        headMeta.setOwnerProfile(profile);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(loreColor + Ll1);
        lore.add(loreColor + Ll2);
        lore.add(loreColor + Ll3);
        headMeta.setLore(lore);
        head.setItemMeta(headMeta);

        return ItemBuilder.of(head).buildItem((i, event) -> {
            Player player = event.getPlayer();
            InternalNpc npc = plugin.menuCores.get(player).getNpc();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            npc.getSettings().setValue(texture[0]);
            npc.getSettings().setSignature(texture[1]);
            npc.getSettings().setSkinName(name);
            player.sendMessage(ChatColor.GREEN + "Skin changed to " + ChatColor.BOLD + name);
            plugin.pages.put(player, 0);
            player.closeInventory();
            plugin.menuCores.get(player).getMainMenu().open(player);
            return ActionResponse.DONE;
        });
    }

    /**
     * @param base64 the string encoded with base64 holding the skin data
     * @return the URL of the skin to mojang's servers
     * @throws MalformedURLException if the base64 does not contain a valid url
     */
    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        Matcher m = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(decoded);
        if (m.find()) {
            return new URL(m.group().replace("\"url\" : \"", "").replace("\"", ""));
        }
        throw new IllegalArgumentException("The value '" + base64 + "' is not valid!");
    }

}
