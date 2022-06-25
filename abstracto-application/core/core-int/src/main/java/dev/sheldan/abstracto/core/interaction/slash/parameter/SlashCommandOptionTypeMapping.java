package dev.sheldan.abstracto.core.interaction.slash.parameter;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;

@Getter
@Builder
public class SlashCommandOptionTypeMapping {
    private Class type;
    private List<OptionType> optionTypes;
}
