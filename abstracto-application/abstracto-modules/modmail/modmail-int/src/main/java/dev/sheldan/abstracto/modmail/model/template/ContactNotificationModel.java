package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@Getter
@Setter
@Builder
public class ContactNotificationModel {
    private UserDisplay userDisplay;
    private MessageChannel createdChannel;
}
