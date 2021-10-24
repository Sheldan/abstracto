package dev.sheldan.abstracto.moderation.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface BanService {
    String BAN_EFFECT_KEY = "ban";
    CompletableFuture<Void> banMemberWithNotification(Member member, String reason, Member banningMember, Integer deletionDays, Message message);
    CompletableFuture<Void> banUserWithNotification(User user, String reason, Member banningMember, Integer deletionDays, Message message);
    CompletableFuture<Void> unBanUserWithNotification(User user, Member unBanningUser);
    CompletableFuture<Void> banUser(Guild guild,  User user, Integer deletionDays, String reason);
    CompletableFuture<Void> unbanUser(Guild guild,  User user);
    CompletableFuture<Void> softBanUser(Guild guild, User user, Duration delDays);
}
