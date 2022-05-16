package dev.sheldan.abstracto.suggestion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionSlashCommandNames;
import dev.sheldan.abstracto.suggestion.service.SuggestionService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Suggest extends AbstractConditionableCommand {

    private static final String TEXT_PARAMETER = "text";
    private static final String SUGGEST_COMMAND = "suggest";
    private static final String ATTACHMENT_PARAMETER = "attachment";
    private static final String SUGGEST_RESPONSE = "suggest_response";

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String text = (String) parameters.get(0);
        ServerChannelMessageUser cause = ServerChannelMessageUser
                .builder()
                .serverId(commandContext.getGuild().getIdLong())
                .channelId(commandContext.getChannel().getIdLong())
                .messageId(commandContext.getMessage().getIdLong())
                .userId(commandContext.getAuthor().getIdLong())
                .build();
        String attachmentURL = commandContext
                .getMessage()
                    .getAttachments()
                    .stream()
                        .filter(Message.Attachment::isImage)
                    .findFirst()
                    .map(Message.Attachment::getProxyUrl)
                    .orElse(null);
        return suggestionService.createSuggestionMessage(cause, text, attachmentURL)
                .thenApply(aVoid ->  CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String suggestionText = slashCommandParameterService.getCommandOption(TEXT_PARAMETER, event, String.class);
        String attachmentURL;
        if(slashCommandParameterService.hasCommandOption(ATTACHMENT_PARAMETER, event)) {
            Message.Attachment attachment = slashCommandParameterService.getCommandOption(ATTACHMENT_PARAMETER, event, File.class, Message.Attachment.class);
            if(attachment.isImage()) {
                attachmentURL = attachment.getProxyUrl();
            } else {
                attachmentURL = null;
            }
        } else {
            attachmentURL = null;
        }
        ServerChannelMessageUser cause = ServerChannelMessageUser
                .builder()
                .userId(event.getMember().getIdLong())
                .channelId(event.getChannel().getIdLong())
                .serverId(event.getGuild().getIdLong())
                .build();
        return suggestionService.createSuggestionMessage(cause, suggestionText, attachmentURL)
                .thenCompose(unused -> interactionService.replyEmbed(SUGGEST_RESPONSE, event))
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build();

        Parameter fileAttachmentParameter = Parameter
                .builder()
                .name(ATTACHMENT_PARAMETER)
                .type(File.class)
                .slashCommandOnly(true)
                .templated(true)
                .optional(true)
                .build();
        List<Parameter> parameters = Arrays.asList(textParameter, fileAttachmentParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(SuggestionSlashCommandNames.SUGGEST_PUBLIC)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name(SUGGEST_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return SuggestionFeatureDefinition.SUGGEST;
    }
}
