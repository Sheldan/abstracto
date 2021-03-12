package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.FutureMemberPair;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // TODO maybe optimize to not need to look into the cache twice
        warnings.forEach(warning -> {
            CompletableFuture<Member> warningMemberFuture = memberService.getMemberInServerAsync(warning.getWarningUser());
            CompletableFuture<Member> warnedMemberFuture = memberService.getMemberInServerAsync(warning.getWarnedUser());
            FutureMemberPair futurePair = FutureMemberPair.builder().firstMember(warningMemberFuture).secondMember(warnedMemberFuture).build();
            loadedWarnings.put(warning.getWarnId(), futurePair);
            allFutures.add(warningMemberFuture);
            allFutures.add(warnedMemberFuture);
        });
        return FutureUtils.toSingleFutureGeneric(allFutures).thenApply(aVoid ->
            self.loadFullWarnEntries(loadedWarnings)
        );

    }

    @Transactional
    public List<WarnEntry> loadFullWarnEntries(Map<ServerSpecificId, FutureMemberPair> loadedWarnInfo) {
        List<WarnEntry> entries = new ArrayList<>();
        loadedWarnInfo.keySet().forEach(warning -> {
            Warning warn = warnManagementService.findById(warning.getId(), warning.getServerId());
            FutureMemberPair memberPair = loadedWarnInfo.get(warning);
            Member warnedMember = !memberPair.getSecondMember().isCompletedExceptionally() ? memberPair.getSecondMember().join() : null;
            FullUserInServer warnedUser = FullUserInServer
                    .builder()
                    .member(warnedMember)
                    .aUserInAServer(warn.getWarnedUser())
                    .build();

            Member warningMember = !memberPair.getFirstMember().isCompletedExceptionally() ? memberPair.getFirstMember().join() : null;
            FullUserInServer warningUser = FullUserInServer
                    .builder()
                    .member(warningMember)
                    .aUserInAServer(warn.getWarningUser())
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
