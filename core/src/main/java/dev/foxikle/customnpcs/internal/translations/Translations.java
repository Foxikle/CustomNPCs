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
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Translations {
    public static final Locale VIETNAMESE = new Locale("vi");
    public static final Locale RUSSIAN = new Locale("ru");
    private static final TranslationStore<MessageFormat> STORE = TranslationStore.messageFormat(Key.key("customnpcs" +
            ":root"));
    private static final Map<Locale, ResourceBundle> BUNDLES = new HashMap<>();
    private static boolean setup = false;

    public void setup() {
        if (setup) return;
        BUNDLES.put(Locale.SIMPLIFIED_CHINESE, ResourceBundle.getBundle("localization.Chinese",
                Locale.SIMPLIFIED_CHINESE));
        BUNDLES.put(RUSSIAN, ResourceBundle.getBundle("localization.Russian", RUSSIAN));
        BUNDLES.put(Locale.GERMAN, ResourceBundle.getBundle("localization.German", Locale.GERMAN));
        BUNDLES.put(Locale.US, ResourceBundle.getBundle("localization.English", Locale.US));
        BUNDLES.put(VIETNAMESE, ResourceBundle.getBundle("localization.Vietnamese", VIETNAMESE));

        BUNDLES.forEach((locale, b) -> STORE.registerAll(locale, b.keySet(), s -> new MessageFormat(b.getString(s))));

        GlobalTranslator.translator().addSource(STORE);
        setup = true;
    }
}