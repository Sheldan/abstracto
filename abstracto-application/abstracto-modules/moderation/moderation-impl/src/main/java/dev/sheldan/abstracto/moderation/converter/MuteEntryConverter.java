package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.MemberDisplayModel;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.template.command.MuteEntry;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class MuteEntryConverter {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private MuteEntryConverter self;

    public CompletableFuture<List<MuteEntry>> fromMutes(List<Mute> mutes) {
        Map<ServerSpecificId, FutureMemberPair> loadedMutes = new HashMap<>();
        List<CompletableFuture<Member>> allFutures = new ArrayList<>();
        mutes.forEach(mute -> {
            CompletableFuture<Member> mutingMemberFuture = memberService.getMemberInServerAsync(mute.getMutingUser());
            CompletableFuture<Member> mutedMemberFuture = memberService.getMemberInServerAsync(mute.getMutedUser());
            FutureMemberPair futurePair = FutureMemberPair
                    .builder()
                    .firstMember(mutingMemberFuture)
                    .secondMember(mutedMemberFuture)
                    .build();
            loadedMutes.put(mute.getMuteId(), futurePair);
            allFutures.add(mutingMemberFuture);
            allFutures.add(mutedMemberFuture);
        });
        CompletableFuture<List<MuteEntry>> future = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(allFutures)
                .whenComplete((unused, throwable) -> future.complete(self.loadFullMuteEntries(loadedMutes)))
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
        return future;
    }

    @Transactional
    public List<MuteEntry> loadFullMuteEntries(Map<ServerSpecificId, FutureMemberPair> loadedMuteInfo) {
        List<MuteEntry> entries = new ArrayList<>();
        List<ServerSpecificId> muteIds = new ArrayList<>(loadedMuteInfo.keySet());
        muteIds.sort(Comparator.comparing(ServerSpecificId::getId));
        muteIds.forEach(muteInfo -> {
            FutureMemberPair memberPair = loadedMuteInfo.get(muteInfo);
            Mute mute = muteManagementService.findMute(muteInfo.getId(), muteInfo.getServerId());
            Member mutedMember = !memberPair.getSecondMember().isCompletedExceptionally() ? memberPair.getSecondMember().join() : null;
            MemberDisplayModel mutedUser = MemberDisplayModel
                    .builder()
                    .member(mutedMember)
                    .userId(mute.getMutedUser().getUserReference().getId())
                    .build();

            Member mutingMember = !memberPair.getFirstMember().isCompletedExceptionally() ? memberPair.getFirstMember().join() : null;
            MemberDisplayModel mutingUser = MemberDisplayModel
                    .builder()
                    .member(mutingMember)
                    .userId(mute.getMutingUser().getUserReference().getId())
                    .build();
            MuteEntry entry = MuteEntry
                    .builder()
                    .mutedUser(mutedUser)
                    .mutingUser(mutingUser)
                    .mute(mute)
                    .muteDuration(Duration.between(mute.getMuteDate(), mute.getMuteTargetDate()))
                    .build();
            entries.add(entry);
        });
        return entries;
    }
}
