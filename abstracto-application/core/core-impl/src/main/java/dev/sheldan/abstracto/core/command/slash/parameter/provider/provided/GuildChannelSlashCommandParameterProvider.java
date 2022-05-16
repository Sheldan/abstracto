package dev.sheldan.abstracto.core.command.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.command.slash.parameter.provider.SlashCommandParameterProvider;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class GuildChannelSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(GuildChannel.class)
                .optionTypes(Arrays.asList(OptionType.CHANNEL))
                .build();
    }
}