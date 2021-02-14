package dev.sheldan.abstracto.moderation.models.template.listener;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@Getter
@Setter
@Builder
public class DeletedInvitesNotificationModel {
    private Guild guild;
    private TextChannel channel;
    private Member author;
    private Message message;
    private List<DeletedInvite> invites;
}
