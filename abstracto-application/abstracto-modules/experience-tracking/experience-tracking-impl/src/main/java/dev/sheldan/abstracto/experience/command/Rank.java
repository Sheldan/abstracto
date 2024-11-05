package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.converter.LeaderBoardModelConverter;
import dev.sheldan.abstracto.experience.model.LeaderBoardEntry;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LeaderBoardEntryModel;
import dev.sheldan.abstracto.experience.model.template.RankModel;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import dev.sheldan.abstracto.experience.service.ExperienceLevelService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to show an embed containing information about the experience amount, level and message count of a ember on a server
 */
@Component
@Slf4j
public class Rank extends AbstractConditionableCommand {

    public static final String RANK_POST_EMBED_TEMPLATE = "rank_post";
    public static final String RANK_COMMAND = "rank";
    public static final String MEMBER_PARAMETER = "member";
    @Autowired
    private LeaderBoardModelConverter converter;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private AUserExperienceService userExperienceService;

    @Autowired
    private ExperienceLevelService experienceLevelService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private Rank self;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member targetMember = !parameters.isEmpty() ? (Member) parameters.get(0) : commandContext.getAuthor();
        if(!targetMember.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(targetMember);
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(aUserInAServer);
        CompletableFuture<List<LeaderBoardEntryModel>> future = converter.fromLeaderBoardEntry(Arrays.asList(userRank));
        RankModel rankModel = RankModel
                .builder()
                .member(targetMember)
                .build();
        return future.thenCompose(leaderBoardEntryModel -> {
                MessageToSend messageToSend = self.renderMessageToSend(targetMember, rankModel, leaderBoardEntryModel.get(0));
                return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()));
            }).thenApply(result -> CommandResult.fromIgnored());
    }

    @Transactional
    public MessageToSend renderMessageToSend(Member toRender, RankModel rankModel, LeaderBoardEntryModel leaderBoardEntryModel) {
        rankModel.setRankUser(leaderBoardEntryModel);
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(toRender);
        AUserExperience experienceObj = userExperienceManagementService.findUserInServer(aUserInAServer);
        log.info("Rendering rank for user {} in server {}.", toRender.getId(), toRender.getGuild().getId());
        Long currentExpNeeded = experienceObj.getCurrentLevel().getExperienceNeeded();
        Long experienceNeededToNextLevel = experienceLevelService.calculateExperienceToNextLevel(experienceObj.getCurrentLevel().getLevel(), experienceObj.getExperience());
        Long nextLevelExperience = experienceLevelService.calculateNextLevel(experienceObj.getCurrentLevel().getLevel()).getExperienceNeeded();
        Long experienceNeededForCurrentLevel = nextLevelExperience - currentExpNeeded;
        Long experienceWithinLevel = experienceObj.getExperience() - currentExpNeeded;
        rankModel.setExperienceForCurrentLevel(currentExpNeeded);
        rankModel.setCurrentLevelPercentage(((float) experienceWithinLevel / experienceNeededForCurrentLevel) * 100);
        rankModel.setLevelExperience(experienceNeededForCurrentLevel);
        rankModel.setExperienceToNextLevel(experienceNeededToNextLevel);
        rankModel.setInLevelExperience(experienceWithinLevel);
        rankModel.setNextLevelExperience(nextLevelExperience);
        return templateService.renderEmbedTemplate(RANK_POST_EMBED_TEMPLATE, rankModel, toRender.getGuild().getIdLong());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member targetMember;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        } else {
            targetMember = event.getMember();
        }
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(targetMember);
        LeaderBoardEntry userRank = userExperienceService.getRankOfUserInServer(aUserInAServer);
        CompletableFuture<List<LeaderBoardEntryModel>> future = converter.fromLeaderBoardEntry(Arrays.asList(userRank));
        RankModel rankModel = RankModel
                .builder()
                .member(targetMember)
                .build();
        return future.thenCompose(leaderBoardEntryModel -> {
            MessageToSend messageToSend = self.renderMessageToSend(targetMember, rankModel, leaderBoardEntryModel.get(0));
            return interactionService.replyMessageToSend(messageToSend, event);
        }).thenApply(result -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .templated(true)
                .type(Member.class)
                .optional(true)
                .build();
        List<Parameter> parameters = Arrays.asList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE)
                .commandName(RANK_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(RANK_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
