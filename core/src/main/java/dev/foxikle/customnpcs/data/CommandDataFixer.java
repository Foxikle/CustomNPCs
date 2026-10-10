package dev.foxikle.customnpcs.data;

import dev.foxikle.customnpcs.actions.Action;
import net.minestom.server.codec.StructCodec;

/**
 * Converts removed actions to RunCommand actions for compatibility
 */
@FunctionalInterface
public interface CommandDataFixer extends CommandDataFixers  {
    StructCodec<? extends Action> fix();
}
