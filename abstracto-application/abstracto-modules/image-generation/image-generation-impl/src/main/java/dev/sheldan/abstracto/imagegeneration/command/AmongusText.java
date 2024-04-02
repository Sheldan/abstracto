package dev.sheldan.abstracto.imagegeneration.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.AttachedFile;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.imagegeneration.config.ImageGenerationFeatureDefinition;
import dev.sheldan.abstracto.imagegeneration.config.ImageGenerationSlashCommandNames;
import dev.sheldan.abstracto.imagegeneration.service.ImageGenerationService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class AmongusText extends AbstractConditionableCommand {

    public static final String TEXT_PARAMETER_KEY = "text";

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FileService fileService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    private static final String AMONGUS_TEXT_EMBED_TEMPLATE_KEY = "amongusText_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String text = (String) commandContext.getParameters().getParameters().get(0);
        File amongusTextImage = imageGenerationService.getAmongusTextImage(text);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(AMONGUS_TEXT_EMBED_TEMPLATE_KEY, new Object(), commandContext.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(amongusTextImage)
                .fileName("amongus.png")
                .build();
        messageToSend.getAttachedFiles().add(file);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String text = slashCommandParameterService.getCommandOption(TEXT_PARAMETER_KEY, event, String.class);
        File amongusTextImage = imageGenerationService.getAmongusTextImage(text);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(AMONGUS_TEXT_EMBED_TEMPLATE_KEY, new Object(), event.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(amongusTextImage)
                .fileName("amongus.png")
                .build();
        messageToSend.getAttachedFiles().add(file);
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(messageToSend, event.getHook()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter memberParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER_KEY)
                .type(String.class)
                .remainder(true)
                .templated(true)
                .build();
        parameters.add(memberParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(ImageGenerationSlashCommandNames.IMAGE_GENERATION)
                .groupName("memes")
                .commandName("amongustext")
                .build();

        return CommandConfiguration.builder()
                .name("amongusText")
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ImageGenerationFeatureDefinition.IMAGE_GENERATION;
    }
}
