package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FutureMemberPair;
import dev.sheldan.abstracto.core.models.MemberDisplayModel;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
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
        warnings.forEach(warning -> {
            CompletableFuture<Member> warningMemberFuture = memberService.getMemberInServerAsync(warning.getWarningUser());
            CompletableFuture<Member> warnedMemberFuture = memberService.getMemberInServerAsync(warning.getWarnedUser());
            FutureMemberPair futurePair = FutureMemberPair.builder().firstMember(warningMemberFuture).secondMember(warnedMemberFuture).build();
            loadedWarnings.put(warning.getWarnId(), futurePair);
            allFutures.add(warningMemberFuture);
            allFutures.add(warnedMemberFuture);
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
            MemberDisplayModel warnedUser = MemberDisplayModel
                    .builder()
                    .member(warnedMember)
                    .userId(warn.getWarnedUser().getUserReference().getId())
                    .build();

            Member warningMember = !memberPair.getFirstMember().isCompletedExceptionally() ? memberPair.getFirstMember().join() : null;
            MemberDisplayModel warningUser = MemberDisplayModel
                    .builder()
                    .member(warningMember)
                    .userId(warn.getWarningUser().getUserReference().getId())
                    .build();
            WarnEntry entry = WarnEntry
                    .builder()
                    .warnedUser(warnedUser)
                    .warningUser(warningUser)
                    .warning(warn)
                    .build();
            entries.add(entry);
        });
        return entries;
    }
}
