package dev.foxikle.customnpcs.internal.menu;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.actions.ActionType;
import dev.foxikle.customnpcs.actions.conditions.Conditional;
import dev.foxikle.customnpcs.actions.conditions.LogicalConditional;
import dev.foxikle.customnpcs.actions.conditions.NumericConditional;
import dev.foxikle.customnpcs.data.Equipment;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.Utils;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.runnables.*;
import lombok.Getter;
import me.flame.menus.builders.items.ItemBuilder;
import me.flame.menus.menu.Menu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.bukkit.Material.*;

/**
 * Handles menu creation
 */
public class MenuCore {

    private static final List<Field> fields = Stream.of(PotionEffectType.class.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())).toList();

    @Getter
    private final InternalNpc npc;
    private final CustomNPCs plugin;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    /**
     * <p> The constructor to make a menu factory
     * </p>
     *
     * @param npc    The NPC to edit
     * @param plugin The instance of the Main class
     */
    public MenuCore(InternalNpc npc, CustomNPCs plugin) {
        this.npc = npc;
        this.plugin = plugin;
    }

    /**
     * <p> Gets the main menu
     * </p>
     *
     * @return The Menu representing the Main NPC menu
     */
    public Menu getMainMenu() {
        List<Component> lore = new ArrayList<>();
        Menu menu = Menu.builder(CustomNPCs.MENUS).title("       Create a New NPC").rows(5).addAllModifiers().normal();

        ItemStack name = new ItemStack(Material.NAME_TAG);
        ItemMeta nameMeta = name.getItemMeta();
        nameMeta.displayName(Utils.mm("<aqua>Change Name"));
        lore.add(Utils.mm("<yellow>The current name is ").append(plugin.getMiniMessage().deserialize(npc.getSettings().getName())));
        nameMeta.lore(lore);
        name.setItemMeta(nameMeta);

        ItemStack equipment = new ItemStack(Material.ARMOR_STAND);
        ItemMeta handMeta = equipment.getItemMeta();
        handMeta.displayName(Utils.mm("<dark_green>Change Item"));
        lore.clear();
        Equipment equip = npc.getEquipment();
        lore.add(Utils.mm("<yellow>The current equipment is "));
        lore.add(Utils.mm("<yellow>Main Hand: " + equip.getHand().getType()));
        lore.add(Utils.mm("<yellow>Offhand: " + equip.getOffhand().getType()));
        lore.add(Utils.mm("<yellow>Helmet: " + equip.getHead().getType()));
        lore.add(Utils.mm("<yellow>Chestplate: " + equip.getChest().getType()));
        lore.add(Utils.mm("<yellow>Leggings: " + equip.getLegs().getType()));
        lore.add(Utils.mm("<yellow>Boots: " + equip.getBoots().getType()));

        handMeta.lore(lore);
        equipment.setItemMeta(handMeta);

        ItemStack directionItem = new ItemStack(Material.COMPASS);
        ItemMeta positionMeta = directionItem.getItemMeta();
        positionMeta.displayName(Utils.mm("<dark_green>Facing Direction"));
        double dir = npc.getSettings().getDirection();
        lore.clear();

        Map<Integer, Integer> highlightIndexMap = Map.of(
                180, 0,
                -135, 1,
                -90, 2,
                -45, 3,
                0, 4,
                45, 5,
                90, 6,
                135, 7
        );

        Component playerDirection = Utils.mm("<green>Player Direction");
        Component clickToChange = Utils.mm("<yellow>Click to change!");

        List<Component> directions = List.of(
                Utils.mm("<green>North"),
                Utils.mm("<green>North East"),
                Utils.mm("<green>East"),
                Utils.mm("<green>South East"),
                Utils.mm("<green>South"),
                Utils.mm("<green>South West"),
                Utils.mm("<green>West"),
                Utils.mm("<green>North West")
        );

        int highlightIndex = highlightIndexMap.getOrDefault((int) dir, -1);
        lore.add(Component.empty());

        for (int i = 0; i < directions.size(); i++) {
            Component direction = directions.get(i);
            if (i == highlightIndex) {
                direction = direction.color(NamedTextColor.DARK_AQUA);
                direction = Utils.mm("<dark_aqua>▸ ").append(direction);
            }
            lore.add(direction);
        }

        lore.add(playerDirection);
        lore.add(Component.empty());
        lore.add(clickToChange);

        positionMeta.lore(lore);
        directionItem.setItemMeta(positionMeta);

        ItemStack resilientItem = new ItemStack(Material.BELL);
        ItemMeta resilientMeta = resilientItem.getItemMeta();
        lore.clear();
        lore.add(npc.getSettings().isResilient() ? Utils.mm("<bold><green>RESILIENT") : Utils.mm("<bold><red>NOT RESILIENT"));
        resilientMeta.lore(lore);
        resilientMeta.displayName(Utils.mm("<dark_green>Change resilience"));
        resilientItem.setItemMeta(resilientMeta);

        ItemStack confirmButton = new ItemStack(Material.LIME_DYE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Utils.mm("<green><bold>CONFIRM"));
        confirmButton.setItemMeta(confirmMeta);

        ItemStack interactableButton;
        if (npc.getSettings().isInteractable()) {
            interactableButton = new ItemStack(Material.OAK_SAPLING);

            ItemStack actionsButton = new ItemStack(Material.RECOVERY_COMPASS);
            ItemMeta actionsButtonMeta = actionsButton.getItemMeta();
            actionsButtonMeta.displayName(Utils.mm("<dark_green>Change actions"));
            lore.clear();
            lore.add(Utils.mm("<yellow>The actions performed when "));
            lore.add(Utils.mm("<yellow>interacting with the npc. "));
            actionsButtonMeta.lore(lore);
            actionsButtonMeta.lore();
            actionsButton.setItemMeta(actionsButtonMeta);
            menu.setItem(34, ItemBuilder.of(actionsButton).clickable((player, event) -> {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                event.setCancelled(true);
                getActionMenu().open(player);
            }));
        } else {
            interactableButton = new ItemStack(Material.DEAD_BUSH);
        }
        ItemMeta clickableMeta = interactableButton.getItemMeta();
        clickableMeta.displayName(Utils.mm("<dark_green>Change interactability"));
        lore.clear();
        lore.add(npc.getSettings().isInteractable() ? Utils.mm("<bold><green>INTERACTABLE") : Utils.mm("<bold><red>NOT INTERACTABLE"));
        clickableMeta.lore(lore);
        interactableButton.setItemMeta(clickableMeta);


        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Utils.mm("<red><bold>CANCEL"));
        cancelButton.setItemMeta(cancelMeta);
        menu.setItem(13, ItemBuilder.of(plugin.getMenuUtils().getSkinIcon(new NamespacedKey(plugin, "nothing_lol_this_should_be_changed_tbh"), "", "Change Skin", ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, "Changes the NPC's skin", "The current skin is " + npc.getSettings().getSkinName(), "Click to change!", "ewogICJ0aW1lc3RhbXAiIDogMTY2OTY0NjQwMTY2MywKICAicHJvZmlsZUlkIiA6ICJmZTE0M2FhZTVmNGE0YTdiYjM4MzcxM2U1Mjg0YmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJKZWZveHk0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhZTI5MDRhMjg2Yjk1M2ZhYjhlY2U1MWQ2MmJmY2NiMzJjYjAyNzQ4ZjQ2N2MwMGJjMzE4ODU1OTgwNTA1OGIiCiAgICB9CiAgfQp9").getItemStack()).clickable((player, event) -> {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            getSkinMenu().open(player);
        }));
        menu.setItem(16, ItemBuilder.of(name).clickable((player, event) -> {
            plugin.nameWaiting.add(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            player.sendMessage(Utils.style("&aType the NPC name the chat."));

            if (plugin.getConfig().getBoolean("NameReferenceMessages")) {
                player.sendMessage(Utils.style("&eFor reference, the current NPC Name is: "));
                player.sendMessage(npc.getSettings().getName());
                player.sendMessage(Utils.style("&8&oThis message can be toggled in the config.yml!"));
            }

            new NameRunnable(player, plugin).runTaskTimer(plugin, 1, 15);
            player.closeInventory();
            event.setCancelled(true);
        }));
        menu.setItem(10, ItemBuilder.of(directionItem).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            double newDir = 0.0;
            if (event.isLeftClick()) {
                newDir = (dir + 225) % 360 - 180;
                if (dir == 135) {
                    newDir = player.getLocation().getYaw();
                }
            } else if (event.isRightClick()) {
                newDir = ((dir + 45) % 360) - 135;
                if (dir == 180) {
                    newDir = player.getLocation().getYaw();
                }
            }
            npc.getSettings().setDirection(newDir);
            getMainMenu().open(player);
        }));
        menu.setItem(22, ItemBuilder.of(resilientItem).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            player.sendMessage("§bThe NPC is now " + (npc.getSettings().isResilient() ? "§c§lNOT RESILIENT" : "§a§lRESILIENT"));
            npc.getSettings().setResilient(!npc.getSettings().isResilient());
            getMainMenu().open(player);
        }));
        menu.setItem(25, ItemBuilder.of(interactableButton).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            player.sendMessage("§bThe NPC is now " + (npc.getSettings().isInteractable() ? "§c§lNOT INTERACTABLE" : "§a§lINTERACTABLE"));
            npc.getSettings().setInteractable(!npc.getSettings().isInteractable());
            getMainMenu().open(player);
        }));
        menu.setItem(31, ItemBuilder.of(confirmButton).clickable((player, event) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1), 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1), 3);
            Bukkit.getScheduler().runTaskLater(plugin, npc::createNPC, 1);
            player.sendMessage(npc.getSettings().isResilient() ? "§aResilient NPC created!" : "§aTemporary NPC created!");
            player.closeInventory();
            event.setCancelled(true);
        }));
        menu.setItem(36, ItemBuilder.of(cancelButton).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
            player.sendMessage("§cNPC aborted.");
            player.closeInventory();
            event.setCancelled(true);
        }));
        menu.setItem(28, ItemBuilder.of(SPYGLASS).setName("§bToggle Vision Mode").setLore(npc.getSettings().isTunnelvision() ? "§a§lTUNNEL Visioned" : "§c§lNORMAL Visioned").clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            npc.getSettings().setTunnelvision(!npc.getSettings().isTunnelvision());
            getMainMenu().open(player);
        }));
        menu.setItem(19, ItemBuilder.of(equipment).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
            getEquipmentMenu().open(player);
        }));
        menu.setItem(8, ItemBuilder.of(COMPARATOR)
                .setName(Utils.style("&dEdit Additional Settings"))
                .clickable((player, event) -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                    getExtraSettingsMenu().open(player);
                }));
        // not a new NPC
        if (plugin.getNPCByID(npc.getUniqueID()) != null)
            menu.setItem(44, ItemBuilder.of(LAVA_BUCKET)
                    .setName(Utils.style("&4Delete NPC"))
                    .clickable((player, event) -> {
                        player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                        MenuUtils.getDeletionConfirmationMenu(plugin.getNPCByID(npc.getUniqueID()), menu).open(player);
                    }));

        menu.setItem(0, ItemBuilder.of(ENDER_EYE)
                .setName(Utils.style("&bSet Facing Direction"))
                .setLore("", Utils.style("&eThis option configures the"), Utils.style("&eNPC's YAW and PITCH."), Utils.style("&eThis sets the NPC's direction"), Utils.style("&eto 'Player Direction'."))
                .clickable((player, event) -> {
                    plugin.facingWaiting.add(player);
                    new FacingDirectionRunnable(plugin, player).go();
                    player.closeInventory();
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                }));

        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu displaying the NPC's current armor
     * </p>
     *
     * @return The Menu representing the Armor menu
     */
    public Menu getEquipmentMenu() {
        ItemStack helm = npc.getEquipment().getHead();
        ItemStack cp = npc.getEquipment().getChest();
        ItemStack legs = npc.getEquipment().getLegs();
        ItemStack boots = npc.getEquipment().getBoots();
        ItemStack hand = npc.getEquipment().getHand();
        ItemStack offhand = npc.getEquipment().getOffhand();
        Menu menu = Menu.builder(CustomNPCs.MENUS).rows(6).addModifier(me.flame.menus.modifiers.Modifier.DISABLE_ITEM_REMOVAL).title("      Edit NPC Equipment").normal();
        menu.getFiller().fill(MenuItems.MENU_GLASS);

        if (helm.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Utils.mm("<green>Empty Helmet Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a helmet to change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(13, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setHead(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set helmet slot to " + npc.getEquipment().getHead().getType());
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = helm.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(helm.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a helmet to change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            helm.setItemMeta(meta);
            menu.setItem(13, ItemBuilder.of(helm).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setHead(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset helmet slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setHead(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set helmet slot to " + npc.getEquipment().getHead().getType());
                    getEquipmentMenu().open(player);
                }
            }));
        }
        if (cp.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Utils.mm("<green>Empty Chestplate Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a chestplate to change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(22, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set chestplate slot to " + npc.getEquipment().getChest().getType());
                } else {
                    event.setCancelled(true);
                    if (event.getCursor().getType() == AIR) return;
                    player.sendMessage("§cThat is not a chestplate!");
                }
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = cp.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(cp.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a chestplate to change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            cp.setItemMeta(meta);
            menu.setItem(22, ItemBuilder.of(cp).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setChest(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset chestplate slot ");
                    getEquipmentMenu().open(player);
                    return;
                } else if (event.getCursor().getType().name().contains("CHESTPLATE")) {
                    npc.getEquipment().setChest(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set chestplate slot to " + npc.getEquipment().getChest().getType());
                    return;
                } else {
                    if (event.getCursor().getType() == AIR) return;
                }
                event.setCancelled(true);
                player.sendMessage("§cThat is not a chestplate!");
                getEquipmentMenu().open(player);
            }));
        }
        if (legs.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Utils.mm("<green>Empty Leggings Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a pair of leggings"));
            lore.add(Utils.mm("<yellow>to change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(31, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set leggings slot to " + npc.getEquipment().getLegs().getType());
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    player.sendMessage("§cThat is not a pair of leggings!");
                }
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = legs.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(legs.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a pair of leggings"));
            lore.add(Utils.mm("<yellow>to change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            legs.setItemMeta(meta);
            menu.setItem(31, ItemBuilder.of(legs).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setLegs(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset leggings slot ");
                    getEquipmentMenu().open(player);
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setLegs(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set leggings slot to " + npc.getEquipment().getLegs().getType());
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    player.sendMessage("§cThat is not a pair of leggings!");
                }
            }));
        }
        if (boots.getType().isAir()) {
            ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.values());
            meta.displayName(Utils.mm("<green>Empty Boots Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a pair of boots to "));
            lore.add(Utils.mm("<yellow>change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(40, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType().name().contains("BOOTS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set boots slot to " + npc.getEquipment().getBoots().getType());
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    player.sendMessage("§cThat is not a pair of boots!");
                }
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = boots.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(boots.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>a pair of boots to "));
            lore.add(Utils.mm("<yellow>change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            boots.setItemMeta(meta);
            menu.setItem(40, ItemBuilder.of(boots).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setBoots(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset boots slot ");
                } else if (event.getCursor().getType().name().contains("LEGGINGS")) {
                    npc.getEquipment().setBoots(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set boots slot to " + npc.getEquipment().getBoots().getType());
                } else {
                    if (event.getCursor().getType() == AIR)
                        event.setCancelled(true);
                    player.sendMessage("§cThat is not a pair of boots!");
                }
                getEquipmentMenu().open(player);
            }));
        }
        if (hand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Utils.mm("<green>Empty Hand Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>an item to change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(23, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setHand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set hand slot to " + npc.getEquipment().getHand().getType());
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = hand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(hand.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>an item to change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            hand.setItemMeta(meta);
            menu.setItem(23, ItemBuilder.of(hand).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setHand(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset hand slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setHand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getHand().getType());
                    getEquipmentMenu().open(player);
                }
            }));
        }
        if (offhand.getType().isAir()) {
            ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Utils.mm("<green>Empty Offhand Slot"));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>an item to change."));
            meta.lore(lore);
            item.setItemMeta(meta);
            menu.setItem(21, ItemBuilder.of(item).clickable((player, event) -> {
                if (event.getCursor().getType() == AIR) return;
                npc.getEquipment().setOffhand(event.getCursor().clone());
                event.getCursor().setAmount(0);
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getOffhand().getType());
                getEquipmentMenu().open(player);
            }));
        } else {
            ItemMeta meta = offhand.getItemMeta();
            List<Component> lore = new ArrayList<>();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_DYE);
            meta.displayName(Component.text(offhand.getType().toString(), NamedTextColor.GREEN));
            lore.add(Utils.mm("<yellow>Click this slot with"));
            lore.add(Utils.mm("<yellow>an item to change."));
            lore.add(Utils.mm("<red>Rick click to remove"));
            meta.lore(lore);
            offhand.setItemMeta(meta);

            menu.setItem(21, ItemBuilder.of(offhand).clickable((player, event) -> {
                if (event.isRightClick()) {
                    npc.getEquipment().setOffhand(new ItemStack(AIR));
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    player.sendMessage("§cSuccessfully reset offhand slot ");
                    getEquipmentMenu().open(player);
                } else {
                    if (event.getCursor().getType() == AIR) return;
                    npc.getEquipment().setOffhand(event.getCursor().clone());
                    event.getCursor().setAmount(0);
                    player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    player.sendMessage("§aSuccessfully set offhand slot to " + npc.getEquipment().getOffhand().getType());
                    getEquipmentMenu().open(player);
                }
            }));
        }

        menu.setItem(8, ItemBuilder.of(ARMOR_STAND)
                .setName("§bImport Player Armor")
                .setLore("§eImports your current armor to the NPC")
                .clickable((player, event) -> {
                    npc.getEquipment().importFromEntityEquipment(player.getEquipment());
                    getEquipmentMenu().open(player);
                }));
        menu.setItem(49, ItemBuilder.of(BARRIER).setName("§cClose").clickable((player, event) -> {
            getMainMenu().open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }));
        return menu;
    }

    /**
     * <p>Gets the menu displaying all current actions
     *
     * @return The Inventory representing the Actions menu
     */
    public Menu getActionMenu() {
        final String DELAY = Utils.style("&eDelay (ticks): &b%d");
        final String REMOVE = Utils.style("&cRight click to remove");
        final String EDIT = Utils.style("&eClick to edit!");

        Menu menu = Menu.builder(CustomNPCs.MENUS).title("          Edit NPC Actions").rows(6).addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);
        ItemBuilder builder;
        List<String> lore;
        for (Action action : npc.getActions()) {
            List<String> args = action.getArgsCopy();
            switch (action.getActionType()) {
                case DISPLAY_TITLE -> {
                    int fIn = Integer.parseInt(args.get(0));
                    int stay = Integer.parseInt(args.get(1));
                    int fOut = Integer.parseInt(args.get(2));
                    args.remove(0);
                    args.remove(0);
                    args.remove(0);
                    builder = ItemBuilder.of(OAK_SIGN).setName(Utils.style("&bDisplay Title"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current title is: '&r" + String.join(" ", args) + "&e'"),
                            Utils.style("&eFade in: &b" + fIn),
                            Utils.style("&eStay: &b" + stay),
                            Utils.style("&eFade out: &b" + fOut),
                            ""
                    );
                }
                case SEND_MESSAGE -> {
                    builder = ItemBuilder.of(PAPER).setName(Utils.style("&bSend Message"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current message is: '&r" + String.join(" ", args) + "&e'"),
                            ""
                    );
                }
                case PLAY_SOUND -> {
                    double pitch = Double.parseDouble(args.get(0));
                    double volume = Double.parseDouble(args.get(1));
                    args.remove(0);
                    args.remove(0);
                    builder = ItemBuilder.of(BELL).setName(Utils.style("&bPlay Sound"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current sound is: '&b" + String.join(" ", args) + "&e'"),
                            Utils.style("&ePitch: &b" + DECIMAL_FORMAT.format(pitch)),
                            Utils.style("&eVolume: &b" + DECIMAL_FORMAT.format(volume)),
                            ""
                    );
                }
                case RUN_COMMAND -> {
                    builder = ItemBuilder.of(ANVIL).setName(Utils.style("&bRun Command"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe command is: '&r/" + String.join(" ", args) + "&e'"),
                            ""
                    );
                }
                case ACTION_BAR -> {
                    builder = ItemBuilder.of(ANVIL).setName(Utils.style("&bSend Actionbar"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current actionbar is: '&r" + String.join(" ", args) + "&e'"),
                            ""
                    );
                }
                case TELEPORT -> {
                    int x = Integer.parseInt(args.get(0));
                    int y = Integer.parseInt(args.get(1));
                    int z = Integer.parseInt(args.get(2));
                    int pitch = Integer.parseInt(args.get(3));
                    int yaw = Integer.parseInt(args.get(4));
                    builder = ItemBuilder.of(ANVIL).setName(Utils.style("&bTeleport Player"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eX: &b" + x),
                            Utils.style("&eY: &b" + y),
                            Utils.style("&eZ: &b" + z),
                            Utils.style("&ePitch: &b" + pitch),
                            Utils.style("&eYaw: &b" + yaw),
                            ""
                    );
                }
                case GIVE_EXP -> {
                    builder = ItemBuilder.of(EXPERIENCE_BOTTLE).setName(Utils.style("&bGive Experience"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current EXP to give is: " + args.get(0) + (args.get(1).equalsIgnoreCase("true") ? " levels" : " points")),
                            ""
                    );
                }
                case REMOVE_EXP -> {
                    builder = ItemBuilder.of(GLASS_BOTTLE).setName(Utils.style("&bRemove Experience"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eThe current EXP to take is: " + args.get(0) + (args.get(1).equalsIgnoreCase("true") ? " levels" : " points")),
                            ""
                    );
                }
                case ADD_EFFECT -> {
                    builder = ItemBuilder.of(BREWING_STAND).setName(Utils.style("&bGive Effect"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eEffect: &b" + args.get(3)),
                            Utils.style("&eDuration: &b" + args.get(0)),
                            Utils.style("&eAmplifier: &b" + args.get(1)),
                            Utils.style("&eHide Particles: &b" + args.get(2)),
                            ""
                    );
                }
                case REMOVE_EFFECT -> {
                    builder = ItemBuilder.of(MILK_BUCKET).setName(Utils.style("&bRemove Effect"));
                    lore = Utils.list(
                            "",
                            Utils.style("&eEffect: &b" + args.get(0)),
                            ""
                    );
                }
                case SEND_TO_SERVER -> {
                    builder = ItemBuilder.of(GRASS_BLOCK).setName(Utils.style("&bSend To Bungeecord/Velocity Server"));
                    lore = Utils.list(
                            "",
                            Utils.style("&bServer: '&b" + String.join(" ", args) + "&b'"),
                            ""
                    );
                }
                case TOGGLE_FOLLOWING -> {
                    builder = ItemBuilder.of(LEAD).setName(Utils.style("&d&l[WIP]&r&e Toggle Following"));
                    lore = Utils.list(Utils.style("&4&lThis Action is currently broken."));
                }
                default -> {
                    builder = ItemBuilder.of(BEDROCK);
                    lore = Utils.list("");
                }
            }

            lore.add(REMOVE);
            if (action.getActionType().isEditable()) lore.add(EDIT);
            if (action.getActionType().isDelayable()) lore.set(0, String.format(DELAY, action.getDelay()));

            menu.addItem(builder.setLore(lore).clickable((player, event) -> {
                if (event.isRightClick()) {
                    player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                    npc.removeAction(action);
                    getActionMenu().open(player);
                } else if (event.isLeftClick()) {
                    if (action.getActionType().isEditable()) {
                        plugin.editingActions.put(player, action.clone());
                        plugin.originalEditingActions.put(player, action);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                        getActionCustomizerMenu(action).open(player);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
                        player.sendMessage(Utils.style("&cThis action cannot be edited!"));
                    }
                }
            }));
        }

        // Close Button
        menu.setItem(45, ItemBuilder.of(ARROW)
                .setName(Utils.style("&cGo Back"))
                .clickable((player, event) -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    getMainMenu().open(player);
                }));

        menu.addItem(ItemBuilder.of(LILY_PAD)
                .setName(Utils.style("&aNew Action"))
                .clickable((player, event) -> {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    getNewActionMenu().open(player);
                }));
        return menu;
    }

    /**
     * Gets the menu to customize an action's conditions
     *
     * @param action the action whose conditions are to be customized
     * @return the inventory to be displayed
     */
    public Menu getConditionMenu(Action action) {
        Menu menu = Menu.builder(CustomNPCs.MENUS).title("   Edit Action Conditionals").rows(4).addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);
        if (action.getConditionals() != null) {
            for (Conditional c : action.getConditionals()) {
                ItemStack item = new ItemStack(Material.BEDROCK);
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = new ArrayList<>();
                if (c.getType() == Conditional.Type.NUMERIC) {
                    item.setType(Material.POPPED_CHORUS_FRUIT);
                    meta.displayName(Utils.mm("<aqua>Numeric Condition"));
                } else if (c.getType() == Conditional.Type.LOGICAL) {
                    item.setType(Material.COMPARATOR);
                    meta.displayName(Utils.mm("<aqua>Logical Condition"));
                }
                lore.add(Component.empty());
                lore.add(Utils.mm("<yellow>Comparator: '<light_purple>" + c.getComparator().name() + "<yellow>'"));
                lore.add(Utils.mm("<yellow>Value: '<light_purple>" + c.getValue().name() + "<yellow>'"));
                lore.add(Utils.mm("<yellow>Target Value: '<light_purple>" + c.getTarget() + "<yellow>'"));
                lore.add(Component.empty());
                lore.add(Utils.mm("<red>Right Click to remove."));
                lore.add(Utils.mm("<yellow>Left Click to edit."));

                meta.lore(lore);
                item.setItemMeta(meta);
                menu.addItem(ItemBuilder.of(item).clickable((player, event) -> {

                    if (event.isRightClick()) {
                        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_HIT, 1, 1);
                        action.removeConditional(c);
                        getConditionMenu(action).open(player);
                        event.setCancelled(true);
                    } else {
                        plugin.editingConditionals.put(player, c.clone());
                        plugin.originalEditingConditionals.put(player, c);
                        getConditionalCustomizerMenu(c).open(player);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }

                }));
            }
        }
        List<Component> lore = new ArrayList<>();

        // Close Button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Utils.mm("<red><bold>GO BACK"));
        closeMeta.lore(lore);
        close.setItemMeta(closeMeta);
        lore.clear();
        menu.setItem(31, ItemBuilder.of(close).clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getActionCustomizerMenu(action).open(player);

        }));

        // Add New
        ItemStack newCondition = new ItemStack(Material.LILY_PAD);
        ItemMeta conditionMeta = newCondition.getItemMeta();
        conditionMeta.displayName(Utils.mm("<green><bold>New Condition"));
        newCondition.setItemMeta(conditionMeta);
        lore.clear();
        menu.addItem(ItemBuilder.of(newCondition).clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            getNewConditionMenu().open(player);

        }));

        // Change Mode
        ItemStack changeMode = new ItemStack(action.getMode() == Conditional.SelectionMode.ALL ? Material.GREEN_CANDLE : Material.RED_CANDLE);
        ItemMeta changeModeMeta = changeMode.getItemMeta();
        changeModeMeta.displayName(Utils.mm("<green><bold>Change Mode"));
        lore.add(action.getMode() == Conditional.SelectionMode.ALL ? Utils.mm("<yellow>Match ALL Conditions") : Utils.mm("<yellow>Match ONE Condition"));
        changeModeMeta.lore(lore);
        changeMode.setItemMeta(changeModeMeta);
        lore.clear();
        menu.setItem(35, ItemBuilder.of(changeMode).clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            action.setMode(action.getMode() == Conditional.SelectionMode.ALL ? Conditional.SelectionMode.ONE : Conditional.SelectionMode.ALL);
            getConditionMenu(action).open(player);

        }));
        return menu;
    }

    /**
     * <p>Gets the menu to customize an action
     * </p>
     *
     * @param action The Action to customize
     * @return The Inventory representing the action to customize
     */
    public Menu getActionCustomizerMenu(Action action) {
        Menu menu = Menu.builder(CustomNPCs.MENUS).addAllModifiers().rows(5).title("         Edit NPC Action").normal();
        List<String> incLore = List.of("§8Left Click to add 1", "§8Right Click to add 5", "§8Shift + Click to add 20");
        List<String> decLore = List.of("§8Left Click to remove 1", "§8Right Click to remove 5", "§8Shift + Click to remove 20");

        menu.setItem(3, ItemBuilder.of(RED_DYE).setName("§eDecrement Delay").setLore(decLore).clickable((player, event) -> {
            if (event.isShiftClick()) {
                if (!(action.getDelay() - 20 < 0)) {
                    action.setDelay(action.getDelay() - 20);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            } else if (event.isLeftClick()) {
                if (!(action.getDelay() - 1 < 0)) {
                    action.setDelay(action.getDelay() - 1);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            } else if (event.isRightClick()) {
                if (!(action.getDelay() - 5 < 0)) {
                    action.setDelay(action.getDelay() - 5);
                } else {
                    player.sendMessage("§cThe delay cannot be negative!");
                }
            }
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getActionCustomizerMenu(action).open(player);
        }));
        menu.setItem(4, ItemBuilder.of(CLOCK).setName("§eDelay Ticks: " + action.getDelay()).buildItem());
        menu.setItem(5, ItemBuilder.of(LIME_DYE).setName("§eIncrement Delay").setLore(incLore).clickable((player, event) -> {
            if (event.isShiftClick()) {
                action.setDelay(action.getDelay() + 20);
            } else if (event.isLeftClick()) {
                action.setDelay(action.getDelay() + 1);
            } else if (event.isRightClick()) {
                action.setDelay(action.getDelay() + 5);
            }
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getActionCustomizerMenu(action).open(player);
        }));
        menu.setItem(36, ItemBuilder.of(ARROW).setName("§6Go Back").clickable((player, event) -> {
            getActionMenu().open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        }));
        menu.setItem(44, ItemBuilder.of(COMPARATOR).setName("§cEdit Conditions").clickable((player, event) -> {
            getConditionMenu(action).open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);

        }));
        menu.setItem(40, ItemBuilder.of(LILY_PAD).setName("§aConfirm").clickable((player, event) -> {
            if (plugin.originalEditingActions.get(player) != null)
                npc.removeAction(plugin.originalEditingActions.remove(player));
            npc.addAction(action);
            getActionMenu().open(player);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        }));

        List<String> args = action.getArgsCopy();
        switch (action.getActionType()) {
            case RUN_COMMAND ->
                    menu.setItem(22, ItemBuilder.of(ANVIL).setName("§eClick to Edit Command").setLore("§e" + String.join(" ", args), "§eClick to change!").clickable((player, event) -> {
                        player.closeInventory();
                        plugin.commandWaiting.add(player);
                        new CommandRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }));
            case DISPLAY_TITLE -> {
                // Increments
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease fade in duration").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease display duration").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(14, ItemBuilder.of(LIME_DYE).setName("§eIncrease fade out duration").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease fade in duration").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease display duration").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 21) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 1) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 5) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(32, ItemBuilder.of(RED_DYE).setName("§eDecrease fade out duration").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(2)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(2)) - 20) < 1) {
                            action.getArgs().set(2, String.valueOf(1));
                        } else {
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(2)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(2)) - 1) < 1) {
                            action.getArgs().set(2, String.valueOf(1));
                        } else {
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(2)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(2)) - 5) < 1) {
                            action.getArgs().set(2, String.valueOf(1));
                        } else {
                            action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                // Displays
                String displayLore = "§8In ticks";
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eFade in: " + args.get(0)).setLore(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eDisplay time: " + args.get(1)).setLore(displayLore).buildItem());
                menu.setItem(23, ItemBuilder.of(CLOCK).setName("§eFade out: " + args.get(2)).setLore(displayLore).buildItem());
                menu.setItem(25, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).clickable((player, event) -> {
                    player.closeInventory();
                    plugin.titleWaiting.add(player);
                    new TitleRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case ADD_EFFECT -> {
                // Increments
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease effect duration").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease effect amplifier").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 20) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 1) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 255) {
                            player.sendMessage("§cThe amplifier cannot be greater than 255!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) + 5) > 255) {
                            action.getArgs().set(1, String.valueOf(255));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease effect duration").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe duration cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease effect amplifier").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 20) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 1) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(1)) == 1) {
                            player.sendMessage("§cThe amplifier cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(1)) - 5) < 1) {
                            action.getArgs().set(1, String.valueOf(1));
                        } else {
                            action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                // Displays
                String displayLore = "§8In ticks";
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eDuration: " + args.get(0)).setLore(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eAmplifier: " + args.get(1)).setLore(displayLore).buildItem());
                boolean particles = Boolean.parseBoolean(args.get(2));
                menu.setItem(23, ItemBuilder.of(particles ? GREEN_CANDLE : RED_CANDLE).setName("§eHide Particles: " + particles).clickable((player, event) -> {
                    action.getArgs().set(2, String.valueOf(!particles));
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));


                List<String> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if (!Objects.equals(action.getArgs().get(3), field.getName())) lore.add("§a" + field.getName());
                    else lore.add("§3▸ " + field.getName());
                });
                menu.setItem(25, ItemBuilder.of(POTION).setName("§eEffect to give").setLore(lore).addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS).clickable((player, event) -> {
                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(3));
                    if (event.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(3, effects.get(index + 1));
                        } else {
                            action.getArgs().set(3, effects.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            action.getArgs().set(3, effects.get(effects.size() - 1));
                        } else {
                            action.getArgs().set(3, effects.get(index - 1));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case REMOVE_EFFECT -> {
                List<String> lore = new ArrayList<>();
                fields.forEach(field -> {
                    if (!Objects.equals(action.getArgs().get(0), field.getName())) lore.add("§a" + field.getName());
                    else lore.add("§3▸ " + field.getName());
                });
                menu.setItem(22, ItemBuilder.of(POTION).setName("§eEffect to Remove").setLore(lore).addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS).clickable((player, event) -> {
                    List<String> effects = new ArrayList<>();
                    fields.forEach(field -> effects.add(field.getName()));

                    int index = effects.indexOf(action.getArgs().get(0));

                    if (event.isLeftClick()) {
                        if (effects.size() > (index + 1)) {
                            action.getArgs().set(0, effects.get(index + 1));
                        } else {
                            action.getArgs().set(0, effects.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            action.getArgs().set(0, effects.get(effects.size() - 1));
                        } else {
                            action.getArgs().set(0, effects.get(index - 1));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case GIVE_EXP -> {
                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§eIncrease xp").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eXp to give: " + args.get(0)).buildItem());
                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease xp").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                boolean levels = Boolean.parseBoolean(args.get(1));
                menu.setItem(24, ItemBuilder.of(levels ? GREEN_CANDLE : RED_CANDLE).setName("§eAwarding EXP " + (levels ? "Levels" : "Points")).setLore("§eClick to change!").clickable((player, event) -> {
                    action.getArgs().set(1, String.valueOf(!levels));
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case REMOVE_EXP -> {
                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§eIncrease xp").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eXp to remove: " + args.get(0)).buildItem());
                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease xp").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 20) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 1) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(0)) == 1) {
                            player.sendMessage("§cThe xp cannot be less than 1!");
                        } else if ((Integer.parseInt(action.getArgs().get(0)) - 5) < 1) {
                            action.getArgs().set(0, String.valueOf(1));
                        } else {
                            action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                boolean levels = Boolean.parseBoolean(args.get(1));
                menu.setItem(24, ItemBuilder.of(levels ? GREEN_CANDLE : RED_CANDLE).setName("§eRemoving EXP " + (levels ? "Levels" : "Points")).setLore("§eClick to change!").clickable((player, event) -> {
                    action.getArgs().set(1, String.valueOf(!levels));
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

            }
            case SEND_MESSAGE ->
                    menu.setItem(22, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).setLore("§eClick to change!").clickable((player, event) -> {

                        player.closeInventory();
                        plugin.messageWaiting.add(player);
                        new MessageRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }));
            case PLAY_SOUND -> {

                String smallIncLore = "§eClick to add .1";
                String smallDecLore = "§eClick to remove .1";

                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease pitch.").setLore(smallIncLore).clickable((player, event) -> {
                    action.getArgs().set(0, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(0)) + .1)));
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease Volume").setLore(smallIncLore).clickable((player, event) -> {
                    action.getArgs().set(1, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(1)) + .1)));
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease pitch").setLore(smallDecLore).clickable((player, event) -> {
                    if (event.isLeftClick()) {
                        if (Double.parseDouble(action.getArgs().get(0)) - .1 <= 0) {
                            player.sendMessage("§cThe pitch cannot be less than or equal 0!");
                        } else {
                            action.getArgs().set(0, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(0)) - .1)));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease volume").setLore(smallDecLore).clickable((player, event) -> {
                    if (event.isLeftClick()) {
                        if (Double.parseDouble(action.getArgs().get(1)) - .1 <= 0) {
                            player.sendMessage("§cThe volume cannot be less than or equal 0!");
                        } else {
                            action.getArgs().set(1, String.valueOf(DECIMAL_FORMAT.format(Double.parseDouble(action.getArgs().get(1)) - .1)));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));


                // Displays
                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§ePitch: " + args.get(0)).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eVolume: " + args.get(1)).buildItem());

                ItemStack sound = new ItemStack(Material.BELL);
                ItemMeta metaDisplaySound = sound.getItemMeta();
                metaDisplaySound.displayName(Utils.mm("<yellow>Sound: " + args.get(0)));
                sound.setItemMeta(metaDisplaySound);
                menu.setItem(24, ItemBuilder.of(OAK_HANGING_SIGN).setName("§eSound: " + args.get(2)).setLore("", "§eClick to change!").clickable((player, event) -> {
                    player.closeInventory();
                    plugin.soundWaiting.add(player);
                    new SoundRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case ACTION_BAR ->
                    menu.setItem(22, ItemBuilder.of(OAK_HANGING_SIGN).setName(String.join(" ", args)).clickable((player, event) -> {
                        player.closeInventory();
                        plugin.actionbarWaiting.add(player);
                        new ActionbarRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }));
            case TELEPORT -> {
                menu.setItem(10, ItemBuilder.of(LIME_DYE).setName("§eIncrease X Coordinate").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(11, ItemBuilder.of(LIME_DYE).setName("§e Increase Y Coordinate").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(12, ItemBuilder.of(LIME_DYE).setName("§eIncrease Z Coordinate").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) + 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(16, ItemBuilder.of(LIME_DYE).setName("§eIncrease Yaw").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 20) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) + 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 1) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) + 5) > 180) {
                            action.getArgs().set(3, String.valueOf(180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(14, ItemBuilder.of(LIME_DYE).setName("§eIncrease Pitch").setLore(incLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 20) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 1) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == 90) {
                            player.sendMessage("§cThe pitch cannot be greater than 90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) + 5) > 90) {
                            action.getArgs().set(4, String.valueOf(90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) + 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                //decrements
                menu.setItem(28, ItemBuilder.of(RED_DYE).setName("§eDecrease X Coordinate").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(0, String.valueOf(Integer.parseInt(action.getArgs().get(0)) - 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(29, ItemBuilder.of(RED_DYE).setName("§eDecrease Y Coordinate").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(1, String.valueOf(Integer.parseInt(action.getArgs().get(1)) - 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(30, ItemBuilder.of(RED_DYE).setName("§eDecrease Z Coordinate").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 20));
                    } else if (event.isLeftClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 1));
                    } else if (event.isRightClick()) {
                        action.getArgs().set(2, String.valueOf(Integer.parseInt(action.getArgs().get(2)) - 5));
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(34, ItemBuilder.of(RED_DYE).setName("§eDecrease Yaw").setLore(decLore).clickable((player, event) -> {
                    if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == 180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 20) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(3)) - 20));
                        }
                    } else if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 1) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(3)) == -180) {
                            player.sendMessage("§cThe yaw cannot be greater than 180!");
                        } else if ((Integer.parseInt(action.getArgs().get(3)) - 5) > -180) {
                            action.getArgs().set(3, String.valueOf(-180));
                        } else {
                            action.getArgs().set(3, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                menu.setItem(32, ItemBuilder.of(RED_DYE).setName("§eDecrease Pitch").setLore(decLore).clickable((player, event) -> {
                    if (event.isLeftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than -90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 1) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 1));
                        }
                    } else if (event.isRightClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than -90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 5) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 5));
                        }
                    } else if (event.isShiftClick()) {
                        if (Integer.parseInt(action.getArgs().get(4)) == -90) {
                            player.sendMessage("§cThe pitch cannot be less than -90!");
                        } else if ((Integer.parseInt(action.getArgs().get(4)) - 20) < -90) {
                            action.getArgs().set(4, String.valueOf(-90));
                        } else {
                            action.getArgs().set(4, String.valueOf(Integer.parseInt(action.getArgs().get(4)) - 20));
                        }
                    }
                    getActionCustomizerMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

                // Displays
                String displayLore = Utils.style("&8In blocks");

                menu.setItem(19, ItemBuilder.of(CLOCK).setName("§eX: §b" + args.get(0)).setLore(displayLore).buildItem());
                menu.setItem(20, ItemBuilder.of(CLOCK).setName("§eY: §b" + args.get(1)).setLore(displayLore).buildItem());
                menu.setItem(21, ItemBuilder.of(CLOCK).setName("§eZ: §b" + args.get(2)).setLore(displayLore).buildItem());

                menu.setItem(25, ItemBuilder.of(COMPASS).setName("§eYaw: §b" + args.get(3)).setLore(displayLore).buildItem());
                menu.setItem(23, ItemBuilder.of(COMPASS).setName("§ePitch: §b" + args.get(4)).setLore(displayLore).buildItem());
            }
            case SEND_TO_SERVER ->
                    menu.setItem(22, ItemBuilder.of(GRASS_BLOCK).setName("§eServer: " + String.join(" ", args)).clickable((player, event) -> {
                        player.closeInventory();
                        plugin.serverWaiting.add(player);
                        new ServerRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                        event.setCancelled(true);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                    }));
            case TOGGLE_FOLLOWING -> {
                npc.addAction(action);
                return getActionMenu();
            }
        }
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p> Gets the menu to customize an action
     * </p>
     *
     * @param conditional The Conditional to customize
     * @return The Inventory representing the conditional to customize
     */
    public Menu getConditionalCustomizerMenu(Conditional conditional) {
        Menu menu = Menu.builder(CustomNPCs.MENUS).rows(3).title("   Edit Action Conditional").addAllModifiers().normal();

        // Go back to actions menu
        menu.setItem(18, ItemBuilder.of(ARROW)
                .setName(Utils.style("&6Go Back"))
                .clickable((player, event) -> {
                    getNewConditionMenu().open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

        menu.setItem(22, ItemBuilder.of(LILY_PAD)
                .setName(Utils.style("&aConfirm"))
                .clickable((player, event) -> {
                    Action action = plugin.editingActions.get(player);
                    event.setCancelled(true);
                    if (plugin.originalEditingConditionals.get(player) != null)
                        action.removeConditional(plugin.originalEditingConditionals.remove(player));
                    action.addConditional(conditional);
                    getConditionMenu(action).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));

        switch (conditional.getType()) {
            case NUMERIC -> {
                 /* 1 button to edit message

                 # # # # # # # # #
                 # # C # T # S # #
                 # # # # # # # # #

                 - T = target value
                 - C = comparator
                 - S = Select Statistic
                 - # = empty space
                */
                ItemStack selectComparator = new ItemStack(Material.COMPARATOR);
                ItemMeta meta = selectComparator.getItemMeta();
                List<Component> lore = new ArrayList<>();
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if (conditional.getComparator() != c)
                        lore.add(Component.text(c.name(), NamedTextColor.GREEN));
                    else
                        lore.add(Utils.mm("<dark_aqua>▸ " + c.name()));
                }
                meta.displayName(Utils.mm("<yellow>Comparator"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                menu.setItem(11, ItemBuilder.of(selectComparator).clickable((player, event) -> {
                    List<Conditional.Comparator> comparators = List.of(Conditional.Comparator.values());
                    int index = comparators.indexOf(conditional.getComparator());
                    if (event.isLeftClick()) {
                        if (comparators.size() > (index + 1)) {
                            conditional.setComparator(comparators.get(index + 1));
                        } else {
                            conditional.setComparator(comparators.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setComparator(comparators.get(comparators.size() - 1));
                        } else {
                            conditional.setComparator(comparators.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.displayName(Utils.mm("<yellow>Select Target Value"));
                lore.add(Utils.mm("<yellow>The target value is '<aqua>" + conditional.getTarget() + "<yellow>'"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                menu.setItem(13, ItemBuilder.of(targetValue).clickable((player, event) -> {
                    player.closeInventory();
                    plugin.targetWaiting.add(player);
                    new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
                for (Conditional.Value v : Conditional.Value.values()) {
                    if (!v.isLogical()) {
                        if (conditional.getValue() != v)
                            lore.add(Component.text(v.name(), NamedTextColor.GREEN));
                        else
                            lore.add(Utils.mm("<dark_aqua>▸ " + v.name()));
                    }
                }
                statisticMeta.displayName(Utils.mm("<yellow>Statistic"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                statisticMeta.lore(lore);
                statistic.setItemMeta(statisticMeta);
                menu.setItem(15, ItemBuilder.of(statistic).clickable((player, event) -> {
                    List<Conditional.Value> statistics = new ArrayList<>();
                    for (Conditional.Value value : Conditional.Value.values()) {
                        if (!value.isLogical()) statistics.add(value);
                    }

                    int index = statistics.indexOf(conditional.getValue());
                    if (event.isLeftClick()) {
                        if (statistics.size() > (index + 1)) {
                            conditional.setValue(statistics.get(index + 1));
                        } else {
                            conditional.setValue(statistics.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setValue(statistics.get(statistics.size() - 1));
                        } else {
                            conditional.setValue(statistics.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
            case LOGICAL -> {
                 /* 1 button to edit message

                 # # # # # # # # #
                 # # C # T # S # #
                 # # # # # # # # #

                 - T = target value
                 - C = comparator
                 - S = Select Statistic
                 - # = empty space
                */
                ItemStack selectComparator = new ItemStack(Material.COMPARATOR);
                ItemMeta meta = selectComparator.getItemMeta();
                List<Component> lore = new ArrayList<>();
                for (Conditional.Comparator c : Conditional.Comparator.values()) {
                    if (c.isStrictlyLogical()) {
                        if (conditional.getComparator() != c)
                            lore.add(Component.text(c.name(), NamedTextColor.GREEN));
                        else
                            lore.add(Utils.mm("<dark_aqua>▸ " + c.name()));
                    }
                }
                meta.displayName(Utils.mm("<yellow>Comparator"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                meta.lore(lore);
                selectComparator.setItemMeta(meta);
                menu.setItem(11, ItemBuilder.of(selectComparator).clickable((player, event) -> {
                    List<Conditional.Comparator> comparators = new ArrayList<>();
                    for (Conditional.Comparator value : Conditional.Comparator.values()) {
                        if (value.isStrictlyLogical()) comparators.add(value);
                    }


                    int index = comparators.indexOf(conditional.getComparator());
                    if (event.isLeftClick()) {
                        if (comparators.size() > (index + 1)) {
                            conditional.setComparator(comparators.get(index + 1));
                        } else {
                            conditional.setComparator(comparators.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setComparator(comparators.get(comparators.size() - 1));
                        } else {
                            conditional.setComparator(comparators.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
                lore.clear();

                ItemStack targetValue = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta targetMeta = targetValue.getItemMeta();
                targetMeta.displayName(Utils.mm("<yellow>Select Target Value"));
                lore.add(Utils.mm("<yellow>The target value is '<aqua>" + conditional.getTarget() + "<yellow>'"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                targetMeta.lore(lore);
                targetValue.setItemMeta(targetMeta);
                menu.setItem(13, ItemBuilder.of(targetValue).clickable((player, event) -> {

                    player.closeInventory();
                    plugin.targetWaiting.add(player);
                    new TargetInputRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
                lore.clear();

                ItemStack statistic = new ItemStack(Material.COMPARATOR);
                ItemMeta statisticMeta = statistic.getItemMeta();
                for (Conditional.Value v : Conditional.Value.values()) {
                    if (v.isLogical()) {
                        if (conditional.getValue() != v)
                            lore.add(Component.text(v.name(), NamedTextColor.GREEN));
                        else
                            lore.add(Utils.mm("<dark_aqua>▸ " + v.name()));
                    }
                }
                statisticMeta.displayName(Utils.mm("<yellow>Statistic"));
                lore.add(Utils.mm("<yellow>Click to change!"));
                statisticMeta.lore(lore);
                statistic.setItemMeta(statisticMeta);
                menu.setItem(15, ItemBuilder.of(statistic).clickable((player, event) -> {
                    List<Conditional.Value> statistics = new ArrayList<>();

                    for (Conditional.Value value : Conditional.Value.values()) {
                        if (value.isLogical()) statistics.add(value);
                    }

                    int index = statistics.indexOf(conditional.getValue());
                    if (event.isLeftClick()) {
                        if (statistics.size() > (index + 1)) {
                            conditional.setValue(statistics.get(index + 1));
                        } else {
                            conditional.setValue(statistics.get(0));
                        }
                    } else if (event.isRightClick()) {
                        if (index == 0) {
                            conditional.setValue(statistics.get(statistics.size() - 1));
                        } else {
                            conditional.setValue(statistics.get(index - 1));
                        }
                    }
                    getConditionalCustomizerMenu(conditional).open(player);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }));
            }
        }
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     *
     * @return The Inventory representing the new Action menu
     */
    public Menu getNewActionMenu() {
        Menu menu = Menu.builder(CustomNPCs.MENUS).rows(4).addAllModifiers().title("          New NPC Action").normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);

        menu.setItem(27, ItemBuilder.of(ARROW).setName("§6Go Back").clickable((player, event) -> {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
            getActionMenu().open(player);
        }));

        menu.addItem(ItemBuilder.of(OAK_SIGN).setName("§bDisplay Title").setLore("§eDisplays a title for the player.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.DISPLAY_TITLE, Utils.list("10", "20", "10", "title!"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(PAPER).setName("§bSend Message").setLore("§eSends the player a message.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.SEND_MESSAGE, Utils.list("message", "to", "be", "sent"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(BELL).setName("§bPlay Sound").setLore("§ePlays a sound for the player.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.PLAY_SOUND, Utils.list("1", "1", Sound.UI_BUTTON_CLICK.name()), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(ANVIL).setName("§bRun Command").setLore("§eRuns a command as the player.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.RUN_COMMAND, Utils.list("command", "to", "be", "run"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(IRON_INGOT).setName("§bSend Actionbar").setLore("§eSends the player an actionbar.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.ACTION_BAR, Utils.list("actionbar", "to", "be", "sent"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(ENDER_PEARL).setName("§bTeleport Player").setLore("§eTeleports a player upon interacting.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.TELEPORT, Utils.list("0", "0", "0", "0", "0"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(GRASS_BLOCK).setName("§bSend To Bungeecord/Velocity Server").setLore("§eSends a player to a Bungeecord/Velocity", "§eserver upon interacting.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.SEND_TO_SERVER, Utils.list("server", "name"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(LEAD).setName("§bStart/Stop Following").setLore("§eToggles whether or not the", "§eNPC follows this player.", Utils.style("&4&lThis Action is currently broken.")).clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.TOGGLE_FOLLOWING, Collections.singletonList(npc.getUniqueID().toString()), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);
        }));

        menu.addItem(ItemBuilder.of(EXPERIENCE_BOTTLE).setName("§bGive Exp").setLore("§eGives the player exp.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.GIVE_EXP, Utils.list("0", "true"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(GLASS_BOTTLE).setName("§bRemove Exp").setLore("§eRemoves exp from the player.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.REMOVE_EXP, Utils.list("0", "true"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(BREWING_STAND).setName("§bGive Effect").setLore("§eGives an effect to the player.").clickable((player, event) -> {

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.ADD_EFFECT, Utils.list("1", "1", "true", "SPEED"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));

        menu.addItem(ItemBuilder.of(MILK_BUCKET).setName("§bRemove Effect").setLore("§eRemoves an effect from the player.").clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Action action = new Action(ActionType.REMOVE_EFFECT, Utils.list("SPEED"), 0, Conditional.SelectionMode.ONE, new ArrayList<>());
            if (!action.getActionType().isDuplicatable()) {
                AtomicBoolean shouldReturn = new AtomicBoolean(false);
                npc.getActions().forEach(a -> {
                    if (a.getActionType() == action.getActionType()) {
                        event.setCancelled(true);
                        shouldReturn.set(true);
                        player.sendMessage(Utils.mm("<red>This NPC already has this action!"));
                    }
                });
                if (shouldReturn.get()) return;
            }
            plugin.editingActions.put(player, action);
            getActionCustomizerMenu(action).open(player);

        }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * <p>Gets the menu to create a new action
     * </p>
     *
     * @return The Inventory representing the new Action menu
     */
    public Menu getNewConditionMenu() {
        Menu menu = Menu.builder(CustomNPCs.MENUS).title("       New Action Condition").rows(3).addAllModifiers().normal();
        menu.getFiller().fillBorders(MenuItems.MENU_GLASS);

        menu.setItem(18, ItemBuilder.of(ARROW).setName("§6Go Back").clickable((player, event) -> {
            getConditionMenu(plugin.editingActions.get(player)).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }));

        menu.addItem(ItemBuilder.of(POPPED_CHORUS_FRUIT).setName("§3Numeric Condition").setLore("§eCompares numbers.").clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = new NumericConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.EXP_LEVELS, 0.0);
            plugin.editingConditionals.put(player, conditional);
            getConditionalCustomizerMenu(conditional).open(player);
        }));

        menu.addItem(ItemBuilder.of(COMPARATOR).setName("§3Logical Condition").setLore("§eCompares things with", "§enumbered options.").clickable((player, event) -> {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            Conditional conditional = new LogicalConditional(Conditional.Comparator.EQUAL_TO, Conditional.Value.GAMEMODE, "SURVIVAL");
            plugin.editingConditionals.put(player, conditional);
            getConditionalCustomizerMenu(conditional).open(player);
        }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }

    /**
     * Gets the menu to set the NPC skins
     *
     * @return The menu to customize the NPC Skins
     */
    public Menu getSkinMenu() {
        Menu menu = Menu.builder(CustomNPCs.MENUS).title("     Edit NPC Skin").rows(3).addAllModifiers().normal();

        menu.setItem(18, ItemBuilder.of(ARROW).setName("§6Go Back").setLore("§eGo back to the main menu").clickable((player, event) -> {
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            getMainMenu().open(player);
        }));

        menu.setItem(11, ItemBuilder.of(ANVIL).setName("§bImport from Player").setLore("§eFetches a player's skin by name").clickable((player, event) -> {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            plugin.playerWaiting.add(player);
            new PlayerNameRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            event.setCancelled(true);
        }));

        menu.setItem(13, ItemBuilder.of(ARMOR_STAND).setName("§bBrowse Skin Catalogue").setLore("§eUse a preset skin").clickable((player, event) -> {
            plugin.skinCatalogue.open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }));

        menu.setItem(15, ItemBuilder.of(WRITABLE_BOOK).setName("§bImport from URL").setLore("§eFetches a skin from a URL").clickable((player, event) -> {
            player.closeInventory();
            plugin.urlWaiting.add(player);
            new UrlRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            event.setCancelled(true);
        }));

        menu.getFiller().fill(MenuItems.MENU_GLASS);

        return menu;
    }

    public Menu getExtraSettingsMenu() {
        Menu menu = Menu.builder(CustomNPCs.MENUS).title("Extra NPC Settings").rows(3).addAllModifiers().normal();
        boolean hideClickableTag = npc.getSettings().isHideClickableHologram();
        menu.setItem(12, ItemBuilder.of((hideClickableTag ? RED_CANDLE : GREEN_CANDLE))
                .setName(Utils.style("&bToggle Hologram Visibility"))
                .setLore("", Utils.style("&eThe Interactable Holograms is:"), Utils.style((hideClickableTag ? "&c&lHIDDEN" : "&a&lSHOWN")))
                .clickable((player, event) -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                    npc.getSettings().setHideClickableHologram(!hideClickableTag);
                    getExtraSettingsMenu().open(player);
                }));

        menu.setItem(14, ItemBuilder.of(NAME_TAG)
                .setName(Utils.style("&bChange NPC Clickable Hologram Text"))
                .setLore("", Utils.style("&eThis changes only THIS NPC's"), Utils.style("&einteractable hologram."))
                .clickable((player, event) -> {
                    plugin.hologramWaiting.add(player);
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                    player.closeInventory();
                    player.sendMessage(Utils.style("&aType the new hologram text in chat!"));
                    new InteractableHologramRunnable(player, plugin).runTaskTimer(plugin, 0, 10);
                }));

        menu.setItem(18, ItemBuilder.of(ARROW)
                .setName(Utils.style("&6Go Back"))
                .clickable((player, inventoryClickEvent) -> {
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1, 1);
                    getMainMenu().open(player);
                }));
        menu.getFiller().fill(MenuItems.MENU_GLASS);
        return menu;
    }
}
