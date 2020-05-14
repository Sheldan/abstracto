package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DeleteWarning extends AbstractConditionableCommand {

    @Autowired
    private WarnManagementService warnManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long warnId = (Long) commandContext.getParameters().getParameters().get(0);
        Optional<Warning> optional = warnManagementService.findById(warnId);
        optional.ifPresent(warning -> {
            if(warning.getWarnedUser().getServerReference().getId().equals(commandContext.getUserInitiatedContext().getServer().getId())) {
                warnManagementService.deleteWarning(warning);
            }
        });
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("warnId").templated(true).type(Long.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("delWarn");
        return CommandConfiguration.builder()
                .name("deleteWarning")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .aliases(aliases)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.WARNING;
    }
}
