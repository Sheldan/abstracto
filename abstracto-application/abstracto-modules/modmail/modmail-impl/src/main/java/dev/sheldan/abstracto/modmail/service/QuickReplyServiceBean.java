package dev.sheldan.abstracto.modmail.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.modmail.exception.QuickReplyExistsException;
import dev.sheldan.abstracto.modmail.exception.QuickReplyNotFoundException;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import dev.sheldan.abstracto.modmail.service.management.QuickReplyManagementService;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuickReplyServiceBean implements QuickReplyService {
    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private QuickReplyManagementService quickReplyManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public QuickReply createQuickReply(String name, String content, Member creator, boolean anonymous) {
        if(quickReplyManagementService.getQuickReplyByName(name, creator.getGuild().getIdLong()).isPresent()) {
            throw new QuickReplyExistsException();
        }
        AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
        return quickReplyManagementService.createQuickReply(name, content, creatorUser, anonymous);
    }

    @Override
    public void deleteQuickReply(String name, Guild guild) {
        if(quickReplyManagementService.getQuickReplyByName(name, guild.getIdLong()).isEmpty()) {
            throw new QuickReplyNotFoundException();
        }
        AServer server = serverManagementService.loadServer(guild);
        quickReplyManagementService.deleteQuickReply(name, server);
    }

    @Override
    public List<QuickReply> getQuickReplies(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return quickReplyManagementService.getQuickReplies(server);
    }

    @Override
    public Optional<QuickReply> getQuickReply(String name, Guild guild) {
        return quickReplyManagementService.getQuickReplyByName(name, guild.getIdLong());
    }

    @Override
    public List<QuickReply> getQuickRepliesContaining(String name, Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        return quickReplyManagementService.getQuickRepliesContaining(name, server);
    }

}
