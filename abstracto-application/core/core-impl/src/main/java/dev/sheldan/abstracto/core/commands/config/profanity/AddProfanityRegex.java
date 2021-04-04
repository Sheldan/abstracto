package dev.sheldan.abstracto.core.commands.config.profanity;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ProfanityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AddProfanityRegex extends AbstractConditionableCommand {

    @Autowired
    private ProfanityService profanityService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String profanityGroupName = (String) parameters.get(0);
        String profanityName = (String) parameters.get(1);
        String regex = (String) parameters.get(2);
        Long serverId = commandContext.getGuild().getIdLong();
        if(parameters.size() < 4) {
            profanityService.createProfanityRegex(serverId, profanityGroupName, profanityName, regex);
        } else {
            String replacement = (String) parameters.get(3);
            profanityService.createProfanityRegex(serverId, profanityGroupName, profanityName, regex, replacement);
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter profanityGroupParameter = Parameter.builder().name("profanityGroup").type(String.class).templated(true).build();
        Parameter nameParameter = Parameter.builder().name("profanityName").type(String.class).templated(true).build();
        Parameter regexParameter = Parameter.builder().name("regex").type(String.class).templated(true).build();
        Parameter replacement = Parameter.builder().name("replacement").type(String.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(profanityGroupParameter, nameParameter, regexParameter, replacement);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("addProfanityRegex")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
