package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetEmote extends AbstractConditionableCommand {

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String emoteKey = (String) commandContext.getParameters().getParameters().get(0);
        Object o = commandContext.getParameters().getParameters().get(1);
        if(o instanceof String) {
            String emote = (String) o;
            emoteManagementService.setEmoteToDefaultEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
        } else {
            Emote emote = (Emote) o;
            // todo check if usable
            emoteManagementService.setEmoteToCustomEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter emoteKey = Parameter.builder().name("emoteKey").type(String.class).description("The internal key of the emote").build();
        Parameter emote = Parameter.builder().name("emote").type(net.dv8tion.jda.api.entities.Emote.class).description("The emote to be used").build();
        List<Parameter> parameters = Arrays.asList(emoteKey, emote);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("setEmote")
                .module(UtilityModuleInterface.UTILITY)
                .parameters(parameters)
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
