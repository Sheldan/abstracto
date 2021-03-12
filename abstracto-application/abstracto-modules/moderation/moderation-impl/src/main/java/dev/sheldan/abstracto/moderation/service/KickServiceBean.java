package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.mode.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private FeatureModeService featureModeService;

    @Override
    public CompletableFuture<Void> kickMember(Member member, String reason, KickLogModel kickLogModel)  {
        Guild guild = member.getGuild();
        log.info("Kicking user {} from guild {}", member.getUser().getIdLong(), guild.getIdLong());
        CompletableFuture<Void> kickFuture = guild.kick(member, reason).submit();
        CompletableFuture<Void> logFuture = this.sendKickLog(kickLogModel);
        return CompletableFuture.allOf(kickFuture, logFuture);
    }

    private CompletableFuture<Void> sendKickLog(KickLogModel kickLogModel)  {
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatureDefinition.MODERATION, kickLogModel.getGuild().getIdLong(), ModerationMode.KICK_LOG)) {
            MessageToSend warnLogMessage = templateService.renderEmbedTemplate(KICK_LOG_TEMPLATE, kickLogModel, kickLogModel.getGuild().getIdLong());
            log.trace("Sending kick log message in guild {}.", kickLogModel.getGuild().getIdLong());
            completableFuture = FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(warnLogMessage, ModerationPostTarget.KICK_LOG, kickLogModel.getGuild().getIdLong()));
        } else {
            log.trace("Feature {} has mode {} for logging disabled for server {}. Not sending kick notification.", ModerationFeatureDefinition.MODERATION, ModerationMode.BAN_LOG, kickLogModel.getGuild().getIdLong());
            completableFuture = CompletableFuture.completedFuture(null);
        }
        return completableFuture;
    }
}
