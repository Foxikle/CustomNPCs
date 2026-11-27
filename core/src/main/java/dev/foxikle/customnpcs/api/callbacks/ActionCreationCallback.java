package dev.foxikle.customnpcs.api.callbacks;

import dev.foxikle.customnpcs.actions.Action;
import dev.foxikle.customnpcs.internal.utils.Utils;
import dev.foxikle.customnpcs.internal.utils.await.Await;
import dev.foxikle.customnpcs.internal.utils.await.AwaitAction;
import lombok.AllArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import studio.mevera.lotus.api.button.ClickAction;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.menu.MenuView;

@AllArgsConstructor
public class ActionCreationCallback implements ClickAction {
    private final Action action;

    @Override
    public void onClick(@NotNull MenuView<?, ?> view, @NotNull InventoryClickEvent event) {
        event.setCancelled(true);
        Action impl = action.getDefaultValue().get();
        AwaitAction aa = view.data().get(Await.CREATION).orElseThrow();
        Utils.sound(view, Sound.UI_BUTTON_CLICK);
        view.lotus().openMenu(view.viewer(), action.getMenu(), DataRegistry.empty()
                .put(Await.CREATION, aa)
                .put(Action.KEY, impl));

    }
}
