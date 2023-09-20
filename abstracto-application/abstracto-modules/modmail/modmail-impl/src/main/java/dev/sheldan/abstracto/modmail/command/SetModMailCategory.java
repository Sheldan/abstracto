package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This command is used to change the category used to create new mod mail threads. This does not migrate the
 * existing mod mail threads.
 */
@Component
public class SetModMailCategory extends AbstractConditionableCommand {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long categoryId = (Long) commandContext.getParameters().getParameters().get(0);
        modMailThreadService.setModMailCategoryTo(commandContext.getGuild(), categoryId);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter categoryId = Parameter
                .builder()
                .name("categoryId")
                .type(Long.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(categoryId);
        HelpInfo helpInfo = HelpInfo.
                builder()
                .templated(true)
                .build();
        List<String> aliases = Arrays.asList("modMailCat");
        return CommandConfiguration.builder()
                .name("setModMailCategory")
                .messageCommandOnly(true)
                .module(ModMailModuleDefinition.MODMAIL)
                .aliases(aliases)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }
}
