package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@Builder
public class ModMailModeratorReplyModel {
    private FullUser threadUser;
    private Member moderator;
    private String text;
    private Message postedMessage;
    private Boolean anonymous;
    private ModMailThread modMailThread;
}
