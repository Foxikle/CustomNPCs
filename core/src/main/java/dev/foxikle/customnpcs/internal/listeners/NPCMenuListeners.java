package dev.foxikle.customnpcs.internal.listeners;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.internal.*;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.actions.conditions.LogicalConditional;
import dev.foxikle.customnpcs.actions.conditions.NumericConditional;
import dev.foxikle.customnpcs.internal.menu.MenuCore;
import dev.foxikle.customnpcs.internal.runnables.*;
import dev.foxikle.customnpcs.internal.interfaces.InternalNPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;
import static org.bukkit.Material.*;

/**
 * Handlers for clicks in Menus
 */
public class NPCMenuListeners implements Listener {
    /**
     * The instance of the main class
     */
    private final CustomNPCs plugin;

    /**
     * The map of MenuCores
     */
    private Map<Player, MenuCore> map;

    /**
     * Creates the handler for NPC menu clicks
     * @param plugin the main class instance
     */
    public NPCMenuListeners(CustomNPCs plugin){
        this.plugin = plugin;
        map = plugin.menuCores;
    }

    /**
     * <p>The generic handler npc menu clicks
     * </p>
     * @param event The callback event object
     * @since 1.3-pre5
     */
    @EventHandler
    public void OnInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        if(event.getAction() == InventoryAction.COLLECT_TO_CURSOR) return; // stop double-clicking
        NamespacedKey key = new NamespacedKey(plugin, "MenuButtonTag");
        ItemStack item = event.getCurrentItem();
        PersistentDataContainer persistentDataContainer = item.getItemMeta().getPersistentDataContainer();
        Player player = (Player) event.getWhoClicked();
        MenuCore mc = map.get(player);
        if(mc == null) return;
        InternalNPC npc = mc.getNpc();
        if(npc.getActions() == null) return;
        if (persistentDataContainer.get(key, PersistentDataType.STRING) != null) {
            if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("NameTag")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("direction")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("resilience")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("clickable")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("changeSkin")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("equipment")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("Confirm")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("Cancel")) {
            } else if (persistentDataContainer.get(key, PersistentDataType.STRING).equals("actions")) {
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SkinButton"))) {
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NoClickey"))) {
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "EquipmentInv"))) {
            String button = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "EquipmentInv"), PersistentDataType.STRING);
            event.setCancelled(true);
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "ActionInv"))) {
            String itemData = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "ActionInv"), PersistentDataType.STRING);
            if (itemData.equals("new_action")) {
            } else if (itemData.equals("go_back")) {
            } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SerializedAction"))){
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "ConditionInv"))) {
            String itemData = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "ConditionInv"), PersistentDataType.STRING);
            if (itemData.equals("new_condition")) {
            } else if (itemData.equals("change_mode")) {
            } else if (itemData.equals("go_back")) {
            } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "SerializedCondition"))){
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NewActionButton"))) {
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "NewConditionButton"))) {
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "CustomizeConditionalButton"))) {
            String itemData = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "CustomizeConditionalButton"), PersistentDataType.STRING);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = plugin.editingConditionals.get(player);
            Action action = plugin.editingActions.get(player);

            switch (itemData) {
                case "select_target_value" -> {}
                case "toggle_comparator" -> {}
                case "select_statistic" -> {}
                case "confirm" -> {}
                case "go_back" -> {}
            }
        } else if (persistentDataContainer.getKeys().contains(new NamespacedKey(plugin, "CustomizeActionButton"))) {
            String itemData = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "CustomizeActionButton"), PersistentDataType.STRING);
            Action action = plugin.editingActions.get(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            switch (Objects.requireNonNull(itemData)) {
                // RUN_COMMAND
                case "edit_command" -> {}
                // DISPLAY_TITLE
                case "increment_in" -> {}
                case "increment_stay" -> {}
                case "increment_out" -> {}
                case "decrement_in" -> {}
                case "decrement_stay" -> {}
                case "decrement_out" -> {}
                case "edit_title" -> {}

                //SEND_MESSAGE
                case "edit_message" ->{}

                // PLAY_SOUND  (pitch / volume / sound)
                case "edit_sound" -> {}
                case "increment_sound_pitch" -> {}
                case "increment_volume" -> {}
                case "decrement_sound_pitch" -> {}
                case "decrement_volume" -> {}

                // ACTION_BAR
                case "edit_actionbar" -> {}

                // TELEPORT
                case "increment_x" -> {}
                case "increment_y" -> {}
                case "increment_z" -> {}
                case "increment_yaw" -> {}
                case "increment_pitch" -> {}
                case "decrement_x" -> {}
                case "decrement_y" -> {}
                case "decrement_z" -> {}
                case "decrement_yaw" -> {}
                case "decrement_pitch" -> {}

                // SEND_TO_SERVER
                case "edit_server" -> {}

                // ADD_EFFECT
                case "decrement_duration" -> {}
                case "decrement_amplifier" -> {}
                case "increment_duration" -> {}
                case "increment_amplifier" -> {}
                case "edit_add_effect" -> {}
                case "toggle_hide_particles" -> {}

                //REMOVE_EFFECT
                case "edit_remove_effect" -> {}

                // GIVE_EXP

                case "increment_give_xp", "increment_remove_xp" -> {}
                case "decrement_give_xp", "decrement_remove_xp" -> {}
                case "edit_give_levels", "edit_remove_levels" -> {}

                // standard controls
                case "decrement_delay" -> {}
                case "increment_delay" -> {}
                case "go_back" -> {}
                case "edit_conditionals" -> {}
                case "confirm" -> {}
            }
        }
    }
}

