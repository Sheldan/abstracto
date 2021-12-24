package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.template.command.WarnEntry;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class WarnEntryConverter {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private WarnEntryConverter self;

    public CompletableFuture<List<WarnEntry>> fromWarnings(List<Warning> warnings) {
        Map<ServerSpecificId, FutureMemberPair> loadedWarnings = new HashMap<>();
        List<CompletableFuture<Member>> allFutures = new ArrayList<>();
        Map<Long, CompletableFuture<Member>> memberCaching = new HashMap<>();
        warnings.forEach(warning -> {
            AUserInAServer warningUser = warning.getWarningUser();
            AUserInAServer warnedUser = warning.getWarnedUser();
            CompletableFuture<Member> warnedFuture;
            if(memberCaching.containsKey(warnedUser.getUserInServerId())) {
                warnedFuture = memberCaching.get(warnedUser.getUserInServerId());
            } else {
                warnedFuture = memberService.getMemberInServerAsync(warnedUser);
                memberCaching.put(warnedUser.getUserInServerId(), warnedFuture);
            }
            CompletableFuture<Member> warningFuture;
            if(memberCaching.containsKey(warningUser.getUserInServerId())) {
                warningFuture = memberCaching.get(warningUser.getUserInServerId());
            } else {
                warningFuture = memberService.getMemberInServerAsync(warningUser);
                memberCaching.put(warningUser.getUserInServerId(), warningFuture);
            }
            FutureMemberPair futurePair = FutureMemberPair.builder().firstMember(warningFuture).secondMember(warnedFuture).build();
            loadedWarnings.put(warning.getWarnId(), futurePair);
            allFutures.add(warningFuture);
            allFutures.add(warnedFuture);
        });
        CompletableFuture<List<WarnEntry>> future = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(allFutures)
                .whenComplete((unused, throwable) -> future.complete(self.loadFullWarnEntries(loadedWarnings)))
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
        return future;
    }

    @Transactional
    public List<WarnEntry> loadFullWarnEntries(Map<ServerSpecificId, FutureMemberPair> loadedWarnInfo) {
        List<ServerSpecificId> warnIds = new ArrayList<>(loadedWarnInfo.keySet());
        warnIds.sort(Comparator.comparing(ServerSpecificId::getId));
        List<WarnEntry> entries = new ArrayList<>();
        warnIds.forEach(warning -> {
            Warning warn = warnManagementService.findById(warning.getId(), warning.getServerId());
            FutureMemberPair memberPair = loadedWarnInfo.get(warning);
            Member warnedMember = !memberPair.getSecondMember().isCompletedExceptionally() ? memberPair.getSecondMember().join() : null;
            MemberDisplay warnedUser = MemberDisplay
                    .builder()
                    .memberMention(warnedMember != null ? warnedMember.getAsMention() : null)
                    .userId(warn.getWarnedUser().getUserReference().getId())
                    .build();

            Member warningMember = !memberPair.getFirstMember().isCompletedExceptionally() ? memberPair.getFirstMember().join() : null;
            MemberDisplay warningUser = MemberDisplay
                    .builder()
                    .memberMention(warningMember != null ? warningMember.getAsMention() : null)
                    .userId(warn.getWarningUser().getUserReference().getId())
                    .build();
            WarnEntry entry = WarnEntry
                    .builder()
                    .warnedUser(warnedUser)
                    .warningUser(warningUser)
                    .reason(warn.getReason())
                    .decayDate(warn.getDecayDate())
                    .decayed(warn.getDecayed())
                    .warnId(warn.getWarnId().getId())
                    .warnDate(warn.getWarnDate())
                    .serverId(warn.getWarnId().getServerId())
                    .build();
            entries.add(entry);
        });
        return entries;
    }
}
