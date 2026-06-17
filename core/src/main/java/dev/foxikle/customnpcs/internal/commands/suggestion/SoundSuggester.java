/*
 * Copyright (c) 2024-2026. Foxikle
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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Keyed;
import org.bukkit.Registry;

import java.util.ArrayList;
import java.util.List;

public class SoundSuggester {

    private static final List<String> CACHED_SUGGESTIONS = new ArrayList<>();

    static {
        for (Keyed keyed : Registry.SOUNDS) {
            CACHED_SUGGESTIONS.add(keyed.key().toString());
        }
    }

    public static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = (context, builder) -> {
        String input = builder.getRemaining().toLowerCase();
        CACHED_SUGGESTIONS.stream()
                .filter(sound -> sound.toLowerCase().startsWith(input))
                .forEach(builder::suggest);
        return builder.buildFuture();
    };
}