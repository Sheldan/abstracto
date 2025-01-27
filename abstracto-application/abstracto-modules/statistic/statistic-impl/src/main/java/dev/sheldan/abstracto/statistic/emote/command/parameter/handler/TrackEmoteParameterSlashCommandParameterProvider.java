package dev.sheldan.abstracto.statistic.emote.command.parameter.handler;

import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.interaction.slash.parameter.provider.SlashCommandParameterProvider;
import dev.sheldan.abstracto.statistic.emote.command.parameter.TrackEmoteParameter;
import java.util.Arrays;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

@Component
public class TrackEmoteParameterSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
            .builder()
            .type(TrackEmoteParameter.class)
            .optionTypes(Arrays.asList(OptionType.STRING))
            .build();
    }
}
