package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.model.BanResult;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedLogModel;
import dev.sheldan.abstracto.moderation.model.template.listener.UserUnBannedLogModel;
import net.dv8tion.jda.api.entities.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface BanService {
    String BAN_EFFECT_KEY = "ban";
    String BAN_INFRACTION_TYPE = "ban";
    String INFRACTION_PARAMETER_DELETION_DURATION_KEY = "DELETION_DURATION";
    CompletableFuture<BanResult> banUserWithNotification(ServerUser userToBeBanned, String reason, ServerUser banningUser, Guild guild, Duration deletionDuration);
    CompletableFuture<Void> banUser(Guild guild, ServerUser userToBeBanned, Duration deletionDuration, String reason);
    CompletableFuture<Void> unbanUser(Guild guild, User user, Member memberPerforming);
    CompletableFuture<Void> sendBanLogMessage(UserBannedLogModel model, Long serverId);
    CompletableFuture<Void> sendUnBanLogMessage(UserUnBannedLogModel model, Long serverId);
}
