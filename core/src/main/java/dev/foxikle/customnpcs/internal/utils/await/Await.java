package dev.foxikle.customnpcs.internal.utils.await;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.api.Skin;
import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.interfaces.InternalNpc;
import dev.foxikle.customnpcs.internal.menu.MenuUtils;
import dev.foxikle.customnpcs.internal.utils.WaitingType;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import studio.mevera.lotus.Lotus;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.data.Key;
import studio.mevera.lotus.api.menu.Menu;

import java.util.concurrent.CompletableFuture;

/**
 * A class used to facilitate callbacks, mostly for menus.
 */
@UtilityClass
public final class Await {

    public static final Key<AwaitAction> CREATION = Key.of("action_creation", AwaitAction.class);
    public static final Key<AwaitAction> CLONE = Key.of("action_clone", AwaitAction.class);


    /**
     * Awaits the user's creation of an action. This Opens a menu for the player.
     */
    public static CompletableFuture<Result<Action>> createAction(Player p) {
        Lotus<Component> l = CustomNPCs.getInstance().getLotus();
        AwaitAction ac = new AwaitAction();
        Menu<Component> menu = l.registeredMenu(MenuUtils.NPC_NEW_ACTION).orElseThrow();
        l.openMenu(p, menu, DataRegistry.empty().put(CREATION, ac));
        return ac.future();
    }

    public static CompletableFuture<Result<String>> input(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitString.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitString");
        AwaitString as = new AwaitString(type, data);
        return input(as, p);
    }

    public static CompletableFuture<Result<Void>> signal(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitSignal.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitSignal");
        AwaitSignal as = new AwaitSignal(type, data);
        return input(as, p);
    }

    public static CompletableFuture<Result<Skin>> skin(Player p, WaitingType type, DataRegistry data) {
        if (type.inputType() != AwaitSkin.class)
            throw new IllegalArgumentException("WaitingType " + type + " does not have an input type of AwaitSkin");

        AwaitSkin as = new AwaitSkin(type, data);
        return input(as, p);
    }

    private static <T> CompletableFuture<Result<T>> input(AwaitInput<T> input, Player p) {
        CustomNPCs.getInstance().wait(p, input);
        p.closeInventory();
        return input.future();
    }

    public static CompletableFuture<Result<Action>> cloneAction(Player p, InternalNpc npc) {
        return null;
    }
}
