package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.UndoAction;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UndoActionServiceBean implements UndoActionService {

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageService messageService;

    @Override
    public void performActions(List<UndoActionInstance> actionsToPerform) {
        performActionsFuture(actionsToPerform);
    }

    @Override
    public CompletableFuture<Void> performActionsFuture(List<UndoActionInstance> actionsToPerform) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        log.info("Performing {} undo actions.", actionsToPerform.size());
        actionsToPerform.forEach(undoActionInstance -> {
            UndoAction action = undoActionInstance.getAction();
            List<Long> ids = undoActionInstance.getIds();
            if(action.equals(UndoAction.DELETE_CHANNEL)) {
                if(ids.size() != 2) {
                    log.error("Not the correct amount of ids provided for the channel deletion undo action.");
                    return;
                }
                log.info("Deleting channel {} in guild {} because of an UNDO action.", ids.get(1), ids.get(0));
                futures.add(deleteChannel(ids.get(0), ids.get(1)));
            } else if(action.equals(UndoAction.DELETE_MESSAGE)) {
                if(ids.size() != 3) {
                    log.error("Not the correct amount of ids provided for the message deletion undo action.");
                    return;
                }
                log.info("Deleting message {} in channel {} in server {} because of an UNDO action.", ids.get(2), ids.get(1), ids.get(0));
                futures.add(messageService.deleteMessageInChannelInServer(ids.get(0), ids.get(1), ids.get(2)));
            }
        });
        return FutureUtils.toSingleFutureGeneric(futures);
    }

    private CompletableFuture<Void> deleteChannel(Long serverId, Long channelId) {
        return channelService.deleteTextChannel(serverId, channelId).exceptionally(throwable -> {
            log.error("Failed to execute undo action channel delete for channel {} in server {}", channelId, serverId, throwable);
            return null;
        });
    }
}
