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

public class MenuUtils {
    
    private final CustomNPCs plugin;

    public MenuUtils(CustomNPCs plugin) {
        this.plugin = plugin;
    }

    public  String getValue(String name){
        return plugin.getConfig().getConfigurationSection("Skins").getString(name+".value");
    }

    public  String getSignature(String name){
        return plugin.getConfig().getConfigurationSection("Skins").getString(name+".signature");
    }

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

    public static ItemStack getSkinIcon(NamespacedKey key, String keyName, String name, ChatColor nameColor, ChatColor loreColor, String Ll1, String Ll2, String Ll3, String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta headMeta = head.getItemMeta();
        headMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, keyName);
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
