package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
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
        Map<Long, CompletableFuture<Member>> memberCaching = new HashMap<>();
        userNotes.forEach(userNote -> {
            AUserInAServer noteUser = userNote.getUser();
            CompletableFuture<Member> noteFuture;
            if(memberCaching.containsKey(noteUser.getUserInServerId())) {
                noteFuture = memberCaching.get(noteUser.getUserInServerId());
            } else {
                noteFuture = memberService.getMemberInServerAsync(noteUser);
                memberCaching.put(noteUser.getUserInServerId(), noteFuture);
            }
            memberFutures.add(noteFuture);
            noteMemberMap.put(userNote.getUserNoteId(), noteFuture);
        });
        if(userNotes.isEmpty()) {
            memberFutures.add(CompletableFuture.completedFuture(null));
        }
        CompletableFuture<List<NoteEntryModel>> future = new CompletableFuture<>();
        FutureUtils.toSingleFutureGeneric(memberFutures)
            .whenComplete((unused, throwable) -> future.complete(self.loadFullNotes(noteMemberMap)))
            .exceptionally(throwable -> {
                future.completeExceptionally(throwable);
                return null;
            });
        return future;
    }

    @Transactional
    public List<NoteEntryModel> loadFullNotes(Map<ServerSpecificId, CompletableFuture<Member>> futureHashMap) {
        List<NoteEntryModel> entryModels = new ArrayList<>();
        futureHashMap.keySet().forEach(serverSpecificId -> {
            CompletableFuture<Member> memberFuture = futureHashMap.get(serverSpecificId);
            Member member = !memberFuture.isCompletedExceptionally() ?  memberFuture.join() : null;
            UserNote note = userNoteManagementService.loadNote(serverSpecificId.getServerId(), serverSpecificId.getId());
            MemberDisplay display = MemberDisplay
                    .builder()
                    .userId(note.getUser().getUserReference().getId())
                    .serverId(note.getServer().getId())
                    .memberMention(member != null ? member.getAsMention() : null)
                    .build();
            NoteEntryModel entryModel = NoteEntryModel
                    .builder()
                    .member(display)
                    .serverId(serverSpecificId.getServerId())
                    .note(note.getNote())
                    .noteId(note.getUserNoteId().getId())
                    .created(note.getCreated())
                    .build();
            entryModels.add(entryModel);
        });
        return entryModels;
    }
}
