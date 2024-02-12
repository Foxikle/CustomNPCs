package dev.foxikle.customnpcs.internal.listeners;

import dev.foxikle.customnpcs.internal.menu.MenuCore;
import dev.foxikle.customnpcs.api.Action;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.InternalNpc;
import dev.foxikle.customnpcs.api.conditions.Conditional;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import java.util.regex.Pattern;
/**
 * The class that deals with misc listeners
 */
public class Listeners implements Listener {
	/**
	 * Player Movement Data that keeps track of old movements to replace PlayerMoveEvent
	 * @since *Insert_Version*
	 */
	private static final ConcurrentMap<UUID, MovementData> playerMovementData = new ConcurrentHashMap<>();
	
	// Helper Constants
	// since *Insert_Version*
	private static final int FIVE_BLOCKS = 25;
	private static final int FIFTY_BLOCKS = 2500; // 50 * 50
	private static final int FOURTY_BLOCKS = 2304; // 48 * 48
	private static final double HALF_BLOCK = 0.25;
	
	// Writing Constants
	// since *Insert_Version*
	private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
	
	private static final String SHOULD_UPDATE_MESSAGE =
		ChatColor.translateAlternateColorCodes('&', "&2&m----------------&r &6[&e!&6] &b&lCustomNPCs &6[&e!&6]  &2&m----------------\n&r&eA new update is available! I'd appreciate if you updated :) \n -&e&oFoxikle");
		
	private static final ConsoleCommandSender sender = Bukkit.getConsoleSender();
	
	private static final Pattern PATTERN = Pattern.compile(" ");
	
    /**
     * The instance of the main Class
     */
    private final CustomNPCs plugin;
    
    // Executors for better handling of async scheduling than that bukkit scheduler
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    //private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    /**
     * Constructor for generic listners class
     * @param plugin The instance of the main class
     */
    public Listeners(CustomNPCs plugin) {
        this.plugin = plugin;
        service.scheduleAtFixedRate(() -> Bukkit.getOnlinePlayers().forEach(this::actionPlayerMovement), 1000, 220, TimeUnit.MILLISECONDS);
    }
    
    public void stop() {
		service.shutdown();
		//executorService.shutdown();
		CompletableFuture.runAsync(() -> {
			try {
				if (/*!executorService.awaitTermination(2, TimeUnit.SECONDS) 
						|| */!service.awaitTermination(2, TimeUnit.SECONDS)) {
					//executorService.shutdownNow();
					service.shutdownNow();
				}
			} catch (InterruptedException e) {
				//executorService.shutdownNow();
				service.shutdownNow();
				Thread.currentThread().interrupt();
			}
		});
	}
    
    private final void actionPlayerMovement(Player player) {
		final Location location = player.getLocation();
		final World world = player.getWorld();
		
		final UUID uuid = player.getUniqueId();
		for (InternalNpc npc : plugin.npcs.values()) {
			if (npc.getTarget() != null) continue;
			
			World npcWorld = npc.getWorld();
			if (world != npcWorld) continue;
			if (npc.isTunnelVision()) continue;
			processPlayerMovement(player, npc, world, npcWorld, location, uuid);
		}
	}
    
