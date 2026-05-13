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

package dev.foxikle.customnpcs.internal.translations;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class Translations {
    public static final Locale VIETNAMESE = new Locale("vi");
    public static final Locale RUSSIAN = new Locale("ru");
    TranslationStore<Component> registry = TranslationStore.component(Key.key("customnpcs:localization"));

    public void setup() {
        List.of(
                ResourceBundle.getBundle("localization.Chinese", Locale.SIMPLIFIED_CHINESE),
                ResourceBundle.getBundle("localization.Russian", RUSSIAN),
                ResourceBundle.getBundle("localization.German", Locale.GERMAN),
                ResourceBundle.getBundle("localization.English", Locale.US),
                ResourceBundle.getBundle("localization.Vietnamese", VIETNAMESE)
        ).forEach(b -> registry.registerAll(b.getLocale(), b.keySet(), s -> Component.translatable(b.getString(s))));

        registry.defaultLocale(Locale.ENGLISH);
        GlobalTranslator.translator().addSource(registry);
    }
}
