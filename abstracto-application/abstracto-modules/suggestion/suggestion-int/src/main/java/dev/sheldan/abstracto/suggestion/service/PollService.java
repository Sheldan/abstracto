package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.suggestion.model.database.PollType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PollService {

    String SERVER_POLL_DURATION_SECONDS = "serverPollDurationSeconds";
    String QUICK_POLL_DURATION_SECONDS = "quickPollDurationSeconds";

    CompletableFuture<Void> createServerPoll(Member creator, List<String> options, String description,
                                             Boolean allowMultiple, Boolean allowAddition, Boolean showDecisions, Duration duration);

    CompletableFuture<Void> createQuickPoll(Member creator, List<String> options, String description,
                                             Boolean allowMultiple, Boolean showDecisions, InteractionHook interactionHook, Duration duration);

    CompletableFuture<Void> setDecisionsInPollTo(Member voter, List<String> chosenValues, Long pollId, PollType pollType);
    CompletableFuture<Void> addOptionToServerPoll(Long pollId, Long serverId, Member adder, String label, String description);
    CompletableFuture<Void> evaluateServerPoll(Long pollId, Long serverId);
    CompletableFuture<Void> remindServerPoll(Long pollId, Long serverId);
    CompletableFuture<Void> evaluateQuickPoll(Long pollId, Long serverId);
    CompletableFuture<Void> closePoll(Long pollId, Long serverId, String text, Member cause);
    CompletableFuture<Void> cancelPoll(Long pollId, Long serverId, Member cause);
}
