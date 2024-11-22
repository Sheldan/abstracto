package dev.sheldan.abstracto.imagegeneration.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CombinedParameterEntry;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.command.config.Parameter.ADDITIONAL_TYPES_KEY;

@Component
public class Pat extends AbstractConditionableCommand {
    public static final String MEMBER_PARAMETER_KEY = "member";

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

    private static final String PAT_EMBED_TEMPLATE_KEY = "pat_response";

    @Value("${abstracto.feature.imagegeneration.pat.imagesize}")
    private Integer imageSize;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Member member;
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(parameters.isEmpty()) {
            member = commandContext.getAuthor();
        } else {
            if(parameters.get(0) instanceof Message) {
                member = ((Message) parameters.get(0)).getMember();
            } else {
                member = (Member) parameters.get(0);
            }
        }
        File patGifFile = imageGenerationService.getPatGif(member.getEffectiveAvatar().getUrl(imageSize));
        MessageToSend messageToSend = templateService.renderEmbedTemplate(PAT_EMBED_TEMPLATE_KEY, new Object(), commandContext.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(patGifFile)
                .fileName("pat.gif")
                .build();
        messageToSend.getAttachedFiles().add(file);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member targetMember;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER_KEY, event)) {
            targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER_KEY, event, Member.class);
        } else {
            targetMember = event.getMember();
        }
        File patGifFile = imageGenerationService.getPatGif(targetMember.getEffectiveAvatar().getUrl(imageSize));
        MessageToSend messageToSend = templateService.renderEmbedTemplate(PAT_EMBED_TEMPLATE_KEY, new Object(), event.getGuild().getIdLong());
        // template support does not support binary files
        AttachedFile file = AttachedFile
                .builder()
                .file(patGifFile)
                .fileName("pat.gif")
                .build();
        messageToSend.getAttachedFiles().add(file);
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(messageToSend, event.getHook()))
                .thenAccept(unused -> fileService.safeDeleteIgnoreException(messageToSend.getAttachedFiles().get(0).getFile()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Map<String, Object> parameterAlternatives = new HashMap<>();
        parameterAlternatives.put(ADDITIONAL_TYPES_KEY, Arrays.asList(CombinedParameterEntry.messageParameter(Message.class), CombinedParameterEntry.parameter(Member.class)));
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER_KEY)
                .type(CombinedParameter.class)
                .additionalInfo(parameterAlternatives)
                .templated(true)
                .optional(true)
                .build();
        parameters.add(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ImageGenerationSlashCommandNames.IMAGE_GENERATION)
                .groupName("memes")
                .commandName("pat")
                .build();

        return CommandConfiguration.builder()
                .name("pat")
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
