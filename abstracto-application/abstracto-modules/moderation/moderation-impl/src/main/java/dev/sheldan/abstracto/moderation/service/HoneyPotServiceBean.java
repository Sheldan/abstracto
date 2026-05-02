package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ConditionContextInstance;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ConditionService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.SystemCondition;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.HoneyPotFeatureConfig;
import dev.sheldan.abstracto.moderation.model.listener.HoneyPotReasonModel;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HoneyPotServiceBean {

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ConditionService conditionService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BanService banService;

    @Autowired
    private MemberService memberService;

    private static final String LEVEL_CONDITION_USER_ID_PARAMETER = "userId";
    private static final String LEVEL_CONDITION_LEVEL_PARAMETER = "level";
    private static final String LEVEL_CONDITION_SERVER_PARAMETER = "serverId";
    private static final String LEVEL_CONDITION_NAME = "HAS_LEVEL";
    private static final String HONEYPOT_BAN_REASON_TEMPLATE = "honeypot_ban_reason";


    public Long getHoneyPotRoleId(Long serverId) {
        return configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_ROLE_ID, serverId);
    }

    public boolean fellIntoHoneyPot(Long serverId, Member member) {
        Integer levelToSkipBan = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_LEVEL, serverId).intValue();
        Long amountOfSecondsToIgnore = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_JOIN_DURATION_SECONDS, serverId);
        boolean allowed =  userHasLevel(member, levelToSkipBan) || userJoinedLongerThanSeconds(member, amountOfSecondsToIgnore);
        return !allowed;
    }

    public boolean fellIntoHoneyPotIgnoringJoinDate(Long serverId, Member member) {
        Integer levelToSkipBan = configService.getLongValueOrConfigDefault(HoneyPotFeatureConfig.HONEYPOT_IGNORED_LEVEL, serverId).intValue();
        boolean allowed =  userHasLevel(member, levelToSkipBan);
        return !allowed;
    }

    public List<Member> getCurrentMembersWithHoneypotRole(Guild guild) {
        return memberService.getMembersWithRole(guild.getIdLong(), getHoneyPotRoleId(guild.getIdLong()));
    }

    public CompletableFuture<Void> banForHoneyPotMessage(Member targetMember, Long channelId) {
        HoneyPotReasonModel reasonModel = HoneyPotReasonModel
            .builder()
            .memberDisplay(MemberDisplay.fromMember(targetMember))
            .build();
        ServerUser bannedUser = ServerUser.fromMember(targetMember);
        String banReason = templateService.renderTemplate(HONEYPOT_BAN_REASON_TEMPLATE, reasonModel, bannedUser.getServerId());
        return banService.banUserWithNotification(bannedUser, banReason, ServerUser.fromMember(targetMember.getGuild().getSelfMember()),
            targetMember.getGuild(), Duration.ofDays(7)).thenAccept(banResult -> {
            log.info("Banned user {} in guild {} due to a message in channel {}.", bannedUser.getUserId(), bannedUser.getServerId(), channelId);
        }).exceptionally(throwable -> {
            log.error("Failed to ban user {} in guild {} due to a message in channel {}.", bannedUser.getUserId(), bannedUser.getServerId(), channelId, throwable);
            return null;
        });
    }

    public CompletableFuture<Void> banForHoneyPotRole(Member targetMember, Role role) {
        HoneyPotReasonModel reasonModel = HoneyPotReasonModel
            .builder()
            .memberDisplay(MemberDisplay.fromMember(targetMember))
            .roleDisplay(RoleDisplay.fromRole(role))
            .build();
        ServerUser bannedUser = ServerUser.fromMember(targetMember);
        String banReason = templateService.renderTemplate(HONEYPOT_BAN_REASON_TEMPLATE, reasonModel, bannedUser.getServerId());
        long roleId = role.getIdLong();
        return banService.banUserWithNotification(bannedUser, banReason, ServerUser.fromMember(targetMember.getGuild().getSelfMember()),
            targetMember.getGuild(), Duration.ofDays(7)).thenAccept(banResult -> {
            log.info("Banned user {} in guild {} due to role {}.", bannedUser.getUserId(), bannedUser.getServerId(), roleId);
        }).exceptionally(throwable -> {
            log.error("Failed to ban user {} in guild {} due to role {}.", bannedUser.getUserId(), bannedUser.getServerId(), roleId, throwable);
            return null;
        });
    }

    private boolean userHasLevel(Member member, Integer level) {
        log.info("Checking if member {} is ignored by the honeypot in server {}.", member.getIdLong(),member.getGuild().getIdLong());
        Map<String, Object> parameters = new HashMap<>();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        parameters.put(LEVEL_CONDITION_USER_ID_PARAMETER, userInAServer.getUserInServerId());
        parameters.put(LEVEL_CONDITION_LEVEL_PARAMETER, level);
        parameters.put(LEVEL_CONDITION_SERVER_PARAMETER, member.getGuild().getIdLong());
        ConditionContextInstance contextInstance = ConditionContextInstance
            .builder()
            .conditionName(LEVEL_CONDITION_NAME)
            .parameters(parameters)
            .build();
        SystemCondition.Result result = conditionService.checkConditions(contextInstance);
        return SystemCondition.Result.isSuccessful(result);
    }

    private boolean userJoinedLongerThanSeconds(Member member, Long seconds) {
        log.info("Checking if member {} joined the server more than {} seconds ago.", member.getIdLong(), seconds);
        // the incorrectness of timejoined should not matter, we chunk anyway
        return member.getTimeJoined().toInstant().isBefore(Instant.now().minus(seconds, ChronoUnit.SECONDS));
    }

}
