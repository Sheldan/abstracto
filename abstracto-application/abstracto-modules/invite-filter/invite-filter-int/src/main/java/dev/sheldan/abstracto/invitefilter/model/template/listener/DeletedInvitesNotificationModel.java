package dev.sheldan.abstracto.invitefilter.model.template.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

@Getter
@Setter
@Builder
public class DeletedInvitesNotificationModel {
    private Guild guild;
    private MessageChannel channel;
    private Member author;
    private Message message;
    private List<DeletedInvite> invites;
}
