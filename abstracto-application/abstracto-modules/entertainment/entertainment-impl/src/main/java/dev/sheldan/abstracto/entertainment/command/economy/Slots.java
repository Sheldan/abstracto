package dev.sheldan.abstracto.entertainment.command.economy;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.validator.MinIntegerValueValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.dto.SlotsResult;
import dev.sheldan.abstracto.entertainment.model.command.SlotsResponseModel;
import dev.sheldan.abstracto.entertainment.service.EconomyServiceBean;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Slots extends AbstractConditionableCommand {

    private static final String SLOTS_COMMAND_NAME = "slots";
    private static final String BID_PARAMETER = "bid";
    private static final String SLOTS_RESPONSE = "slots_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private EconomyServiceBean economyUserServiceBean;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Integer bid = (Integer) commandContext.getParameters().getParameters().get(0);
        Member member = commandContext.getAuthor();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        SlotsResult slotsResult = economyUserServiceBean.triggerSlots(aUserInAServer, bid.longValue());
        SlotsResponseModel responseModel = SlotsResponseModel.fromSlotsResult(slotsResult);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SLOTS_RESPONSE, responseModel, member.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long bid = slashCommandParameterService.getCommandOption(BID_PARAMETER, event, Integer.class).longValue();
        Member member = event.getMember();
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        SlotsResult slotsResult = economyUserServiceBean.triggerSlots(aUserInAServer, bid);
        SlotsResponseModel responseModel = SlotsResponseModel.fromSlotsResult(slotsResult);
        return interactionService.replyEmbed(SLOTS_RESPONSE, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter bidParameter = Parameter
                .builder()
                .name(BID_PARAMETER)
                .type(Integer.class)
                .templated(true)
                .validators(Arrays.asList(MinIntegerValueValidator.min(0L)))
                .build();
        parameters.add(bidParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.ECONOMY)
                .commandName("slots")
                .build();

        return CommandConfiguration.builder()
                .name(SLOTS_COMMAND_NAME)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ECONOMY;
    }
}
