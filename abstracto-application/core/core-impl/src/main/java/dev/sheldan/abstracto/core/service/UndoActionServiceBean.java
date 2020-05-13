package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.UndoAction;
import dev.sheldan.abstracto.core.models.UndoActionInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UndoActionServiceBean implements UndoActionService {

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Override
    public void performActions(List<UndoActionInstance> actionsToPerform) {
        actionsToPerform.forEach(undoActionInstance -> {
            UndoAction action = undoActionInstance.getAction();
            List<Long> ids = undoActionInstance.getIds();
            switch (action) {
                case DELETE_CHANNEL:
                    if(ids.size() != 2) {
                        log.error("Not the correct amount of ids provided for the channel deletion undo action.");
                        break;
                    }
                    deleteChannel(ids.get(0), ids.get(1));
                    break;
                case DELETE_MESSAGE:
                    if(ids.size() != 2) {
                        log.error("Not the correct amount of ids provided for the message deletion undo action.");
                        break;
                    }
                    botService.deleteMessage(ids.get(0), ids.get(1));
            }
        });
    }

    private void deleteChannel(Long serverId, Long channelId) {
        channelService.deleteTextChannel(serverId, channelId).exceptionally((throwable) -> {
            log.error("Failed to execute undo action channel delete for channel {} in server {}", channelId, serverId, throwable);
            return null;
        });
    }
}
