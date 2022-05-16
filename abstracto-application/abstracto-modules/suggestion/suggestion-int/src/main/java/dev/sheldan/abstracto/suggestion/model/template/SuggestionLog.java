package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

@Getter
@Setter
@SuperBuilder
public class SuggestionLog {
    private Long suggestionId;
    private SuggestionState state;
    private User suggester;
    private Member member;
    private AUserInAServer suggesterUser;
    private String text;
    private String attachmentURL;
    private String reason;
    private Long serverId;
    private Long originalChannelId;
    private Long originalMessageId;
    private Boolean useButtons;
    private ButtonConfigModel agreeButtonModel;
    private ButtonConfigModel disAgreeButtonModel;
    private ButtonConfigModel removeVoteButtonModel;

    public String getOriginalMessageUrl() {
        return MessageUtils.buildMessageUrl(serverId, originalChannelId , originalMessageId);
    }
}
