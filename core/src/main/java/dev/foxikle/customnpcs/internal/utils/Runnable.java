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
                Msg.format(player, "<tr:data.%s.title>", type.i18nKey()),
                Msg.format(player, "<tr:data.%s.subtitle>", type.i18nKey()),
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000L), Duration.ofMillis(0))
        ));
    }
}
