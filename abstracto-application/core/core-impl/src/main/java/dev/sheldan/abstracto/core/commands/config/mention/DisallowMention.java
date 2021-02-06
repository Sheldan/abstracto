package dev.sheldan.abstracto.core.commands.config.mention;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.AllowedMentionService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DisallowMention extends AbstractConditionableCommand {

    @Autowired
    private AllowedMentionService allowedMentionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String mentionTypeInput = (String) commandContext.getParameters().getParameters().get(0);
        Message.MentionType mentionType = allowedMentionService.getMentionTypeFromString(mentionTypeInput);
        allowedMentionService.disAllowMentionForServer(mentionType, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter mentionTypeParameter = Parameter.builder().name("mentionType").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(mentionTypeParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("disallowMention")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

}
