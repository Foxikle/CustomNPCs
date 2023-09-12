package dev.foxikle.customnpcs.runnables;

import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NPCMovementRunnable extends BukkitRunnable {
    private final NPC npc;
    private final CustomNPCs plugin;

    /**
     * <p> Creates a runnable for collecting text input for the npc name
     * </p>
     * @param plugin The instance to get who's waiting for the title
     * @param npc The player to display the title to
     */
    public NPCMovementRunnable(NPC npc, CustomNPCs plugin){
        this.npc = npc;
        this.plugin = plugin;
    }

    /**
     * <p> Repeatedly sends a title to the player with instructions for entering text
     * </p>
     */
    @Override
    public void run() {
        if(npc.getTarget() == null) {
            plugin.getLogger().warning("Canceling task!");
            this.cancel();
            return;
        }
        if(npc.pathNodes.size() <= 5)
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> npc.getPathfinder().computepath(npc.getCurrentLocation(), npc.getTarget().getLocation()));

        if (!npc.pathNodes.isEmpty()) {
            Location targetLocation = npc.pathNodes.get(0);
            npc.pathNodes.remove(0);

            // Calculate direction and move the NPC
            Vector direction = targetLocation.toVector().subtract(npc.getCurrentLocation().toVector()).normalize();
            double desiredSpeed = 8;
            double distanceToTravel = desiredSpeed / 20.0; // Minecraft ticks per second is 20
            Vec3 vec = new Vec3(direction.multiply(distanceToTravel).getX(), direction.multiply(distanceToTravel).getY(), direction.multiply(distanceToTravel).getZ());
            //npc.setDeltaMovement(vec);
            //npc.travel(vec);
            npc.moveRelative(10, vec);
            // Check if the NPC has reached the target location
        } else {
                Bukkit.getScheduler().runTask(plugin, () -> npc.getPathfinder().computepath(npc.getCurrentLocation(), npc.getTarget().getLocation()));
        }

    }
}
