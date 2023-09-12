package dev.foxikle.customnpcs.ai;

import dev.foxikle.customnpcs.CustomNPCs;
import dev.foxikle.customnpcs.NPC;
import org.bukkit.*;
import org.patheloper.api.pathing.result.PathfinderResult;
import org.patheloper.mapping.bukkit.BukkitMapper;

import java.util.concurrent.CompletionStage;

public class Pathfinder {

    private final World world;
    private final CustomNPCs plugin;
    private final NPC npc;

    public Pathfinder(World world, CustomNPCs plugin, NPC npc) {
        this.world = world;
        this.plugin = plugin;
        this.npc = npc;
    }

    public World getWorld() {
        return world;
    }

    public void computepath(Location start, Location target){
        CompletionStage<PathfinderResult> pathfindingResult = plugin.pathfinder.findPath(BukkitMapper.toPathPosition(start), BukkitMapper.toPathPosition(target));
        // This is just a simple way to display the pathfinding result.
        pathfindingResult.thenAccept(result -> {
            if(result.successful()) {
                result.getPath().getPositions().forEach(position -> {
                    Location loc = BukkitMapper.toLocation(position);
                    npc.addPathNode(loc);
                    Particle.DustOptions options = new Particle.DustOptions(Color.fromRGB(255, 195, 0), 1.5F);
                    Bukkit.getOnlinePlayers().forEach(player -> player.spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 2,  options));
                });
            }
        });
    }
}
