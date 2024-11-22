package dev.sheldan.abstracto.entertainment.command.economy;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentModuleDefinition;
import dev.sheldan.abstracto.entertainment.config.EntertainmentSlashCommandNames;
import dev.sheldan.abstracto.entertainment.model.command.TransferCreditsModel;
import dev.sheldan.abstracto.entertainment.service.EconomyService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TransferCredits extends AbstractConditionableCommand {
    private static final String TRANSFER_CREDITS_COMMAND = "transferCredits";
    private static final String TRANSFER_CREDITS_RESPONSE = "transferCredits_response";
    private static final String MEMBER_PARAMETER = "targetMember";
    private static final String AMOUNT_PARAMETER = "amount";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private EconomyService economyService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member targetMember = (Member) parameters.get(0);
        Integer amount = (Integer) parameters.get(1);
        AUserInAServer targetUser = userInServerManagementService.loadOrCreateUser(targetMember);
        AUserInAServer sourceUser = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        economyService.transferCredits(sourceUser, targetUser, amount.longValue());
        TransferCreditsModel responseModel = TransferCreditsModel
                .builder()
                .sourceMember(MemberDisplay.fromMember(commandContext.getAuthor()))
                .targetMember(MemberDisplay.fromMember(targetMember))
                .credits(amount)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(TRANSFER_CREDITS_RESPONSE, responseModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        Integer amount = slashCommandParameterService.getCommandOption(AMOUNT_PARAMETER, event, Integer.class);
        AUserInAServer targetUser = userInServerManagementService.loadOrCreateUser(targetMember);
        AUserInAServer sourceUser = userInServerManagementService.loadOrCreateUser(event.getMember());
        TransferCreditsModel responseModel = TransferCreditsModel
                .builder()
                .sourceMember(MemberDisplay.fromMember(event.getMember()))
                .targetMember(MemberDisplay.fromMember(targetMember))
                .credits(amount)
                .build();
        economyService.transferCredits(sourceUser, targetUser, amount.longValue());
        return interactionService.replyEmbed(TRANSFER_CREDITS_RESPONSE, responseModel, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .templated(true)
                .type(Member.class)
                .optional(true)
                .build();

        Parameter amountParameter = Parameter
                .builder()
                .name(AMOUNT_PARAMETER)
                .templated(true)
                .type(Integer.class)
                .optional(true)
                .build();

        List<Parameter> parameters = Arrays.asList(memberParameter, amountParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(EntertainmentSlashCommandNames.ECONOMY)
                .commandName("transfer")
                .build();

        return CommandConfiguration.builder()
                .name(TRANSFER_CREDITS_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(EntertainmentModuleDefinition.ENTERTAINMENT)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .causesReaction(false)
                .async(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ECONOMY;
    }
}
