/*
 * Copyright (c) 2026. Foxikle
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

package dev.foxikle.customnpcs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesValidationTest {

    private record LocaleInfo(String baseName, Locale locale, String label) {
    }

    static Stream<Arguments> localeProvider() {
        return Stream.of(
                Arguments.of(new LocaleInfo("localization.English", Locale.US, "English_en_US")),
                Arguments.of(new LocaleInfo("localization.Chinese", Locale.SIMPLIFIED_CHINESE, "Chinese_zh_CN")),
                Arguments.of(new LocaleInfo("localization.German", Locale.GERMAN, "German_de")),
                Arguments.of(new LocaleInfo("localization.Russian", new Locale("ru"), "Russian_ru")),
                Arguments.of(new LocaleInfo("localization.Vietnamese", new Locale("vi"), "Vietnamese_vi"))
        );
    }

    @ParameterizedTest
    @MethodSource("localeProvider")
    void testNoUnescapedSingleQuotes(LocaleInfo info) {
        ResourceBundle bundle = ResourceBundle.getBundle(info.baseName, info.locale);
        List<String> failures = new ArrayList<>();

        for (String key : bundle.keySet()) {
            String value = bundle.getString(key);

            int maxPlaceholder = -1;
            for (int i = 0; i <= 9; i++) {
                if (value.contains("{" + i + "}")) {
                    maxPlaceholder = i;
                }
            }

            if (maxPlaceholder >= 0) {
                Object[] args = new Object[maxPlaceholder + 1];
                for (int i = 0; i <= maxPlaceholder; i++) {
                    args[i] = "ARG" + i;
                }
                try {
                    String result = MessageFormat.format(value, args);
                    for (int i = 0; i <= maxPlaceholder; i++) {
                        String placeholder = "{" + i + "}";
                        if (result.contains(placeholder)) {
                            failures.add(key + "=" + value + " -> `" + placeholder + "` not resolved (got: " + result + ")");
                        }
                    }
                } catch (Exception e) {
                    failures.add(key + "=" + value + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }

            for (int i = 0; i < value.length(); i++) {
                if (value.charAt(i) == '\'') {
                    if (i + 1 < value.length() && value.charAt(i + 1) == '\'') {
                        i++;
                        continue;
                    }
                    int braceIdx = value.indexOf('{', i + 1);
                    int nextQuote = value.indexOf('\'', i + 1);
                    if (braceIdx >= 0 && (nextQuote < 0 || braceIdx < nextQuote)) {
                        failures.add(key + "=" + value + " -> lone `'` before `{` at position " + i);
                    }
                }
            }
        }

        assertTrue(failures.isEmpty(),
                "Found " + failures.size() + " issue(s) in " + info.label + ":\n  " + String.join("\n  ", failures));
    }
}
