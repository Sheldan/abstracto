package dev.sheldan.abstracto.core.utils;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtils {

    private static Pattern messageRegex = Pattern.compile("(?<number>\\d+)(?<unit>[ywdhms]+)");

    public static Duration parseDuration(String textToParseFrom) {
        Matcher matcher = ParseUtils.messageRegex.matcher(textToParseFrom);
        Duration start = Duration.ZERO;
        while(matcher.find()) {
            String unit = matcher.group("unit");
            String number = matcher.group("number");
            long parsed = Long.parseLong(number);
            switch (unit) {
                case "w": start = start.plus(Duration.ofDays(parsed *  7)); break;
                case "d": start = start.plus(Duration.ofDays(parsed)); break;
                case "h": start = start.plus(Duration.ofHours(parsed)); break;
                case "m": start = start.plus(Duration.ofMinutes(parsed)); break;
                case "s": start = start.plus(Duration.ofSeconds(parsed)); break;
            }
        }
        return start;
    }
}
