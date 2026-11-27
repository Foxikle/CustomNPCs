package dev.foxikle.customnpcs.api.actions;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.AwaitAction;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.menu.InteractiveMenu;
import studio.mevera.lotus.api.menu.MenuView;

public interface ActionCustomizer extends InteractiveMenu<Component> {

    @SuppressWarnings("unchecked")
    default <T> T getAction(MenuView<?, ?> view) {
        return (T) view.data().get(Action.KEY).orElseThrow();
    }

    @Override
    default void onClose(@NotNull MenuView<Component, ?> view, @NotNull InventoryCloseEvent event) {
        AwaitAction aa = view.data().get(Await.CREATION).orElseThrow();
        aa.cancel();
    }
}
