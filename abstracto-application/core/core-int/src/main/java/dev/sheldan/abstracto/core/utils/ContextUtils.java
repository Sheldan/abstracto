package dev.sheldan.abstracto.core.utils;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
public class ContextUtils {

    public static boolean isGuildKnown(Interaction interaction) {
        return !isGuildNotKnown(interaction);
    }

    public static boolean isGuildNotKnown(Interaction interaction) {
        return interaction.getGuild() == null || interaction.getGuild().isDetached();
    }

    public static boolean isUserCommandInGuild(Interaction interaction) {
        return isUserCommand(interaction) && interaction.getGuild() != null && interaction.getGuild().isDetached();
    }

    public static boolean isUserCommand(Interaction interaction) {
        return interaction.getIntegrationOwners() != null && interaction.getIntegrationOwners().getUserIntegration() != null;
    }

    public static Long serverIdOrNull(Interaction interaction) {
        return ContextUtils.isGuildKnown(interaction) ? interaction.getGuild().getIdLong() : null;
    }

    public static Long serverIdOrNull(InteractionHook hook) {
        return ContextUtils.isGuildKnown(hook.getInteraction()) ? hook.getInteraction().getGuild().getIdLong() : null;
    }
}
