package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetEmote implements Command {

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private EmoteService emoteService;

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
        return CommandConfiguration.builder()
                .name("setEmote")
                .module(UtilityModuleInterface.UTILITY)
                .parameters(parameters)
                .description("Configures the emote key pointing towards a defined emote")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}