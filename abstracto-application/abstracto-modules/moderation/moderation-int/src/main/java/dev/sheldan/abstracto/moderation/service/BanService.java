package dev.sheldan.abstracto.moderation.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.CompletableFuture;

public interface BanService {
    String BAN_EFFECT_KEY = "ban";
    CompletableFuture<Void> banMember(Member member, String reason, Member banningMember, Message message);
    CompletableFuture<Void> banUser(User user, String reason, Member banningMember, Message message);
    CompletableFuture<Void> unBanUser(User user, Member unBanningUser);
}
