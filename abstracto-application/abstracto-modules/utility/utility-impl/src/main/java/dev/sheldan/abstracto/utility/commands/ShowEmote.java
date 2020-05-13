package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.ShowEmoteLog;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShowEmote extends AbstractConditionableCommand {

    private static final String SHOW_EMOTE_RESPONSE_TEMPLATE = "showEmote_response";

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Object emoteParameter = parameters.get(0);
        if(!(emoteParameter instanceof Emote)) {
            return CommandResult.fromError(templateService.renderTemplate("no_custom_emote_found", new Object()));
        }
        Emote emote = (Emote) emoteParameter;
        ShowEmoteLog emoteLog = (ShowEmoteLog) ContextConverter.fromCommandContext(commandContext, ShowEmoteLog.class);
        emoteLog.setEmote(emote);
        String message = templateService.renderTemplate(SHOW_EMOTE_RESPONSE_TEMPLATE, emoteLog);
        commandContext.getChannel().sendMessage(message).queue();
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("emote").type(Emote.class).optional(false).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("showEmote")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.UTILITY;
    }
}
