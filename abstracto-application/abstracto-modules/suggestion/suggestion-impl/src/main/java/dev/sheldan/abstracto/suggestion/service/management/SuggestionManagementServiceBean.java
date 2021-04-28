package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.suggestion.exception.SuggestionNotFoundException;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import dev.sheldan.abstracto.suggestion.repository.SuggestionRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
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
    public Suggestion createSuggestion(Member suggester, String text, Message message, Long suggestionId, Message commandMessage) {
        AUserInAServer user = userInServerManagementService.loadOrCreateUser(suggester);
        return this.createSuggestion(user, text, message, suggestionId, commandMessage);
    }

    @Override
    public Suggestion createSuggestion(AUserInAServer suggester, String text, Message createdMessage, Long suggestionId, Message commandMessage) {
        long channelId = createdMessage.getChannel().getIdLong();
        AChannel channel = channelManagementService.loadChannel(channelId);
        AChannel commandChannel = channelManagementService.loadChannel(commandMessage.getChannel().getIdLong());
        Suggestion suggestion = Suggestion
                .builder()
                .state(SuggestionState.NEW)
                .suggester(suggester)
                .suggestionText(text)
                .suggestionId(new ServerSpecificId(suggester.getServerReference().getId(), suggestionId))
                .server(suggester.getServerReference())
                .channel(channel)
                .commandChannel(commandChannel)
                .commandMessageId(commandMessage.getIdLong())
                .messageId(createdMessage.getIdLong())
                .build();
        log.info("Persisting suggestion {} at message {} in channel {} on server {} from user {}.",
                suggestionId, createdMessage.getId(), channelId, createdMessage.getGuild().getId(), suggester.getUserReference().getId());
        return suggestionRepository.save(suggestion);
    }

    @Override
    public Optional<Suggestion> getSuggestionOptional(Long serverId, Long suggestionId) {
        return suggestionRepository.findById(new ServerSpecificId(serverId, suggestionId));
    }

    @Override
    public Suggestion getSuggestion(Long serverId, Long suggestionId) {
        return getSuggestionOptional(serverId, suggestionId).orElseThrow(() -> new SuggestionNotFoundException(suggestionId));
    }

    @Override
    public void setSuggestionState(Suggestion suggestion, SuggestionState newState) {
        suggestion.setState(newState);
        log.info("Setting suggestion {} in server {} to state {}.", suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId(), newState);
        suggestionRepository.save(suggestion);
    }

    @Override
    public void deleteSuggestion(Long serverId, Long suggestionId) {
        deleteSuggestion(getSuggestion(serverId, suggestionId));
    }

    @Override
    public void deleteSuggestion(List<Suggestion> suggestions) {
        suggestionRepository.deleteAll(suggestions);
    }

    @Override
    public void deleteSuggestion(Suggestion suggestion) {
        log.info("Deleting suggestion {} in server {}.", suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
        suggestionRepository.delete(suggestion);
    }

    @Override
    public List<Suggestion> getSuggestionsUpdatedBeforeNotNew(Instant date) {
        return suggestionRepository.findByUpdatedLessThanAndStateNot(date, SuggestionState.NEW);
    }
}