    private final void processPlayerMovement(final Player player, 
    									final InternalNpc npc, 
    									final World world, 
    									final World npcWorld,
    									final Location location,
    									final UUID uuid) {
    	final Location npcLocation = npc.getCurrentLocation(); 
    	MovementData oldMovementData; // difference in order of initialization in if/else statement
        final MovementData movementData = playerMovementData.get(uuid);
        final double distanceSquared = location.distanceSquared(npcLocation);
        if (movementData == null) {
        	playerMovementData.put(uuid, new MovementData(uuid, location, distanceSquared));
        	movementData = playerMovementData.get(uuid);
        	oldMovementData = movementData;
        } else {
        	oldMovementData = movementData;
			movementData.setLastLocation(location);
			movementData.setDistanceSquared(distanceSquared);
		}
    	trackFromTo(player, npc, world, npcWorld, location, npcLocation, uuid, movementData, oldMovementData);
        if (distanceSquared > FIVE_BLOCKS) {
            Collection<Entity> entities = npcWorld.getNearbyEntities(location, 2.5, 2.5, 2.5);
            entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
            for (Entity en : entities) {
                if (!(en instanceof Player p)) continue;
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) p).getHandle(), EntityAnchorArgument.Anchor.EYES);
            }
            float direction = (float) npc.getFacingDirection();
            npc.setYBodyRot(direction);
            npc.setYRot(direction);
            npc.setYHeadRot(direction);
        }
    }
    
    private final void trackFromTo(Player player, 
    							   InternalNpc npc, 
    							   World world, 
    							   World npcWorld,
    							   Location location,
    							   Location npcLocation,
    							   UUID uuid, 
    							   MovementData data,
    							   MovementData oldData) {
    	if (data.distanceSquared <= FIVE_BLOCKS) {
            npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
        	return;
        } else if (oldData.distanceSquared >= FOURTY_BLOCKS && data.distanceSquared <= FIFTY_BLOCKS) {
            npc.injectPlayer(player);
        }
    }

    /**
     * <p>The npc interaction handler
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
    	Player player = e.getPlayer();
    	
        if (e.getHand() != EquipmentSlot.HAND) return; 
        if (e.getRightClicked().getType() != EntityType.PLAYER) return;
        Player rightClicked = (Player) e.getRightClicked();
        ServerPlayer sp = ((CraftPlayer) rightClicked).getHandle();
        InternalNpc npc;
        
        UUID uuid = sp.getUUID();
        try {
            npc = plugin.getNPCByID(uuid);
        } catch (IllegalArgumentException ignored){
            return;
        }
        if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
            player.performCommand("npc edit " + uuid);
        } else if (npc.isClickable()) {
            npc.getActions().forEach(action -> Bukkit.dispatchCommand(sender, action.getCommand(player)));
        }
    }

    /**
     * <p>The handler for text input
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
    	Player player = e.getPlayer();  
    	String message = e.getMessage();
    	MenuCore core = plugin.menuCores.get(player);
        if (plugin.commandWaiting.contains(player)) {
            plugin.commandWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(ChatColor.GREEN + "Successfully set command to be '" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message) + ChatColor.RESET + "" + ChatColor.GREEN + "'");
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getActionCustomizerMenu(action)));
        } else if (plugin.nameWaiting.contains(player)) {
            plugin.nameWaiting.remove(player);
            core.getNpc().setName(message);
            player.sendMessage(Component.text("Successfully set name to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getMainMenu()));
        } else if (plugin.targetWaiting.contains(player)) {

            Conditional conditional = plugin.editingConditionals.get(player);
            if (conditional.getType() == Conditional.Type.NUMERIC) {
                try {
                    Double.parseDouble(message);
                } catch (NumberFormatException ignored) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCannot parse the number '&f" + message + "&c'. Please try again."));
                    return;
                }
            }
            plugin.targetWaiting.remove(player);
            conditional.setTargetValue(message);
            player.sendMessage(ChatColor.translateAlternateColorCodes("&aSuccessfully set target to be '&r" + message + "&a'"));
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getConditionalCustomizerMenu(plugin.editingConditionals.get(player))));
        } else if (plugin.titleWaiting.contains(player)) {
            plugin.titleWaiting.remove(player);
            List<String> args = plugin.editingActions.get(player).getArgsCopy();
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.add(0, args.get(0));
            currentArgs.add(1, args.get(1));
            currentArgs.add(2, args.get(2));
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set title to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getActionCustomizerMenu(action)));
        } else if (plugin.messageWaiting.contains(player)) {
            plugin.messageWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set message to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getActionCustomizerMenu(action)));
        } else if (plugin.serverWaiting.contains(player)) {
            plugin.serverWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(ChatColor.GREEN + "Successfully set server to be '" + ChatColor.RESET +  message + ChatColor.RESET + "" + ChatColor.GREEN + "'");
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getActionCustomizerMenu(action)));
        } else if (plugin.actionbarWaiting.contains(player)) {
            plugin.actionbarWaiting.remove(player);
            Action action = plugin.editingActions.get(player);
            List<String> currentArgs = action.getArgs();
            currentArgs.clear();
            currentArgs.addAll(List.of(PATTERN.split(message)));
            player.sendMessage(Component.text("Successfully set actionbar to be '", NamedTextColor.GREEN).append(plugin.getMiniMessage().deserialize(message)).append(Component.text("'", NamedTextColor.GREEN)));
            SCHEDULER.runTask(plugin, () -> player.openInventory(core.getActionCustomizerMenu(action)));
        }
        e.setCancelled(true);
    }

    /**
     * <p>The npc injection handler on join
     * </p>
     * @param e The event callback
     * @since 1.3-pre5
     */
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
    	Player player = e.getPlayer();
        if (plugin.update && plugin.getConfig().getBoolean("AlertOnUpdate") && player.hasPermission("customnpcs.alert")) {
        	player.sendMessage(SHOULD_UPDATE_MESSAGE);
        }
        List<InternalNpc> npcs = plugin.getNPCs();
        for (InternalNpc npc : npcs) npc.injectPlayer(player);
        SCHEDULER.runTaskLater(plugin, () -> npcs.forEach(npc -> npc.injectPlayer(player)), 10);
    }

    /**
     * <p>The npc interaction handler while mounted on an entity
     * </p>
     * @param e The event callback
     * @since 1.3-pre4
     */
     
     /*
    @EventHandler
    public void onEntityMove(EntityMoveEvent e) {
        final Entity et = e.getEntity();
        List<Player> players = new ArrayList<>();
		et.getPassengers().forEach(entity1 -> {
			if(entity1 instanceof Player player) players.add(player);
		});
        executorService.submit(() -> players.forEach(this::actionPlayerMovement));
    }
    */

    /**
     * <p>The npc injection handler on velocity
     * </p>
     * @param e The event callback
     * @since 1.3-pre4
     */
    @EventHandler
    public void onVelocity(PlayerVelocityEvent e) {
        actionPlayerMovement(e.getPlayer());
    }

    /**
     * <p>The npc follow handler
     * TODO: Replace with proper Pathfinding.
     * </p>
     * @param e The event callback
     * @since 1.3-pre2
     */
    @EventHandler
    public void followHandler(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        Location location = player.getLocation();
        Location to = e.getTo();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (world != npc.getWorld()) continue; //TODO: Make npc travel between dimensions
            if (npc.getTarget() != player) continue;
            npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            if(npc.getCurrentLocation().distanceSquared(to) >= HALF_BLOCK){
                SCHEDULER.runTaskLater(plugin, () -> {
                    if (to.distanceSquared(location) >= 1) npc.moveTo(new Vec3(to.x(), to.y(), to.z()));
                }, 30);
            }
        }
    }

    /**
     * <p>The npc injection handler
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        Location location = player.getLocation();
        World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
        	Location spawnLocation = npc.getSpawnLoc();
            if (world != npc.getWorld()) return;
            
            double distanceSquared = location.distanceSquared(spawnLocation);
            if (distanceSquared <= FIVE_BLOCKS) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (distanceSquared >= FOURTY_BLOCKS && distanceSquared <= FIFTY_BLOCKS) {
                npc.injectPlayer(player);
            }
        }
    }


    /**
     * Logic for injecting NPCs on world changes
     * @param e Event callback
     */
    @EventHandler
    public void onDimentionChange(PlayerChangedWorldEvent e) {
    	Player player = e.getPlayer();
    	Location location = player.getLocation();
    	World world = player.getWorld();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (world != npc.getWorld()) continue; 
            if (location.distanceSquared(npc.getCurrentLocation()) <= FOURTY_BLOCKS) npc.injectPlayer(player);
        }
    }

    /**
     * <p>The npc leave message handler. Cancels the leave message.
     * </p>
     * @param e The event callback
     * @since 1.0
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
    	Player player = e.getPlayer();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (npc.getPlayer().getBukkitEntity().getPlayer() != player) continue;
            e.setQuitMessage("");
        }
        plugin.commandWaiting.remove(player);
     	plugin.nameWaiting.remove(player);
     	plugin.targetWaiting.remove(player);
     	plugin.titleWaiting.remove(player);
     	plugin.messageWaiting.remove(player);
     	plugin.serverWaiting.remove(player);
     	plugin.actionbarWaiting.remove(player);
    }

    /**
     * <p>The npc interaction handler
     * </p>
     * @param e The event callback
     * @since 1.2
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
    	Player player = e.getPlayer();
    	Location respawnLocation = e.getRespawnLocation();
        for (InternalNpc npc : plugin.npcs.values()) {
            if (!respawnLocation.distanceSquared(npc.getCurrentLocation()) <= FOURTY_BLOCKS) continue;
            npc.injectPlayer(player);
        }
    }
    
    private static class MovementData {
    	private final UUID uniqueId;
    	private Location lastLocation;
    	private double distanceSquared;
    	
    	MovementData(UUID uniqueId, Location lastLocation, double distanceSquared) {
    		this.uniqueId = uniqueId;
    		this.lastLocation = lastLocation;
    		this.distanceSquared = distanceSquared;
    	}
    	
    	public UUID getUniqueId() { return uniqueId; }
    	
    	public Location getLastLocation() { return lastLocation; }
    	
    	public double getDistanceSquared() { return distanceSquared; }
    	
    	public void setDistanceSquared(double distanceSquared) { this.distanceSquared = distanceSquared; }
    	
    	public void setLastLocation(Location location) { this.lastLocation = location; }
    	
    	public MovementData copy() { return new MovementData(uuid, location, distanceSquared); }
    }
}
