package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QuickRepliesListResponseModel {
    private List<QuickReplyListItem> quickReplies;

    public static QuickRepliesListResponseModel fromQuickReplies(List<QuickReply> quickReplies) {
        return QuickRepliesListResponseModel
                .builder()
                .quickReplies(quickReplies
                        .stream()
                        .map(QuickReplyListItem::fromQuickReply)
                        .collect(Collectors.toList()))
                .build();
    }
}
