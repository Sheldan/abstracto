package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.UndoActionException;
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
            switch (action) {
                case DELETE_CHANNEL:
                    if(undoActionInstance.getIds().size() != 2) {
                        throw new UndoActionException("Not the correct amount of ides provided for the channel deletion undo action");
                    }
                    deleteChannel(undoActionInstance.getIds().get(0), undoActionInstance.getIds().get(1));
                    break;
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
