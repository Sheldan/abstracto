package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.validator.MaxStringLengthValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetEmote extends AbstractConditionableCommand {

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private EmoteService emoteService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String emoteKey = (String) parameters.get(0);
        AEmote emote = (AEmote) parameters.get(1);
        emoteManagementService.setEmoteToAEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<ParameterValidator> emoteKeyValidators = Arrays.asList(MaxStringLengthValidator.max(255L));
        Parameter emoteKey = Parameter.builder().name("emoteKey").validators(emoteKeyValidators).type(String.class).templated(true).build();
        Parameter emote = Parameter.builder().name("emote").type(AEmote.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(emoteKey, emote);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("setEmote")
                .module(UtilityModuleInterface.UTILITY)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
