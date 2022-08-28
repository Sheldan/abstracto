package dev.sheldan.abstracto.customcommand.command;

import dev.sheldan.abstracto.core.command.CommandAlternative;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.customcommand.config.CustomCommandFeatureConfig;
import dev.sheldan.abstracto.customcommand.model.command.CustomCommandResponseModel;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.service.management.CustomCommandManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CustomCommandAlternative implements CommandAlternative {

    private static final String CUSTOM_COMMAND_RESPONSE = "custom_command_response";

    @Autowired
    private CommandRegistry commandRegistry;

    @Autowired
    private CustomCommandManagementService customCommandManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private CustomCommandFeatureConfig customCommandFeatureConfig;

    @Override
    public boolean shouldExecute(UnParsedCommandParameter parameter, Guild guild) {
        return featureFlagService.isFeatureEnabled(customCommandFeatureConfig, guild.getIdLong());
    }

    @Override
    public void execute(UnParsedCommandParameter parameter, Message message) {
        String contentStripped = message.getContentRaw();
        List<String> parameters = Arrays.asList(contentStripped.split(" "));
        String commandName = commandRegistry.getCommandName(parameters.get(0), message.getGuild().getIdLong());
        Optional<CustomCommand> customCommandOptional = customCommandManagementService.getCustomCommandByName(commandName, message.getGuild().getIdLong());
        customCommandOptional.ifPresent(customCommand -> {
            CustomCommandResponseModel model = CustomCommandResponseModel
                    .builder()
                    .additionalText(customCommand.getAdditionalMessage())
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(CUSTOM_COMMAND_RESPONSE, model, message.getGuild().getIdLong());
            channelService.sendMessageToSendToChannel(messageToSend, message.getChannel());
        });
    }


    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
