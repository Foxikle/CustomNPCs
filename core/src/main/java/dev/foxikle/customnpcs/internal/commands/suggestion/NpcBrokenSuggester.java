/*
 * Copyright (c) 2025-2026. Foxikle
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

package dev.foxikle.customnpcs.internal.commands.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.utils.BrokenReason;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class NpcBrokenSuggester {

    public static final SuggestionProvider<CommandSourceStack> WORLD = (_, builder) -> {
        CustomNPCs plugin = CustomNPCs.getInstance();
        String input = builder.getRemaining().toLowerCase();

        plugin.getStorageManager().getBrokenNPCs(BrokenReason.INVALID_WORLD)
                .keySet().stream()
                .map(UUID::toString)
                .filter(uuid -> uuid.toLowerCase().startsWith(input))
                .forEach(builder::suggest);

        return builder.buildFuture();
    };

    public static final SuggestionProvider<CommandSourceStack> LINES = (_, builder) -> {
        CustomNPCs plugin = CustomNPCs.getInstance();
        String input = builder.getRemaining().toLowerCase();

        plugin.getStorageManager().getBrokenNPCs(BrokenReason.EMPTY_LINES)
                .keySet().stream()
                .map(UUID::toString)
                .filter(uuid -> uuid.toLowerCase().startsWith(input))
                .forEach(builder::suggest);

        return builder.buildFuture();
    };
}