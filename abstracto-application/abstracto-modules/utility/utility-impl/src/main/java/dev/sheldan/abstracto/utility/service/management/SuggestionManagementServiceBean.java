package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.UserConverter;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.repository.SuggestionRepository;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SuggestionManagementServiceBean {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserInServerConverter userInServerConverter;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private UserService userService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelConverter channelConverter;

    public Suggestion createSuggestion(Member suggester, String text) {
        UserInServerDto userDto = userService.loadUser(suggester);
        return this.createSuggestion(userDto, text);
    }

    public Suggestion createSuggestion(UserInServerDto suggester, String text) {
        AUserInAServer aUserInAServer = userInServerConverter.fromDto(suggester);
        Suggestion suggestion = Suggestion
                .builder()
                .state(SuggestionState.NEW)
                .suggester(aUserInAServer)
                .suggestionDate(Instant.now())
                .build();
        suggestionRepository.save(suggestion);
        return suggestion;
    }

    public Suggestion getSuggestion(Long suggestionId) {
        return suggestionRepository.getOne(suggestionId);
    }


    public void setPostedMessage(Suggestion suggestion, Message message) {
        suggestion.setMessageId(message.getIdLong());
        suggestion.setChannel(AChannel.builder().id(message.getTextChannel().getIdLong()).build());
        suggestion.setServer(AServer.builder().id(message.getGuild().getIdLong()).build());
        suggestionRepository.save(suggestion);
    }

    public void setSuggestionState(Suggestion suggestion, SuggestionState newState) {
        suggestion.setState(newState);
        suggestionRepository.save(suggestion);
    }
}
