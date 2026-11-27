package dev.foxikle.customnpcs.internal.menu.util;

import net.kyori.adventure.text.Component;
import studio.mevera.lotus.api.data.DataRegistry;
import studio.mevera.lotus.api.data.Key;
import studio.mevera.lotus.api.menu.Menu;
import studio.mevera.lotus.api.menu.MenuView;

public record GoBack (Menu<Component> menu, DataRegistry data) {

    public static final Key<GoBack> KEY = Key.of("go_back", GoBack.class);

    public GoBack(MenuView<Component, ?> view) {
      this(view.menu(), view.data());
    }
}
