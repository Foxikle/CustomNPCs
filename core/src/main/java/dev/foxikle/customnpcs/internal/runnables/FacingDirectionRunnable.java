/*
 * Copyright (c) 2024. Foxikle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.foxikle.customnpcs.internal.runnables;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.utils.Msg;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class FacingDirectionRunnable extends BukkitRunnable {

    private final CustomNPCs plugin;
    private final Player player;

    public FacingDirectionRunnable(CustomNPCs plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void go(){
        runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void run() {
        if (!plugin.facingWaiting.contains(player.getUniqueId())) cancel();
        player.showTitle(Title.title(
                Msg.translated("customnpcs.data.facing_direction.title"),
                Msg.translated("customnpcs.data.facing_direction.subtitle"),
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000L), Duration.ofMillis(0))
        ));
    }
}
