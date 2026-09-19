/*
 * Copyright (c) 2025-2026. Foxikle
 *
 * Permission is hereby granted,
free of charge,
to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
to deal
 * in the Software without restriction,
including without limitation the rights
 * to use,
copy,
modify,
merge,
publish,
distribute,
sublicense,
and/or sell
 * copies of the Software,
and to permit persons to whom the Software is
 * furnished to do so,
subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS",
WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR
 * IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER
 * LIABILITY,
WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE,
ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.foxikle.customnpcs.internal.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum WaitingType {
    COMMAND("command"),
    NAME("name"),
    TARGET("target"),
    TITLE("title"),
    SUBTITLE("subtitle"),
    MESSAGE("message"),
    FACING("facing_direction"),
    SOUND("sound"),
    SERVER("server"),
    ACTIONBAR("actionbar"),
    URL("url"),
    PLAYER("player_name"),
    HOLOGRAM("interactable_hologram"),
    NUDGE("nudge"),
    RECORDING("recording");

    private final String i18nKey;
}
