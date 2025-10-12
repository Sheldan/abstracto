package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandAlternative;
import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureMode;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.models.template.commands.SlashCommandSuggestionModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SlashCommandSuggestor implements CommandAlternative {

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private CommandReceivedHandler commandReceivedHandler;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    public static final String SUGGESTION_TEMPLATE_KEY = "slash_command_suggestion";

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public boolean shouldExecute(UnParsedCommandParameter parameter, Guild guild, Message message) {
        boolean featureModeActive = featureModeService.featureModeActive(CoreFeatureDefinition.CORE_FEATURE, guild.getIdLong(), CoreFeatureMode.SUGGEST_SLASH_COMMANDS);
        if(!featureModeActive) {
            return false;
        }
        String commandName = commandReceivedHandler.getCommandName(message);
        Long guildId = message.getGuildIdLong();
        Optional<Command> potentialCommand = commandManager.getCommandByNameOptional(commandName, true, guildId);
        return potentialCommand.isPresent() && potentialCommand.get().getConfiguration().isSlashCommandOnly();
    }

    @Override
    public void execute(UnParsedCommandParameter parameter, Message message) {
        String commandName = commandReceivedHandler.getCommandName(message);
        Long guildId = message.getGuildIdLong();
        Optional<Command> potentialCommand = commandManager.getCommandByNameOptional(commandName, true, guildId);
        // limitation to not check conditions if command is executable, I dont want to completely built the entire command context, as that would require
        // to parse the parameters, therefore the major checks should suffice
        if(potentialCommand.isPresent()) {
            Command command = potentialCommand.get();
            if(command.getConfiguration().isSlashCommandOnly()) {
                boolean featureAvailable = featureFlagService.getFeatureFlagValue(command.getFeature(), guildId);
                if(featureAvailable) {
                    boolean shouldNotifyUser = command.getFeatureModeLimitations().isEmpty();
                    for (FeatureMode featureModeLimitation : command.getFeatureModeLimitations()) {
                        if(featureModeService.featureModeActive(command.getFeature(), guildId, featureModeLimitation)) {
                            shouldNotifyUser = true;
                        }
                    }
                    if(shouldNotifyUser) {
                        notifyUser(message, command, commandName, guildId);
                    }
                }
            }
        }
    }

    private void notifyUser(Message message, Command command, String commandName, Long guildId) {
        String path = command.getConfiguration().getSlashCommandConfig().getSlashCommandPath();
        SlashCommandSuggestionModel model = SlashCommandSuggestionModel
            .builder()
            .slashCommandPath(path)
            .build();
        Long userId = message.getAuthor().getIdLong();
        log.info("Suggesting slash command for command {} to user {}.", commandName, userId);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_TEMPLATE_KEY, model, guildId);
        FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, message.getChannel()))
            .thenAccept(unused -> {
                log.debug("Successfully suggested command.");
            }).exceptionally(throwable -> {
                log.warn("Failed to suggest slash command for command {} to user {}", commandName, userId);
                return null;
            });
    }
}
