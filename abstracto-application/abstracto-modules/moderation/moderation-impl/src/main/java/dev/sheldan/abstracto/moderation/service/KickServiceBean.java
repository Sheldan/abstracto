package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class KickServiceBean implements KickService {

    public static final String KICK_LOG_TEMPLATE = "kick_log";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InfractionService infractionService;

    @Autowired
    private KickServiceBean self;

    @Override
    public CompletableFuture<Void> kickMember(Member member, String reason, KickLogModel kickLogModel)  {
        Guild guild = member.getGuild();
        log.info("Kicking user {} from guild {}", member.getUser().getIdLong(), guild.getIdLong());
        CompletableFuture<Void> kickFuture = guild.kick(member, reason).submit();
        CompletableFuture<Message> logFuture = this.sendKickLog(kickLogModel);
        return CompletableFuture.allOf(kickFuture, logFuture)
                .thenAccept(unused -> self.storeInfraction(member, reason, kickLogModel, guild, logFuture.join()));
    }

    @Transactional
    public CompletableFuture<Long> storeInfraction(Member member, String reason, KickLogModel kickLogModel, Guild guild, Message logMessage) {
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, guild.getIdLong())) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(ModerationFeatureConfig.KICK_INFRACTION_POINTS, guild.getIdLong());
            AUserInAServer kickedUser = userInServerManagementService.loadOrCreateUser(member);
            AUserInAServer kickingUser = userInServerManagementService.loadOrCreateUser(kickLogModel.getMember());
            return infractionService.createInfractionWithNotification(kickedUser, infractionPoints, KICK_INFRACTION_TYPE, reason, kickingUser, logMessage).thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Message> sendKickLog(KickLogModel kickLogModel)  {
        MessageToSend warnLogMessage = templateService.renderEmbedTemplate(KICK_LOG_TEMPLATE, kickLogModel, kickLogModel.getGuild().getIdLong());
        log.debug("Sending kick log message in guild {}.", kickLogModel.getGuild().getIdLong());
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(warnLogMessage, ModerationPostTarget.KICK_LOG, kickLogModel.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(messageFutures).thenApply(unused -> messageFutures.get(0).join());
    }
}
