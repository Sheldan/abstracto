package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

@Getter
@Setter
@Builder
public class ModMailUserReplyModel {
    private FullUser threadUser;
    private Message postedMessage;
    private ModMailThread modMailThread;
    private List<FullUser> subscribers;
}
