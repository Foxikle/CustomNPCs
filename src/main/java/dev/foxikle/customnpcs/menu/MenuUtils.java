package dev.foxikle.customnpcs.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.foxikle.customnpcs.CustomNPCs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('§', "§eNext Page"));
        NamespacedKey key = new NamespacedKey(plugin, "NoClickey");
        nextMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "next");
        next.setItemMeta(nextMeta);

        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.translateAlternateColorCodes('§', "§ePrevious Page"));
        prevMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "prev");
        prev.setItemMeta(prevMeta);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('§', "§c§lClose"));
        closeMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "close");
        close.setItemMeta(closeMeta);

        int pagesint = (int) Math.ceil(items.size() / 28.0);
        for (int i = 0; i < pagesint; i++) {
            Inventory inv = addBorder(Bukkit.createInventory(null, 54,   ChatColor.BLACK + "" + ChatColor.BOLD + "     Select a Skin" + ChatColor.RESET + "        (" + (i + 1) + "/" + pagesint + ")"));
            if (i < pagesint - 1) {
                inv.setItem(53, next);
            }
            if (i >= 1) {
                inv.setItem(45, prev);
            }
            inv.setItem(49, close);
            for (int x = 0; x < 28; x++) {
                if (items.size() >= 1) {
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
        List<String> lore = new ArrayList<>();
        NamespacedKey key = new NamespacedKey(plugin, "NoClickey");
        meta.getCustomTagContainer().setCustomTag(key, ItemTagType.STRING, "PANE");
        meta.setDisplayName(" ");
        lore.add("");
        meta.setLore(lore);
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

        ItemMeta headMeta = head.getItemMeta();
        headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, keyValue);
        headMeta.setDisplayName(nameColor + name);

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {

        }
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(loreColor + Ll1);
        lore.add(loreColor + Ll2);
        lore.add(loreColor + Ll3);
        headMeta.setLore(lore);
        head.setItemMeta(headMeta);

        return head;
    }

}
