package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.listener.HoneyPotServiceBean;
import dev.sheldan.abstracto.moderation.model.template.command.HoneyPotBanResponseModel;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class HoneyPotBan extends AbstractConditionableCommand {

    private static final String DURATION_PARAMETER = "duration";
    private static final String HONEYPOT_BAN_RESPONSE = "honeypotBan_response";
    private static final String HONEYPOT_BAN_COMMAND = "honeypotBan";

    @Autowired
    private HoneyPotBan self;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private HoneyPotServiceBean honeyPotServiceBean;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ConfigService configService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Duration duration;
        if(slashCommandParameterService.hasCommandOption(DURATION_PARAMETER, event)) {
            String durationStr = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
            duration = ParseUtils.parseDuration(durationStr);
        } else {
            Long ignoredSeconds =
                configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_JOIN_DURATION_SECONDS, event.getGuild().getIdLong());
            duration = Duration.ofSeconds(ignoredSeconds);
        }

        return event.deferReply(false).submit()
            .thenCompose(hook -> self.banEveryHoneypotMember(hook, event.getGuild(), duration))
            .thenCompose(banResponse -> self.sendResponse(banResponse))
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<Void> sendResponse(Pair<InteractionHook, Integer> banResponse) {
        HoneyPotBanResponseModel responseModel = HoneyPotBanResponseModel
            .builder()
            .bannedMemberCount(banResponse.getRight())
            .build();
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(HONEYPOT_BAN_RESPONSE, responseModel, banResponse.getLeft()));
    }

    @Transactional
    public CompletableFuture<Pair<InteractionHook, Integer>> banEveryHoneypotMember(InteractionHook hook, Guild guild, Duration duration) {
        Instant maxJoinAge;
        if(duration != null) {
            maxJoinAge = Instant.now().minus(duration);
        } else {
            maxJoinAge = Instant.now();
        }
        List<Member> currentMembersWithHoneypotRole = honeyPotServiceBean.getCurrentMembersWithHoneypotRole(guild)
            .stream().filter(member -> member.getTimeJoined().toInstant().isBefore(maxJoinAge))
            .toList();
        Role honeyPotRole = guild.getRoleById(honeyPotServiceBean.getHoneyPotRoleId(guild.getIdLong()));
        List<CompletableFuture<Void>> futures = currentMembersWithHoneypotRole.stream().map(member ->
            honeyPotServiceBean.banForHoneyPot(member, honeyPotRole)
        ).toList();
        Integer memberCount = currentMembersWithHoneypotRole.size();
        CompletableFutureList<Void> futureList = new CompletableFutureList<>(futures);
        return futureList.getMainFuture()
            .thenApply(unused -> Pair.of(hook, memberCount));
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter durationParameter = Parameter
            .builder()
            .name(DURATION_PARAMETER)
            .templated(true)
            .type(String.class)
            .optional(true)
            .build();


        List<Parameter> parameters = Arrays.asList(durationParameter);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .hasExample(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(ModerationSlashCommandNames.MODERATION)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .commandName("honeypotban")
            .build();

        return CommandConfiguration.builder()
            .name(HONEYPOT_BAN_COMMAND)
            .module(ModerationModuleDefinition.MODERATION)
            .templated(true)
            .async(true)
            .slashCommandConfig(slashCommandConfig)
            .slashCommandOnly(true)
            .supportsEmbedException(true)
            .parameters(parameters)
            .help(helpInfo)
            .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.HONEYPOT;
    }
}
