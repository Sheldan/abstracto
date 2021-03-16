package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeature;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@Component
public class EntertainmentServiceBean implements EntertainmentService {

    public static final List<String> EIGHT_BALL_ANSWER_KEYS = Arrays.asList(
            "IS_CERTAIN", "IS_DECIDEDLY", "WITHOUT_DOUBT", "DEFINITELY_SO", "MAY_RELY", // certain
            "SEE_IT", "MOST_LIKELY", "OUTLOOK", "YES", "POINT_YES", // certain
            "HAZY", "ASK_AGAIN", "NOT_TELL", "CANNOT_PREDICT", "CONCENTRATE", // uncertain
            "DONT_COUNT", "REPLY_NO", "SOURCES_NO", "OUTLOOK_NOT_GOOD", "DOUBTFUL" // negative
    );

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private ConfigService configService;

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
        Long possibilities = configService.getLongValueOrConfigDefault(EntertainmentFeature.ROULETTE_BULLETS_CONFIG_KEY, memberExecuting.getGuild().getIdLong());
        // 1/possibilities of chance, we don't have a state, each time its reset
        return secureRandom.nextInt(possibilities.intValue()) == 0;
    }

    @Override
    public String takeChoice(List<String> choices, Member memberExecuting) {
        return choices.get(secureRandom.nextInt(choices.size()));
    }
}
