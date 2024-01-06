package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
     * @param plugin The instance of the Main class
     */
    public MenuUtils(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    /**
     * <p> Gets the Value of a stored Skin
     * </p>
     * @param name The name of the skin to get the value from.
     * @return The encoded value of a skin
     */
    public String getValue(String name){
        return plugin.getConfig().getConfigurationSection("Skins").getString(name+".value");
    }

    /**
     * <p> Gets the Signature of a stored Skin
     * </p>
     * @param name The name of the skin to get the value from.
     * @return The encoded signature of a skin
     */
    public String getSignature(String name){
        return plugin.getConfig().getConfigurationSection("Skins").getString(name+".signature");
    }

    /**
     * <p> Gets the list of inventories that display all of the available skins in the config.
     * </p>
     * @return The list of inventories displaying the skin options
     */
    public List<Inventory> getCatalogueInventories() {
        List<Inventory> invs = new ArrayList<>();
        List<ItemStack> items = makeIcons();

        // Menu controls
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.displayName(Component.text("Next Page", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        NamespacedKey key = new NamespacedKey(plugin, "NoClickey");
        nextMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "next");
        next.setItemMeta(nextMeta);

        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.displayName(Component.text("Previous Page", NamedTextColor.YELLOW).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        prevMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "prev");
        prev.setItemMeta(prevMeta);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "close");
        close.setItemMeta(closeMeta);

        int pagesint = (int) Math.ceil(items.size() / 28.0);
        for (int i = 0; i < pagesint; i++) {
            Inventory inv = addBorder(Bukkit.createInventory(null, 54,   Component.text("        Select a Skin" + ChatColor.RESET + "   (" + (i + 1) + "/" + pagesint + ")", NamedTextColor.BLACK, TextDecoration.BOLD)));
            if (i < pagesint - 1) {
                inv.setItem(53, next);
            }
            if (i >= 1) {
                inv.setItem(45, prev);
            }
            inv.setItem(49, close);
            for (int x = 0; x < 28; x++) {
                if (!items.isEmpty()) {
                    inv.addItem(items.get(0));
                } else {
                    break;
                }
                items.remove(0);
            }
            invs.add(inv);
        }

        return invs;
    }

    /**
     * <p> Returns an inventory of with a border of grey stained glass panes.
     * </p>
     * @param inv The inventory to put a border in.
     * @return The inventory with a border.
     */
    public Inventory addBorder(Inventory inv) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "NoClickey");
        meta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, "PANE");
        meta.displayName(Component.empty());
        item.setItemMeta(meta);

        for (int x = 0; x < inv.getSize(); x++) {
            if ((x < 9 || x > inv.getSize() - 9 || x % 9 == 0 || (x + 1) % 9 == 0) && inv.getItem(x) == null) {
                inv.setItem(x, item);
            }
        }
        return inv;
    }

    /**
     * <p> Gets the items that represent skins
     * </p>
     * @return The list of skins to put into an inventory
     */
    private List<ItemStack> makeIcons(){
        final FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Skins");
        Set<String> names = section.getKeys(false);
        List<ItemStack> returnme = new ArrayList<>();
        NamespacedKey key = new NamespacedKey(plugin, "SkinButton");
        for(String str: names){
            String value = section.getString(str+".value");
            returnme.add(getSkinIcon(key, str, str.replace("_", " "), ChatColor.AQUA, ChatColor.YELLOW, "The " + str.replace("_", " ") + " Skin", "", "Click to select!", value));
        }
        return returnme;
    }

    /**
     * <p> Gets the icon for a skin
     * </p>
     * @param key the namespaced key to add data to the item
     * @param keyValue The data to associate with the key
     * @param name The name of the item
     * @param texture The texture of the skull
     * @param loreColor The color to make the lore
     * @param nameColor The color to make the name
     * @param Ll1 The lore line 1
     * @param Ll2 The lore line 2
     * @param Ll3 The lore line 3
     * @return The encoded value of a skin
     */

    public ItemStack getSkinIcon(NamespacedKey key, String keyValue, String name, ChatColor nameColor, ChatColor loreColor, String Ll1, String Ll2, String Ll3, String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, keyValue);
        headMeta.setDisplayName(nameColor + name);
        PlayerProfile profile =  Bukkit.createPlayerProfile(UUID.fromString("92864445-51c5-4c3b-9039-517c9927d1b4"), name);
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(getUrlFromBase64(texture));
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

        return head;
    }

    /**
     *
     * @param base64 the string encoded with base64 holding the skin data
     * @return the URL of the skin to mojang's servers
     * @throws MalformedURLException if the base64 does not contain a valid url
     */
    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        Matcher m = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(decoded);
        if(m.find()){
            return new URL(m.group().replace("\"url\" : \"", "").replace("\"", ""));
        }
        throw new IllegalArgumentException("The value '" +  base64 + "' is not valid!");
    }

}
