package dev.sheldan.abstracto.assignableroles.service.management;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.exceptions.AssignablePlacePostNotFoundException;
import dev.sheldan.abstracto.assignableroles.repository.AssignableRolePlacePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssignableRolePlacePostManagementServiceBean implements AssignableRolePlacePostManagementService {

    @Autowired
    private AssignableRolePlacePostRepository repository;


    @Override
    public Optional<AssignableRolePlacePost> findByMessageIdOptional(Long messageId) {
        return repository.findById(messageId);
    }

    @Override
    public AssignableRolePlacePost findByMessageId(Long messageId) {
        return findByMessageIdOptional(messageId).orElseThrow(() -> new AssignablePlacePostNotFoundException(messageId));
    }

}
