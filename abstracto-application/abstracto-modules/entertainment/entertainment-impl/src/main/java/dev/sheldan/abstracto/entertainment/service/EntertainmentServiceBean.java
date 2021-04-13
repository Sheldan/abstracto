package dev.sheldan.abstracto.entertainment.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureConfig;
import dev.sheldan.abstracto.entertainment.exception.ReactDuplicateCharacterException;
import dev.sheldan.abstracto.entertainment.model.ReactMapping;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.*;

@Component
@Slf4j
public class EntertainmentServiceBean implements EntertainmentService {

    public static final List<String> EIGHT_BALL_ANSWER_KEYS = Arrays.asList(
            "IS_CERTAIN", "IS_DECIDEDLY", "WITHOUT_DOUBT", "DEFINITELY_SO", "MAY_RELY", // certain
            "SEE_IT", "MOST_LIKELY", "OUTLOOK", "YES", "POINT_YES", // certain
            "HAZY", "ASK_AGAIN", "NOT_TELL", "CANNOT_PREDICT", "CONCENTRATE", // uncertain
            "DONT_COUNT", "REPLY_NO", "SOURCES_NO", "OUTLOOK_NOT_GOOD", "DOUBTFUL" // negative
    );

    private ReactMapping reactMapping;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private ConfigService configService;

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
        return secureRandom.nextInt(100);
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
            // reject any other character, as the ones we can deal with
            if (!this.reactMapping.getSingle().containsKey(charAsString)) {
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
        } catch (IOException e) {
            log.error("Failed to load react bindings.", e);
        }

    }
}
