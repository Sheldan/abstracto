package dev.sheldan.abstracto.entertainment.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureConfig;
import dev.sheldan.abstracto.entertainment.exception.ReactDuplicateCharacterException;
import dev.sheldan.abstracto.entertainment.model.PressFPayload;
import dev.sheldan.abstracto.entertainment.model.ReactMapping;
import dev.sheldan.abstracto.entertainment.model.command.PressFPromptModel;
import dev.sheldan.abstracto.entertainment.model.command.PressFResultModel;
import dev.sheldan.abstracto.entertainment.model.database.PressF;
import dev.sheldan.abstracto.entertainment.service.management.PressFManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EntertainmentServiceBean implements EntertainmentService {

    public static final List<String> EIGHT_BALL_ANSWER_KEYS = Arrays.asList(
            "IS_CERTAIN", "IS_DECIDEDLY", "WITHOUT_DOUBT", "DEFINITELY_SO", "MAY_RELY", // certain
            "SEE_IT", "MOST_LIKELY", "OUTLOOK", "YES", "POINT_YES", // certain
            "HAZY", "ASK_AGAIN", "NOT_TELL", "CANNOT_PREDICT", "CONCENTRATE", // uncertain
            "DONT_COUNT", "REPLY_NO", "SOURCES_NO", "OUTLOOK_NOT_GOOD", "DOUBTFUL" // negative
    );

    public static final String PRESS_F_BUTTON_ORIGIN = "PRESS_F_BUTTON";
    private static final String PRESS_F_RESULT_TEMPLATE_KEY = "pressF_result";

    private ReactMapping reactMapping;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private PressFManagementService pressFManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MessageService messageService;

    @Value("classpath:react_mappings.json")
    private Resource reactMappingSource;

    @Autowired
    private Gson gson;

    @Override
    public String getEightBallValue(String text) {
        return EIGHT_BALL_ANSWER_KEYS.get(secureRandom.nextInt(EIGHT_BALL_ANSWER_KEYS.size()));
    }

    @Override
    public Integer getLoveCalcValue(String firstPart, String secondPart) {
        String fullInput = firstPart.toLowerCase() + secondPart.toLowerCase();
        Random random = new Random(fullInput.hashCode());
        return random.nextInt(100);
    }

    @Override
    public Integer calculateRollResult(Integer low, Integer high) {
        int actualLow = Math.min(low, high);
        int actualHigh = Math.max(low, high);
        return actualLow + secureRandom.nextInt(actualHigh - actualLow);
    }

    @Override
    public boolean executeRoulette(Member memberExecuting) {
        Long possibilities = configService.getLongValueOrConfigDefault(EntertainmentFeatureConfig.ROULETTE_BULLETS_CONFIG_KEY, memberExecuting.getGuild().getIdLong());
        // 1/possibilities of chance, we don't have a state, each time its reset
        return secureRandom.nextInt(possibilities.intValue()) == 0;
    }

    @Override
    public String takeChoice(List<String> choices, Member memberExecuting) {
        return choices.get(secureRandom.nextInt(choices.size()));
    }

    @Override
    public String createMockText(String text, Member memberExecuting, Member mockedUser) {
        if(text == null) {
            return "";
        }
        char[] textChars = text.toLowerCase().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, textCharsLength = textChars.length; i < textCharsLength; i++) {
            char character = textChars[i];
            if(i % 2 == 0) {
                sb.append(Character.toUpperCase(character));
            } else {
                sb.append(character);
            }
        }
        return sb.toString();
    }

    @Override
    public PressFPromptModel getPressFModel(String text) {
        String pressFComponent = componentService.generateComponentId();
        return PressFPromptModel
                .builder()
                .pressFComponentId(pressFComponent)
                .text(text)
                .build();
    }

    @Transactional
    public void persistPressF(String text, Duration duration, Member executingMember, String componentId, GuildMessageChannel guildMessageChannel, Long messageId) {
        Instant targetDate = Instant.now().plus(duration);
        log.info("Persisting pressF started by {} in server {} with due date {}.", executingMember.getIdLong(), executingMember.getGuild().getIdLong(), targetDate);
        AUserInAServer creator = userInServerManagementService.loadOrCreateUser(executingMember);
        AChannel channel = channelManagementService.loadChannel(guildMessageChannel);
        PressF pressF = pressFManagementService.createPressF(text, targetDate, creator, channel, messageId);
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("pressFId", pressF.getId().toString());
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        log.debug("Starting scheduled job for pressF {}", pressF.getId());
        schedulerService.executeJobWithParametersOnce("pressFEvaluationJob", "entertainment", jobParameters, Date.from(targetDate));
        PressFPayload pressFPayload = PressFPayload
                .builder()
                .pressFId(pressF.getId())
                .build();
        log.debug("Persisting payload for pressF {}", pressF.getId());
        componentPayloadService.createButtonPayload(componentId, pressFPayload, PRESS_F_BUTTON_ORIGIN, creator.getServerReference());
    }

    @Override
    @Transactional
    public CompletableFuture<Void> evaluatePressF(Long pressFId) {
        Optional<PressF> pressFOptional = pressFManagementService.getPressFById(pressFId);
        if(pressFOptional.isPresent()) {
            log.info("Evaluating pressF with id {}", pressFId);
            PressF pressF = pressFOptional.get();
            PressFResultModel model = PressFResultModel
                    .builder()
                    .userCount((long) pressF.getPresser().size())
                    .text(pressF.getText())
                    .messageId(pressF.getMessageId())
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(PRESS_F_RESULT_TEMPLATE_KEY, model);
            Long serverId = pressF.getServer().getId();
            Long channelId = pressF.getPressFChannel().getId();
            Long messageId = pressF.getMessageId();
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageEmbedToSendToAChannel(messageToSend, pressF.getPressFChannel()))
            .thenCompose(unused -> messageService.loadMessage(serverId, channelId, messageId).thenCompose(message -> {
                log.info("Clearing buttons from pressF {} in with message {} in channel {} in server {}.", pressFId, pressFId, channelId, serverId);
                return componentService.clearButtons(message);
            }));
        } else {
            throw new AbstractoRunTimeException(String.format("PressF with id %s not found.", pressFId));
        }
    }

    @Override
    public List<String> convertTextToEmojis(String text) {
        return convertTextToEmojis(text, false);
    }

    @Override
    public String convertTextToEmojisAString(String text) {
        return String.join("", convertTextToEmojis(text));
    }

    @Override
    public List<String> convertTextToEmojis(String text, boolean allowDuplicates) {
        if(text == null) {
            return new ArrayList<>();
        }
        text = text.toLowerCase();
        // we have to have a separate set to check for combo duplicates, because the checks are different:
        // first check is if we already used it as an replacement
        // the second check below is whether or not we used it as a replacement, that way we allow
        // unicode cars from users as well, this leads to things like sos[sos] not being allowed, because the
        // unicode chars get removed, and the first sos gets replaced with the unicode
        Set<String> replacedCombos = new HashSet<>();
        List<String> result = new ArrayList<>();
        // this is used to replace the replacements for more than one character
        for (String s : this.reactMapping.getCombinationKeys()) {
            if (text.contains(s)) {
                String replacement = this.reactMapping.getCombination().get(s);
                if(!replacedCombos.contains(replacement) || allowDuplicates) {
                    if(allowDuplicates) {
                        text = text.replaceAll(s, replacement);
                    } else {
                        text = text.replaceFirst(s, replacement);
                    }
                    replacedCombos.add(replacement);
                }
            }
        }
        log.debug("Replaced {} combos.", replacedCombos.size());
        Set<String> usedReplacements = new HashSet<>();
        char[] split = text.toCharArray();

        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            char normalCharacter = split[i];
            String charAsString = Character.toString(normalCharacter);
            // the split, also splits surrogate chars (naturally), therefore we need this additional checks
            // to ignore the first part, and connect the chars again in order to check them
            if(Character.isHighSurrogate(normalCharacter)) {
                continue;
            }
            // in this case we already have unicode, this can either come from the multiple char replacement
            // or because we already got unicode to begin with (multi char only), in that case, we also do a duplicate check
            // and add it directly
            if(Character.isLowSurrogate(normalCharacter)) {
                String usedUnicode = split[i - 1] + charAsString;
                if(!usedReplacements.contains(usedUnicode) || allowDuplicates) {
                    usedReplacements.add(usedUnicode);
                    result.add(usedUnicode);
                }
                continue;
            }
            if(replacedCombos.contains(charAsString) && (!usedReplacements.contains(charAsString) || allowDuplicates)) {
                usedReplacements.add(charAsString);
                result.add(charAsString);
                continue;
            }
            // reject any other character, as the ones we can deal with
            if (!this.reactMapping.getSingle().containsKey(charAsString)) {
                log.info("Cannot find mapping. Not replacing with emote.");
                continue;
            }
            List<String> listToUse = this.reactMapping.getSingle().get(charAsString);
            boolean foundReplacement = false;
            for (String replacementChar : listToUse) {
                if (!usedReplacements.contains(replacementChar) || allowDuplicates) {
                    result.add(replacementChar);
                    usedReplacements.add(replacementChar);
                    foundReplacement = true;
                    break;
                }
            }
            if (!foundReplacement) {
                throw new ReactDuplicateCharacterException();
            }
        }
        log.debug("We used {} replacements for a string of length {}.", usedReplacements.size(), text.length());
        return result;
    }

    @Override
    public String convertTextToEmojisAsString(String text, boolean allowDuplicates) {
        return String.join("", convertTextToEmojis(text, allowDuplicates));
    }

    @PostConstruct
    public void postConstruct() {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(reactMappingSource.getInputStream()));
            this.reactMapping = gson.fromJson(reader, ReactMapping.class);
            this.reactMapping.populateKeys();
            log.info("Loaded {} single replacement mappings.", this.reactMapping.getSingle().size());
            log.info("Loaded {} combo replacements.", this.reactMapping.getCombination().size());
        } catch (IOException e) {
            log.error("Failed to load react bindings.", e);
        }

    }
}
