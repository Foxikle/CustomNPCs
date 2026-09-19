package dev.foxikle.customnpcs.internal.utils;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class Runnable extends BukkitRunnable {

    private final Player player;
    private final CustomNPCs plugin;
    private final WaitingType type;


    public Runnable(Player player, CustomNPCs plugin, WaitingType type) {
        this.player = player;
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public void run() {
        if (!plugin.isWaiting(player, type)) this.cancel();
        player.showTitle(Title.title(
                Msg.get(player, "data." + type.i18nKey() + ".title"),
                Msg.get(player, "data." + type.i18nKey() + ".subtitle"),
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000L), Duration.ofMillis(0))
        ));
    }
}
