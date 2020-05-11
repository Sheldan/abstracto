package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetModMailCategory extends AbstractConditionableCommand {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long categoryId = (Long) commandContext.getParameters().getParameters().get(0);
        modMailThreadService.setModMailCategoryTo(commandContext.getUserInitiatedContext().getServer(), categoryId);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter categoryId = Parameter.builder().name("categoryId").type(Long.class).description("The category id to be used for modmail.").build();
        List<Parameter> parameters = Arrays.asList(categoryId);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<String> aliases = Arrays.asList("modMailCat");
        return CommandConfiguration.builder()
                .name("setModMailCategory")
                .module(ModMailModuleInterface.MODMAIL)
                .aliases(aliases)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }
}
