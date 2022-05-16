package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.IncorrectFeatureModeConditionDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureModeCondition implements CommandCondition {

    @Autowired
    private FeatureModeService modeService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        Long serverId = context.getUserInitiatedContext().getGuild().getIdLong();
        return checkFeatureModeCondition(command, serverId);
    }

    private ConditionResult checkFeatureModeCondition(Command command, Long serverId) {
        if(!command.getFeatureModeLimitations().isEmpty()){
            FeatureDefinition feature = command.getFeature();
            if(feature != null) {
                for (FeatureMode featureModeLimitation : command.getFeatureModeLimitations()) {
                    if(modeService.featureModeActive(feature, serverId, featureModeLimitation)) {
                        return ConditionResult
                                .builder()
                                .result(true)
                                .build();
                    }
                }
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new IncorrectFeatureModeConditionDetail(feature, command.getFeatureModeLimitations()))
                        .build();
            }
        }

        return ConditionResult
                .builder()
                .result(true)
                .build();
    }

    @Override
    public ConditionResult shouldExecute(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        Long serverId = slashCommandInteractionEvent.getGuild().getIdLong();
        return checkFeatureModeCondition(command, serverId);
    }

    @Override
    public boolean supportsSlashCommands() {
        return true;
    }
}
