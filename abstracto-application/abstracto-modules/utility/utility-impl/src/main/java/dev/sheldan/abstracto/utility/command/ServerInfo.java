package dev.sheldan.abstracto.utility.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.template.display.EmoteDisplay;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.UtilityFeatureDefinition;
import dev.sheldan.abstracto.utility.config.UtilitySlashCommandNames;
import dev.sheldan.abstracto.utility.model.ServerInfoModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ServerInfo extends AbstractConditionableCommand {

    public static final String SERVERINFO_RESPONSE_TEMPLATE_KEY = "serverinfo_response";
    private static final String SERVER_INFO_COMMAND = "serverInfo";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return interactionService.replyMessageToSend(getMessageToSend(event.getGuild()), event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Guild guild) {
        return templateService.renderEmbedTemplate(SERVERINFO_RESPONSE_TEMPLATE_KEY, buildModel(guild), guild.getIdLong());
    }

    private ServerInfoModel buildModel(Guild guild) {
        ServerInfoModel model = ServerInfoModel
                .builder()
                .guild(guild)
                .timeCreated(guild.getTimeCreated().toInstant())
                .build();
        List<EmoteDisplay> staticEmotes = new ArrayList<>();
        List<EmoteDisplay> animatedEmotes = new ArrayList<>();
        guild.getEmojis().forEach(emote -> {
            EmoteDisplay emoteDisplay = EmoteDisplay.fromEmote(emote);
            if(emote.isAnimated()) {
                animatedEmotes.add(emoteDisplay);
            } else {
                staticEmotes.add(emoteDisplay);
            }
        });
        model.setAnimatedEmotes(animatedEmotes);
        model.setStaticEmotes(staticEmotes);
        return model;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(UtilitySlashCommandNames.UTILITY)
                .commandName(SERVER_INFO_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SERVER_INFO_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return UtilityFeatureDefinition.UTILITY;
    }
}
