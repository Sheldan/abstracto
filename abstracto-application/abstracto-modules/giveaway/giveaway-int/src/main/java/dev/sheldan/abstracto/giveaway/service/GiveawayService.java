package dev.sheldan.abstracto.giveaway.service;

import dev.sheldan.abstracto.giveaway.model.GiveawayCreationRequest;
import dev.sheldan.abstracto.giveaway.model.database.Giveaway;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.concurrent.CompletableFuture;

public interface GiveawayService {
    CompletableFuture<Void> createGiveaway(GiveawayCreationRequest giveawayCreationRequest);
    CompletableFuture<Void> addGiveawayParticipant(Giveaway giveaway, Member member, MessageChannel messageChannel);
    CompletableFuture<Void> evaluateGiveaway(Long giveawayId, Long serverId);
    CompletableFuture<Void> cancelGiveaway(Long giveawayId, Long serverId);
}
