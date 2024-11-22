package dev.foxikle.customnpcs.internal.commands.suggestion;

import dev.foxikle.customnpcs.internal.CustomNPCs;
import dev.foxikle.customnpcs.internal.FileManager;
import dev.velix.imperat.BukkitSource;
import dev.velix.imperat.command.parameters.CommandParameter;
import dev.velix.imperat.context.SuggestionContext;
import dev.velix.imperat.resolvers.SuggestionResolver;

import java.util.Collection;
import java.util.UUID;

public class NpcBrokenSuggester implements SuggestionResolver<BukkitSource> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> autoComplete(SuggestionContext<BukkitSource> context, CommandParameter<BukkitSource> parameter) {
        final CustomNPCs plugin = CustomNPCs.getInstance();
        FileManager fileManager = plugin.getFileManager();
        return fileManager.getBrokenNPCs().keySet().stream().map(UUID::toString).toList();
    }
}