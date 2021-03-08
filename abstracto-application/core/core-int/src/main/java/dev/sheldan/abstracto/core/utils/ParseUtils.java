package dev.sheldan.abstracto.core.utils;


import dev.sheldan.abstracto.core.exception.DurationFormatException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtils {

    private ParseUtils() {

    }

    private static Pattern messageRegex = Pattern.compile("(?<number>\\d+)(?<unit>[ywdhms]+)");
    private static List<String> validDuration = Arrays.asList("w", "d", "h", "m", "s");

    public static Duration parseDuration(String textToParseFrom) {
        if(textToParseFrom == null || textToParseFrom.isEmpty()) {
            throw new DurationFormatException("", validDuration);
        }
        Matcher matcher = ParseUtils.messageRegex.matcher(textToParseFrom);
        Duration start = Duration.ZERO;
        String rest = textToParseFrom;
        while(matcher.find()) {
            String unit = matcher.group("unit");
            String number = matcher.group("number");
            rest = rest.replace(matcher.group(0), "");
            long parsed = Long.parseLong(number);
            switch (unit) {
                case "w": start = start.plus(Duration.ofDays(parsed *  7)); break;
                case "d": start = start.plus(Duration.ofDays(parsed)); break;
                case "h": start = start.plus(Duration.ofHours(parsed)); break;
                case "m": start = start.plus(Duration.ofMinutes(parsed)); break;
                case "s": start = start.plus(Duration.ofSeconds(parsed)); break;
                default: throw new DurationFormatException(unit, validDuration);
            }
        }
        if(!rest.equals("")) {
            throw new DurationFormatException(rest, validDuration);
        }
        return start;
    }
}
