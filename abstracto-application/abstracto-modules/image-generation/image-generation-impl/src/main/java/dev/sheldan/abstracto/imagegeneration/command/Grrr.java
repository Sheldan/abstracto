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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Grrr extends AbstractConditionableCommand {

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

    private static final String GRRR_EMBED_TEMPLATE_KEY = "grrr_response";
    public static final String TEXT_PARAMETER_KEY = "text";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String text = (String) parameters.get(0);
        File grrrFile = imageGenerationService.getGrrrImage(text);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GRRR_EMBED_TEMPLATE_KEY, new Object(), commandContext.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(grrrFile)
                .fileName("grrr.png")
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
        File grrrFile = imageGenerationService.getGrrrImage(text);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(GRRR_EMBED_TEMPLATE_KEY, new Object(), event.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(grrrFile)
                .fileName("grrr.png")
                .build();
        messageToSend.getAttachedFiles().add(file);
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(messageToSend, event.getHook()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter textParameter = Parameter
                .builder()
                .name(TEXT_PARAMETER_KEY)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build();
        parameters.add(textParameter);
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
                .commandName("grrr")
                .build();

        return CommandConfiguration.builder()
                .name("grrr")
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
