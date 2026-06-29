package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuickReplyListItem {
    private String name;
    private String content;
    private boolean anonymous;
    private MemberDisplay creator;

    public static QuickReplyListItem fromQuickReply(QuickReply quickReply) {
        MemberDisplay creatorObj = MemberDisplay.fromAUserInAServer(quickReply.getCreator());

        return QuickReplyListItem
                .builder()
                .name(quickReply.getName())
                .content(quickReply.getAdditionalMessage())
                .creator(creatorObj)
                .anonymous(quickReply.getAnonymous())
                .build();
    }
}
