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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Translations {
    public static final Locale VIETNAMESE = Locale.forLanguageTag("vi");
    public static final Locale RUSSIAN = Locale.forLanguageTag("ru");

    private static final String DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();


    private static final Map<String, String> FILES = Map.of(
            Locale.ENGLISH.getLanguage(), "english",
            Locale.CHINESE.getLanguage(), "chinese",
            Locale.GERMAN.getLanguage(), "german",
            RUSSIAN.getLanguage(), "russian",
            VIETNAMESE.getLanguage(), "vietnamese"
    );

    /**
     * Lazily populated, thread-safe. Each value is an immutable map.
     */
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();


    public static void setup() {
        for (String language : FILES.keySet()) {
            CACHE.computeIfAbsent(language, Translations::load);
        }
    }

    /**
     * Looks up a translation. Falls back to English, then to the key itself.
     * Any locale is accepted: only the language part is used (en_GB -> en, ru_RU -> ru).
     */
    @NotNull
    public static String getString(Locale locale, String key) {
        String value = tableFor(locale).get(key);
        if (value == null) {
            value = table(DEFAULT_LANGUAGE).get(key);
        }
        return value != null ? value : key;
    }

    private static Map<String, String> tableFor(Locale locale) {
        String language = locale.getLanguage();
        return table(FILES.containsKey(language) ? language : DEFAULT_LANGUAGE);
    }

    private static Map<String, String> table(String language) {
        return CACHE.computeIfAbsent(language, Translations::load);
    }

    private static Map<String, String> load(String language) {
        String path = "localization/" + FILES.get(language) + ".properties";
        try (InputStream in = Translations.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing translation file: " + path);
            }
            // Read as UTF-8 explicitly (ResourceBundle on Java 8 would assume ISO-8859-1).
            Properties props = new Properties();
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));

            Map<String, String> map = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                map.put(key, props.getProperty(key));
            }
            return Map.copyOf(map);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}