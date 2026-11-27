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

import dev.foxikle.customnpcs.internal.utils.await.AwaitInput;
import dev.foxikle.customnpcs.internal.utils.await.AwaitSignal;
import dev.foxikle.customnpcs.internal.utils.await.AwaitSkin;
import dev.foxikle.customnpcs.internal.utils.await.AwaitString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public enum WaitingType {
    COMMAND("command", AwaitString.class),
    NAME("name", AwaitString.class),
    TARGET("target", AwaitString.class),
    TITLE("title", AwaitString.class),
    SUBTITLE("subtitle", AwaitString.class),
    MESSAGE("message", AwaitString.class),
    FACING("facing_direction", AwaitSignal.class),
    SERVER("server", AwaitString.class),
    ACTIONBAR("actionbar", AwaitString.class),
    URL("url", AwaitSkin.class),
    PLAYER("player_name", AwaitSkin.class),
    HOLOGRAM("interactable_hologram", AwaitString.class),
    NUDGE("nudge", AwaitSignal.class),
    RECORDING("recording", AwaitSignal.class);

    private final String i18nKey;
    private final Class<? extends AwaitInput<?>> inputType;
}
