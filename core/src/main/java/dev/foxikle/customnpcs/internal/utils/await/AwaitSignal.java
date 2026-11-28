package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.internal.utils.WaitingType;
import studio.mevera.lotus.api.data.DataRegistry;

public class AwaitSignal extends AwaitInput<Void> {
    public AwaitSignal(WaitingType type, DataRegistry data) {
        super(type, data);
    }

    public void complete() {
        super.complete(null);
    }
}
