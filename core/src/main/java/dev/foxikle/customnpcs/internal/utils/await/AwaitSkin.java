package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.api.Skin;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import studio.mevera.lotus.api.data.DataRegistry;

public final class AwaitSkin extends AwaitInput<Skin> {
    public AwaitSkin(WaitingType type, DataRegistry data) {
        super(type, data);
    }
}
