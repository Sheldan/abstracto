package dev.sheldan.abstracto.utility.models.template;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.utility.models.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@SuperBuilder
public class ExecutedReminderModel extends ServerContext {
    private Reminder reminder;
    private Member member;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.reminder.getServer().getId() ,this.reminder.getChannel().getId(), this.reminder.getMessageId());
    }
}
