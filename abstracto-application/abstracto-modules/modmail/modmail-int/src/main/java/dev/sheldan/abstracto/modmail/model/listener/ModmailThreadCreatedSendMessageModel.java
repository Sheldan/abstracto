package dev.sheldan.abstracto.modmail.model.listener;

import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ModmailThreadCreatedSendMessageModel {
    private MessageToSend messageToSend;
    private Long serverId;
    private Long userId;
}
