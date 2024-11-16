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

package dev.foxikle.customnpcs.internal.translations;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;

import java.util.Locale;
import java.util.ResourceBundle;

public class Translations {
    TranslationRegistry registry = TranslationRegistry.create(Key.key("customnpcs:translations_english"));


    public void setup(Locale locale) {
//        registry.defaultLocale(locale);

        switch (locale.getLanguage()) {
            case "zh" -> {
                ResourceBundle bundle = ResourceBundle.getBundle("localization.Chinese", Locale.SIMPLIFIED_CHINESE, UTF8ResourceBundleControl.get());
                registry.registerAll(Locale.SIMPLIFIED_CHINESE, bundle, true);
            }
            case "ru" -> {
                ResourceBundle bundle = ResourceBundle.getBundle("localization.Russian", new Locale("ru"), UTF8ResourceBundleControl.get());
                registry.registerAll(new Locale("ru"), bundle, true);
            }
            case "de" -> {
                ResourceBundle bundle = ResourceBundle.getBundle("localization.German", Locale.GERMAN, UTF8ResourceBundleControl.get());
                registry.registerAll(Locale.GERMAN, bundle, true);
            }
            default -> {
                ResourceBundle bundle = ResourceBundle.getBundle("localization.English", Locale.US, UTF8ResourceBundleControl.get());
                registry.registerAll(Locale.US, bundle, true);
            }
        }


        GlobalTranslator.translator().addSource(registry);
    }
}
