package dev.sheldan.abstracto.utility.models.template.commands.reminder;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;

@Getter
@Setter
@SuperBuilder
public class ExecutedReminderModel extends ServerContext {
    private Reminder reminder;
    private Member member;
    private Duration duration;

    public String getMessageUrl() {
        return MessageUtils.buildMessageUrl(this.reminder.getServer().getId() ,this.reminder.getChannel().getId(), this.reminder.getMessageId());
    }
}
