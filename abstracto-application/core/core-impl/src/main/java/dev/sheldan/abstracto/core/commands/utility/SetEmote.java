package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.exception.ConfigurationException;
import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.EmoteService;
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
    public Result execute(CommandContext commandContext) {
        String emoteKey = (String) commandContext.getParameters().getParameters().get(0);
        Object o = commandContext.getParameters().getParameters().get(1);
        if(o instanceof String) {
            String emote = (String) o;
            emoteManagementService.setEmoteToDefaultEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
        } else {
            Emote emote = (Emote) o;
            if(emoteService.isEmoteUsableByBot(emote)) {
                emoteManagementService.setEmoteToCustomEmote(emoteKey, emote, commandContext.getGuild().getIdLong());
            } else {
                throw new ConfigurationException("Emote is not usable by bot.");
            }
        }
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter emoteKey = Parameter.builder().name("emoteKey").type(String.class).description("The internal key of the emote").build();
        Parameter emote = Parameter.builder().name("emote").type(net.dv8tion.jda.api.entities.Emote.class).description("The emote to be used").build();
        List<Parameter> parameters = Arrays.asList(emoteKey, emote);
        return CommandConfiguration.builder()
                .name("setEmote")
                .module(UtilityModule.UTILITY)
                .parameters(parameters)
                .description("Configures the emote key pointing towards a defined emote")
                .causesReaction(true)
                .build();
    }
}
