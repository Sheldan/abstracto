package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

@Getter
@Setter
@Builder
public class ContactNotificationModel {
    private Member targetMember;
    private MessageChannel createdChannel;
}
