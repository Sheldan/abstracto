package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.repository.SuggestionRepository;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class SuggestionManagementServiceBean implements SuggestionManagementService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public Suggestion createSuggestion(Member suggester, String text, Message message, Long suggestionId) {
        AUserInAServer user = userInServerManagementService.loadUser(suggester);
        return this.createSuggestion(user, text, message, suggestionId);
    }

    @Override
    public Suggestion createSuggestion(AUserInAServer suggester, String text, Message message, Long suggestionId) {
        long channelId = message.getChannel().getIdLong();
        AChannel channel = channelManagementService.loadChannel(channelId);
        Suggestion suggestion = Suggestion
                .builder()
                .state(SuggestionState.NEW)
                .suggester(suggester)
                .id(suggestionId)
                .server(suggester.getServerReference())
                .suggestionDate(Instant.now())
                .channel(channel)
                .messageId(message.getIdLong())
                .build();
        suggestionRepository.save(suggestion);
        return suggestion;
    }

    @Override
    public Optional<Suggestion> getSuggestion(Long suggestionId) {
        return suggestionRepository.findById(suggestionId);
    }


    @Override
    public void setSuggestionState(Suggestion suggestion, SuggestionState newState) {
        suggestion.setState(newState);
        suggestionRepository.save(suggestion);
    }
}
