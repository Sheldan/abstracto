package dev.sheldan.abstracto.core.command.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.command.slash.parameter.provider.SlashCommandParameterProvider;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class MemberSlashCommandParameterProvider implements SlashCommandParameterProvider {
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(Member.class)
                .optionTypes(Arrays.asList(OptionType.USER))
                .build();
    }
}
