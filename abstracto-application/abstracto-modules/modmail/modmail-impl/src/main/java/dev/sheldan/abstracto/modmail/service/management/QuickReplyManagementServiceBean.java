package dev.sheldan.abstracto.modmail.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import dev.sheldan.abstracto.modmail.repository.QuickReplyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuickReplyManagementServiceBean implements QuickReplyManagementService {

    @Autowired
    private QuickReplyRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public Optional<QuickReply> getQuickReplyByName(String name, Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        return repository.getByNameIgnoreCaseAndServer(name, server);
    }

    @Override
    public QuickReply createQuickReply(String name, String content, AUserInAServer creator, boolean anonymous) {
        QuickReply quickReply = QuickReply
            .builder()
            .name(name)
            .additionalMessage(content)
            .anonymous(anonymous)
            .server(creator.getServerReference())
            .creator(creator)
            .build();
        return repository.save(quickReply);
    }

    @Override
    public void deleteQuickReply(String name, AServer server) {
        repository.deleteByNameAndServer(name, server);
    }

    @Override
    public List<QuickReply> getQuickReplies(AServer server) {
        return repository.findByServer(server);
    }

    @Override
    public List<QuickReply> getQuickRepliesContaining(String name, AServer server) {
        return repository.findByNameContainingIgnoreCaseAndServer(name, server);
    }


}
