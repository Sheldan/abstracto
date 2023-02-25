package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@SuperBuilder
public class SuggestionUpdateModel {
    private Long suggestionId;
    private SuggestionState state;
    private User suggester;
    private Member member;
    private String text;
    private Message message;
    private String reason;
    private Long serverId;
    private Long originalChannelId;
    private Long originalMessageId;
    private Long agreeVotes;
    private Long disAgreeVotes;
    private Long totalVotes;
    private Float agreementPercentage;
    private Float disAgreementPercentage;

    public String getOriginalMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId, originalChannelId , originalMessageId);
    }
}
