package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.MemberService;
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
    private MuteManagementService muteManagementService;

    @Autowired
    private MuteEntryConverter self;

    public CompletableFuture<List<MuteEntry>> fromMutes(List<Mute> mutes) {
        Map<ServerSpecificId, FutureMemberPair> loadedMutes = new HashMap<>();
        List<CompletableFuture<Member>> allFutures = new ArrayList<>();
        Map<Long, CompletableFuture<Member>> memberCaching = new HashMap<>();
        mutes.forEach(mute -> {
            AUserInAServer mutingUser = mute.getMutingUser();
            AUserInAServer mutedUser = mute.getMutedUser();
            CompletableFuture<Member> mutedFuture;
            if(memberCaching.containsKey(mutedUser.getUserInServerId())) {
                mutedFuture = memberCaching.get(mutedUser.getUserInServerId());
            } else {
                mutedFuture = memberService.getMemberInServerAsync(mutedUser);
                memberCaching.put(mutedUser.getUserInServerId(), mutedFuture);
            }
            CompletableFuture<Member> mutingFuture;
            if(memberCaching.containsKey(mutingUser.getUserInServerId())) {
                mutingFuture = memberCaching.get(mutingUser.getUserInServerId());
            } else {
                mutingFuture = memberService.getMemberInServerAsync(mutingUser);
                memberCaching.put(mutingUser.getUserInServerId(), mutingFuture);
            }
            FutureMemberPair futurePair = FutureMemberPair
                    .builder()
                    .firstMember(mutingFuture)
                    .secondMember(mutedFuture)
                    .build();
            loadedMutes.put(mute.getMuteId(), futurePair);
            allFutures.add(mutingFuture);
            allFutures.add(mutedFuture);
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
            MemberDisplay mutedUser = MemberDisplay
                    .builder()
                    .memberMention(mutedMember != null ? mutedMember.getAsMention() : null)
                    .userId(mute.getMutedUser().getUserReference().getId())
                    .serverId(mute.getServer().getId())
                    .build();

            Member mutingMember = !memberPair.getFirstMember().isCompletedExceptionally() ? memberPair.getFirstMember().join() : null;
            MemberDisplay mutingUser = MemberDisplay
                    .builder()
                    .memberMention(mutingMember != null ? mutingMember.getAsMention() : null)
                    .userId(mute.getMutingUser().getUserReference().getId())
                    .build();
            MuteEntry entry = MuteEntry
                    .builder()
                    .mutedUser(mutedUser)
                    .mutingUser(mutingUser)
                    .muteId(mute.getMuteId().getId())
                    .serverId(mute.getMuteId().getServerId())
                    .reason(mute.getReason())
                    .muteDate(mute.getMuteDate())
                    .muteEnded(mute.getMuteEnded())
                    .muteDuration(Duration.between(mute.getMuteDate(), mute.getMuteTargetDate()))
                    .build();
            entries.add(entry);
        });
        return entries;
    }
}
