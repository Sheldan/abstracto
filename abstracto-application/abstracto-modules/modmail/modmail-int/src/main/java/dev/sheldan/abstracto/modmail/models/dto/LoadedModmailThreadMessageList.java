package dev.sheldan.abstracto.modmail.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class LoadedModmailThreadMessageList {
    private List<LoadedModmailThreadMessage> messageList;
    public List<CompletableFuture> getAllFutures() {
        List<CompletableFuture> futures = new ArrayList<>();
        messageList.forEach(loadedModmailThreadMessage -> {
            futures.add(loadedModmailThreadMessage.getMemberFuture());
            futures.add(loadedModmailThreadMessage.getMessageFuture());
        });
        return futures;
    }
}
