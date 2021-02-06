package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.AllowedMentionConfig;
import dev.sheldan.abstracto.core.models.database.AllowedMention;
import dev.sheldan.abstracto.core.exception.UnknownMentionTypeException;
import dev.sheldan.abstracto.core.service.management.AllowedMentionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AllowedMentionServiceBean implements AllowedMentionService {

    public static final String EVERYONE_MENTION_KEY = "everyone";
    public static final String ROLE_MENTION_KEY = "role";
    public static final String USER_MENTION_KEY = "user";

    @Autowired
    private AllowedMentionConfig allowedMentionConfig;

    @Autowired
    private AllowedMentionManagementService allowedMentionManagementService;

    private final HashMap<String, Message.MentionType> ALL_MENTION_TYPES = new HashMap<>();

    @Override
    public boolean allMentionsAllowed(Long serverId) {
        return getEffectiveAllowedMention(serverId).allAllowed();
    }

    @Override
    public List<Message.MentionType> getAllowedMentionTypesForServer(Long serverId) {
        AllowedMention allowedMention = getEffectiveAllowedMention(serverId);
        return mapAllowedMentionToMentionType(allowedMention);
    }

    @Override
    public void allowMentionForServer(Message.MentionType mentionType, Long serverId) {
        log.info("Allowing mention {} for server {}.", mentionType, serverId);
        setOrInitializeAllowedMention(mentionType, serverId, true);
    }

    @Override
    public void disAllowMentionForServer(Message.MentionType mentionType, Long serverId) {
        log.info("Disallowing mention {} for server {}.", mentionType, serverId);
        setOrInitializeAllowedMention(mentionType, serverId, false);
    }

    @Override
    public AllowedMention getDefaultAllowedMention() {
            return AllowedMention
                    .builder()
                    .everyone(allowedMentionConfig.getEveryone())
                    .role(allowedMentionConfig.getRole())
                    .user(allowedMentionConfig.getUser())
                    .build();
    }

    @Override
    public AllowedMention getEffectiveAllowedMention(Long serverId) {
        Optional<AllowedMention> customAllowMentions = allowedMentionManagementService.getCustomAllowedMentionFor(serverId);
        return customAllowMentions.orElseGet(this::getDefaultAllowedMention);
    }

    @Override
    public Message.MentionType getMentionTypeFromString(String input) {
        input = input.toLowerCase();
        if (ALL_MENTION_TYPES.containsKey(input)) {
            return ALL_MENTION_TYPES.get(input);
        }
        throw new UnknownMentionTypeException();
    }

    private List<Message.MentionType> mapAllowedMentionToMentionType(AllowedMention allowedMention) {
        // if all are allowed, we dont want to restrict it
        if(allowedMention.allAllowed()) {
            return null;
        }
        List<Message.MentionType> types = new ArrayList<>();
        if(allowedMention.getEveryone()) {
            types.add(Message.MentionType.EVERYONE);
        }
        if(allowedMention.getRole()) {
            types.add(Message.MentionType.ROLE);
        }
        if(allowedMention.getUser()) {
            types.add(Message.MentionType.USER);
        }
        return types;
    }

    private void setOrInitializeAllowedMention(Message.MentionType mentionType, Long serverId, boolean initialMentionValue) {
        Optional<AllowedMention> customAllowedMentionOptional = allowedMentionManagementService.getCustomAllowedMentionFor(serverId);
        AllowedMention customAllowedMention = customAllowedMentionOptional.orElseGet(this::getDefaultAllowedMention);
        switch (mentionType) {
            case EVERYONE:
                customAllowedMention.setEveryone(initialMentionValue);
                break;
            case ROLE:
                customAllowedMention.setRole(initialMentionValue);
                break;
            case USER:
                customAllowedMention.setUser(initialMentionValue);
                break;
        }
        if (!customAllowedMentionOptional.isPresent()) {
            allowedMentionManagementService.createCustomAllowedMention(serverId, customAllowedMention);
        }
    }

    @PostConstruct
    public void postConstruct() {
         ALL_MENTION_TYPES.put(EVERYONE_MENTION_KEY, Message.MentionType.EVERYONE);
         ALL_MENTION_TYPES.put(ROLE_MENTION_KEY, Message.MentionType.ROLE);
         ALL_MENTION_TYPES.put(USER_MENTION_KEY, Message.MentionType.USER);
    }
}
