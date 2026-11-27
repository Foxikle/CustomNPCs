package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.internal.utils.WaitingType;
import studio.mevera.lotus.api.data.DataRegistry;

public final class AwaitString extends AwaitInput<String>{
    public AwaitString(WaitingType type, DataRegistry data) {
        super(type, data);
    }
}
