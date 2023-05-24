package dev.foxikle.customnpcs.listeners;

import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;

public class Listeners implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (e.getRightClicked().getType() == EntityType.PLAYER) {
                Player player = e.getPlayer();
                Player rightClicked = (Player) e.getRightClicked();
                ServerPlayer sp = ((CraftPlayer) rightClicked).getHandle();
                NPC npc = CustomNPCs.getInstance().getNPCByID(sp.getUUID());
                if (player.hasPermission("customnpcs.edit") && player.isSneaking()) {
                    player.performCommand("npc edit " + npc.getUUID());
                } else if (npc.isClickable()) {
                    if (npc.getPlayer() == sp) player.performCommand(npc.getCommand());
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (CustomNPCs.getInstance().waiting.contains(e.getPlayer())) {
            CustomNPCs.getInstance().waiting.remove(e.getPlayer());
            CustomNPCs.getInstance().menus.get(e.getPlayer()).getNpc().setCommand(e.getMessage());
            e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set command to be '" + e.getMessage().replace("/", "") + "'");
            Bukkit.getScheduler().runTask(CustomNPCs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    e.getPlayer().openInventory(CustomNPCs.getInstance().menus.get(e.getPlayer()).getMainMenu());
                }
            });
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
            for (NPC npc : CustomNPCs.getInstance().getNPCs()) {
                npc.injectPlayer(e.getPlayer());
            }
            Bukkit.getScheduler().runTaskLater(CustomNPCs.getInstance(), () ->{
                for (NPC npc : CustomNPCs.getInstance().getNPCs()) {
                    npc.injectPlayer(e.getPlayer());
                }
        }, 10);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if (getDistance(e.getPlayer().getLocation(), npc.getLocation()) <= 5) { // should be 5
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (getDistance(e.getFrom(), npc.getLocation()) >= 48 && getDistance(e.getTo(), npc.getLocation()) <= 48) {
                npc.injectPlayer(player);
            }
            if (getDistance(e.getPlayer().getLocation(), npc.getLocation()) > 5) {
                Collection<Entity> entities = npc.getWorld().getNearbyEntities(npc.getLocation(), 2.5, 2.5, 2.5);
                entities.removeIf(entity -> entity.getScoreboardTags().contains("NPC"));
                for (Entity en : entities) {
                    if (en.getType() == EntityType.PLAYER) {
                        return;
                    }
                }
                    npc.setYBodyRot((float) npc.getFacingDirection());
                    npc.setYRot((float) npc.getFacingDirection());
                    npc.setYHeadRot((float) npc.getFacingDirection());
            }
        }
    }
    @EventHandler
    public void onMove(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if (getDistance(e.getPlayer().getLocation(), npc.getLocation()) <= 5) {
                npc.lookAt(EntityAnchorArgument.Anchor.EYES, ((CraftPlayer) player).getHandle(), EntityAnchorArgument.Anchor.EYES);
            } else if (getDistance(e.getFrom(), npc.getLocation()) >= 48 && getDistance(e.getTo(), npc.getLocation()) <= 48) {
                npc.injectPlayer(player);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        for (NPC npc : CustomNPCs.getInstance().npcs.values()) {
            if(npc.getPlayer().getBukkitEntity().getPlayer() == e.getPlayer()){
                e.setQuitMessage("");
            }
        }
    }

    private double getDistance(Location loc1, Location loc2) {
        return loc2.distance(loc1);
    }
}
