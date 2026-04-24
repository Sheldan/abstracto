package dev.sheldan.abstracto.modmail.model.listener;

import dev.sheldan.abstracto.core.listener.ListenerModel;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModmailThreadActionListenerModel implements ListenerModel {
    private long threadId;
    private ServerUser serverUser;
    private long serverId;
    private ModMailThreadState state;
    private Boolean appeal;
    private Instant created;
    private Instant updated;
    private long messageCount;
    private long subscriberCount;
    private long channelId;


}
