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

/*
 * I do not own this code. It was written by Minikloon, (Samuel).
 * I altered it to use Apapche's StringUtils instead of the Minestom one.
 * All credit goes to Minikloon. Thanks <3
 * https://gist.github.com/Minikloon/e6a7679d171b90dc4e0731db46d77c84
 */
package dev.foxikle.customnpcs.internal.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ComponentWrapper {
    public static List<Component> wrap(Component component, int length) {
        if (!(component instanceof TextComponent text)) {
            return Collections.singletonList(component);
        }

        List<Component> wrapped = new ArrayList<>();

        List<TextComponent> parts = flatten(text);
        Component currentLine = Component.empty();
        int lineLength = 0;
        for (int i = 0; i < parts.size(); i++) {
            TextComponent part = parts.get(i);
            Style style = part.style();
            String content = part.content();

            TextComponent nextPart = i == parts.size() - 1 ? null : parts.get(i + 1);
            boolean join = nextPart != null && (part.content().endsWith(" ") || nextPart.content().startsWith(" "));

            StringBuilder lineBuilder = new StringBuilder();

            String[] words = content.split(" ");
            Pattern delimiterPattern = Pattern.compile("\n"); // Regular expression to include delimiters
            words = Arrays.stream(words)
                    .flatMap(word -> Arrays.stream(split(word, delimiterPattern)))
                    .toArray(String[]::new);

            for (int j = 0; j < words.length; j++) {
                String word = words[j];
                boolean lastWord = j == words.length - 1;
                if (word.isEmpty()) continue;
                boolean isLongEnough = lineLength != 0 && lineLength + word.length() > length;
                int newLines = StringUtils.countMatches(word, '\n') + (isLongEnough ? 1 : 0);
                for (int k = 0; k < newLines; ++k) {
                    String endOfLine = lineBuilder.toString();

                    currentLine = currentLine.append(Component.text(endOfLine).style(style));
                    wrapped.add(currentLine);

                    lineLength = 0;
                    currentLine = Component.empty().style(style);
                    lineBuilder = new StringBuilder();
                }
                boolean addSpace = (!lastWord || join) && !word.endsWith("\n");
                String cleanWord = word.replace("\n", "");
                lineBuilder.append(cleanWord).append(addSpace ? " " : "");
                lineLength += word.length() + 1;
            }
            String endOfComponent = lineBuilder.toString();
            if (!endOfComponent.isEmpty()) {
                currentLine = currentLine.append(Component.text(endOfComponent).style(style));
            }
        }

        if (lineLength > 0) {
            wrapped.add(currentLine);
        }

        return wrapped;
    }

    private static List<TextComponent> flatten(TextComponent component) {
        List<TextComponent> flattened = new ArrayList<>();

        Style enforcedState = enforceStates(component.style());
        component = component.style(enforcedState);

        Stack<TextComponent> toCheck = new Stack<>();
        toCheck.add(component);

        while (!toCheck.empty()) {
            TextComponent parent = toCheck.pop();
            if (!parent.content().isEmpty()) {
                flattened.add(parent);
            }

            List<Component> reversed = new ArrayList<>(parent.children());
            Collections.reverse(reversed);

            for (Component child : reversed) {
                if (child instanceof TextComponent text) {
                    Style style = parent.style();
                    style = style.merge(child.style());
                    toCheck.add(text.style(style));
                } else {
                    toCheck.add(unsupported());
                }
            }
        }
        return flattened;
    }

    private static Style enforceStates(Style style) {
        Style.Builder builder = style.toBuilder();
        style.decorations().forEach((decoration, state) -> {
            if (state == TextDecoration.State.NOT_SET) {
                builder.decoration(decoration, false);
            }
        });
        return builder.build();
    }

    private static TextComponent unsupported() {
        return Component.text("!CANNOT WRAP!").color(NamedTextColor.DARK_RED);
    }

    private static String[] split(CharSequence input, Pattern pattern) {
        int matchCount = 0;
        int index = 0;
        ArrayList<String> matchList = new ArrayList<>();
        Matcher m = pattern.matcher(input);

        while (m.find()) {
            {
                if (index == 0 && index == m.start() && 0 == m.end()) {
                    // no empty leading substring included for zero-width match
                    // at the beginning of the input char sequence.
                    continue;
                }
                String match = input.subSequence(index, m.start()).toString();
                matchList.add(match);
                index = m.end();
                matchList.add(input.subSequence(m.start(), index).toString());

                ++matchCount;
            }
        }

        // If no match was found, return this
        if (index == 0) return new String[]{input.toString()};

        // Add remaining segment
        matchList.add(input.subSequence(index, input.length()).toString());

        // Construct result
        int resultSize = matchList.size();
        String[] result = new String[resultSize];
        return matchList.subList(0, resultSize).toArray(result);
    }
}