package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import dev.sheldan.abstracto.moderation.model.template.command.NoteEntryModel;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
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
public class UserNotesConverter {
    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserNotesConverter self;

    @Autowired
    private UserNoteManagementService userNoteManagementService;


    public CompletableFuture<List<NoteEntryModel>> fromNotes(List<UserNote> userNotes){
        List<CompletableFuture<Member>> memberFutures = new ArrayList<>();
        HashMap<ServerSpecificId, CompletableFuture<Member>> noteMemberMap = new HashMap<>();
        userNotes.forEach(userNote -> {
            CompletableFuture<Member> memberFuture = memberService.getMemberInServerAsync(userNote.getUser());
            memberFutures.add(memberFuture);
            noteMemberMap.put(userNote.getUserNoteId(), memberFuture);
        });
        if(userNotes.isEmpty()) {
            memberFutures.add(CompletableFuture.completedFuture(null));
        }

        return FutureUtils.toSingleFutureGeneric(memberFutures).thenApply(aVoid ->
            self.loadFullNotes(noteMemberMap)
        );
    }

    @Transactional
    public List<NoteEntryModel> loadFullNotes(Map<ServerSpecificId, CompletableFuture<Member>> futureHashMap) {
        List<NoteEntryModel> entryModels = new ArrayList<>();
        futureHashMap.keySet().forEach(serverSpecificId -> {
            CompletableFuture<Member> memberFuture = futureHashMap.get(serverSpecificId);
            Member member = !memberFuture.isCompletedExceptionally() ?  memberFuture.join() : null;
            UserNote note = userNoteManagementService.loadNote(serverSpecificId.getServerId(), serverSpecificId.getId());
            FullUserInServer fullUser = FullUserInServer
                    .builder()
                    .member(member)
                    .aUserInAServer(note.getUser())
                    .build();
            NoteEntryModel entryModel = NoteEntryModel
                    .builder()
                    .note(note)
                    .fullUser(fullUser)
                    .build();
            entryModels.add(entryModel);
        });
        return entryModels;
    }
}
