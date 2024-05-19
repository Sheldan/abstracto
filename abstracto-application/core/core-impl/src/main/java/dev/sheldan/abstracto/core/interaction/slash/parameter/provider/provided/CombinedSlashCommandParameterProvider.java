package dev.sheldan.abstracto.core.interaction.slash.parameter.provider.provided;

import dev.sheldan.abstracto.core.command.config.CombinedParameterEntry;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandOptionTypeMapping;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.provider.SlashCommandParameterProvider;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
public class CombinedSlashCommandParameterProvider implements SlashCommandParameterProvider {

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public SlashCommandOptionTypeMapping getOptionMapping() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(CombinedParameter.class)
                .optionTypes(Arrays.asList(OptionType.STRING))
                .build();
    }

    /**
     * The purpose of this method is just for the sake of a combined parameter, so that the combined parameters are evaluated _again_
     * and every part of the parameter gets its own option. We only need this when creating the options. The other time a slash command parameter provider
     * is executed, its when actually retrieving the parameter, but in that case the call is not done via the CombinedParameter type, but
     * via the concrete type (Member, String, etc) anyway
     */
    @Override
    public SlashCommandOptionTypeMapping getOptionMapping(Parameter parameter) {
        Map<String, Object> additionalInfo = parameter.getAdditionalInfo();
        if(!additionalInfo.containsKey(ADDITIONAL_TYPES_KEY)) {
            return empty();
        }
        List<CombinedParameterEntry> combinedParameterEntries = (List<CombinedParameterEntry>) additionalInfo.get(ADDITIONAL_TYPES_KEY);
        if(combinedParameterEntries.isEmpty()) {
            return empty();
        }
        List<Class> possibleTypes = combinedParameterEntries
                .stream()
                .filter(CombinedParameterEntry::isUsableInSlashCommands)
                .map(CombinedParameterEntry::getType)
                .toList();
        if(possibleTypes.isEmpty()) {
            return empty();
        }
        Set<OptionType> optionTypes = new HashSet<>();
        possibleTypes.forEach(additionalType -> {
            optionTypes.addAll(slashCommandParameterService.getTypesFromParameter(additionalType, parameter.getUseStrictParameters()));
        });
        return SlashCommandOptionTypeMapping
                .builder()
                .type(CombinedParameter.class)
                .optionTypes(new ArrayList<>(optionTypes))
                // this is needed, because the combined parameter is defined as strict, hence we are using the strictTypes value from the returned object
                .strictTypes(parameter.getUseStrictParameters() ? new ArrayList<>(optionTypes) : new ArrayList<>())
                .build();
    }

    private static SlashCommandOptionTypeMapping empty() {
        return SlashCommandOptionTypeMapping
                .builder()
                .type(CombinedParameter.class)
                .optionTypes(new ArrayList<>())
                .build();
    }
}
