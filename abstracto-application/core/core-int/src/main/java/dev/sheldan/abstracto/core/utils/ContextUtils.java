package dev.sheldan.abstracto.core.utils;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
public class ContextUtils {

    public static boolean isGuildAware(Interaction interaction) {
        return !isNotGuildAware(interaction);
    }

    public static boolean isNotGuildAware(Interaction interaction) {
        return interaction.getGuild() == null || interaction.getGuild().isDetached();
    }

    public static boolean isUserCommand(Interaction interaction) {
        return interaction.getGuild() != null && interaction.getGuild().isDetached();
    }

    public static Long serverIdOrNull(Interaction interaction) {
        return ContextUtils.isGuildAware(interaction) ? interaction.getGuild().getIdLong() : null;
    }

    public static Long serverIdOrNull(InteractionHook hook) {
        return ContextUtils.isGuildAware(hook.getInteraction()) ? hook.getInteraction().getGuild().getIdLong() : null;
    }
}
