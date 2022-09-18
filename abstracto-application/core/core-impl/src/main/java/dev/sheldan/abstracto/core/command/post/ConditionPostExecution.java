package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.model.condition.GenericConditionModel;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ReactionService;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConditionPostExecution implements PostCommandExecution {
    public static final String GENERIC_COMMAND_EXCEPTION_MODEL_KEY = "generic_condition_notification";

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.CONDITION)
                && commandResult.getConditionResult() != null &&
                !commandResult.getConditionResult().isResult()
                && commandResult.getConditionResult().getConditionDetail() != null
                && commandResult.getConditionResult().isReportResult()) {
            reactionService.addReactionToMessage(CoreFeatureConfig.WARN_REACTION_KEY, commandContext.getGuild().getIdLong(), commandContext.getMessage());
            GenericConditionModel conditionModel = GenericConditionModel
                    .builder()
                    .conditionDetail(commandResult.getConditionResult().getConditionDetail())
                    .guildChannelMember(GuildChannelMember
                            .builder()
                            .guild(commandContext.getGuild())
                            .textChannel(commandContext.getChannel())
                            .member(commandContext.getAuthor())
                            .build())
                    .build();
            channelService.sendEmbedTemplateInTextChannelList(GENERIC_COMMAND_EXCEPTION_MODEL_KEY, conditionModel, commandContext.getChannel());
        }
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent interaction, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.CONDITION)
                && commandResult.getConditionResult() != null &&
                !commandResult.getConditionResult().isResult()
                && commandResult.getConditionResult().getConditionDetail() != null
                && commandResult.getConditionResult().isReportResult()) {
            GenericConditionModel conditionModel = GenericConditionModel
                    .builder()
                    .conditionDetail(commandResult.getConditionResult().getConditionDetail())
                    .guildChannelMember(GuildChannelMember
                            .builder()
                            .guild(interaction.getGuild())
                            .textChannel((GuildChannel) interaction.getChannel())
                            .member(interaction.getMember())
                            .build())
                    .build();
            interactionService.replyEmbed(GENERIC_COMMAND_EXCEPTION_MODEL_KEY, conditionModel, interaction.getInteraction());
        }
    }

    @Override
    public boolean supportsSlash() {
        return true;
    }
}
