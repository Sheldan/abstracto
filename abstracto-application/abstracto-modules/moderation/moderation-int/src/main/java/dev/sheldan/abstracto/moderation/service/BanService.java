package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.moderation.model.BanResult;
import net.dv8tion.jda.api.entities.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface BanService {
    String BAN_EFFECT_KEY = "ban";
    String BAN_INFRACTION_TYPE = "ban";
    String INFRACTION_PARAMETER_DELETION_DURATION_KEY = "DELETION_DURATION";
    CompletableFuture<BanResult> banUserWithNotification(User user, String reason, Member banningMember, Duration deletionDuration);
    CompletableFuture<Void> unBanUserWithNotification(User user, Member unBanningUser);
    CompletableFuture<Void> banUser(Guild guild,  User user, Duration deletionDuration, String reason);
    CompletableFuture<Void> unbanUser(Guild guild,  User user);
    CompletableFuture<Void> softBanUser(Guild guild, User user, Duration delDays);
}
