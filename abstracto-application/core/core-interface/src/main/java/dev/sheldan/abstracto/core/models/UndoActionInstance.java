package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@Setter
public class UndoActionInstance {
    private List<Long> ids;
    private UndoAction action;

    public static UndoActionInstance getChannelDeleteAction(Long serverId, Long channelId) {
        return UndoActionInstance
                .builder()
                .action(UndoAction.DELETE_CHANNEL)
                .ids(Arrays.asList(serverId, channelId))
                .build();
    }

    public static UndoActionInstance getMessageDeleteAction(Long serverId, Long channelId, Long messageId) {
        return UndoActionInstance
                .builder()
                .action(UndoAction.DELETE_MESSAGE)
                .ids(Arrays.asList(serverId, channelId, messageId))
                .build();
    }
}
