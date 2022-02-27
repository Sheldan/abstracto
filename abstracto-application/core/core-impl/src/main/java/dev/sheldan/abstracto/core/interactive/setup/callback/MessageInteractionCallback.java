package dev.sheldan.abstracto.core.interactive.setup.callback;

import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Builder
@Getter
public class MessageInteractionCallback {
    private Long serverId;
    private Long channelId;
    private Long userId;
    private Consumer<MessageReceivedModel> action;
    private Consumer<MessageReceivedModel> timeoutAction;
    private Instant timeoutPoint;

    public Predicate<MessageReceivedModel> condition;
}
