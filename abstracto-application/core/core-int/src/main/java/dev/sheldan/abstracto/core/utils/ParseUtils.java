package dev.sheldan.abstracto.core.utils;


import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.DurationFormatException;
import dev.sheldan.abstracto.core.exception.InstantFormatException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.Timestamp;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParseUtils {

    private ParseUtils() {

    }

    private static final Pattern MESSAGE_REGEX = Pattern.compile("(?<number>\\d+)(?<unit>[ywdhms]+)");
    private static final List<String> VALID_DURATION = Arrays.asList("w", "d", "h", "m", "s");

    public static Duration parseDuration(String textToParseFrom) {
        if(textToParseFrom == null || textToParseFrom.isEmpty()) {
            throw new DurationFormatException("", VALID_DURATION);
        }
        Duration targetDuration;
        try {
            Timestamp discordTimeStamp = TimeFormat.parse(textToParseFrom);
            targetDuration = Duration.between(Instant.now(), discordTimeStamp.toInstant());
            return targetDuration;
        } catch (IllegalArgumentException ex) {
            // ignore
        }
        Matcher matcher = ParseUtils.MESSAGE_REGEX.matcher(textToParseFrom);
        targetDuration = Duration.ZERO;
        String rest = textToParseFrom;
        while(matcher.find()) {
            String unit = matcher.group("unit");
            String number = matcher.group("number");
            rest = rest.replace(matcher.group(0), "");
            long parsed = Long.parseLong(number);
            targetDuration = switch (unit) {
                case "w" -> targetDuration.plus(Duration.ofDays(parsed * 7));
                case "d" -> targetDuration.plus(Duration.ofDays(parsed));
                case "h" -> targetDuration.plus(Duration.ofHours(parsed));
                case "m" -> targetDuration.plus(Duration.ofMinutes(parsed));
                case "s" -> targetDuration.plus(Duration.ofSeconds(parsed));
                default -> throw new DurationFormatException(unit, VALID_DURATION);
            };
        }
        if(!rest.equals("")) {
            throw new DurationFormatException(rest, VALID_DURATION);
        }
        return targetDuration;
    }

    public static Instant parseInstant(String textToParseFrom) {
        if(textToParseFrom == null || textToParseFrom.isEmpty()) {
            throw new DurationFormatException("", VALID_DURATION);
        }
        try {
            Timestamp discordTimeStamp = TimeFormat.parse(textToParseFrom);
            return discordTimeStamp.toInstant();
        } catch (IllegalArgumentException ex) {
            // ignore
        }
        if(StringUtils.isNumeric(textToParseFrom)) {
            return Instant.ofEpochSecond(Integer.parseInt(textToParseFrom));
        }
        throw new InstantFormatException(textToParseFrom);
    }

    public static Role parseRoleFromText(String text, Guild guild) {
        Role role;
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(text);
        if(matcher.matches()) {
            String roleId = matcher.group(1);
            role = guild.getRoleById(roleId);
        } else {
            if(NumberUtils.isParsable(text)) {
                role = guild.getRoleById(text);
            } else {
                List<Role> roles = guild.getRolesByName(text, true);
                if(roles.isEmpty()) {
                    throw new AbstractoTemplatedException("No role found with name.", "no_role_found_by_name_exception");
                }
                if(roles.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple roles found with name.", "multiple_roles_found_by_name_exception");
                }
                role = roles.get(0);
            }
        }
        if(role != null && role.isPublicRole()) {
            throw new AbstractoTemplatedException("Public role cannot be used for role parameter.", "everyone_role_not_allowed_exception");
        }
        return role;
    }

    public static GuildChannel parseGuildChannelFromText(String text, Guild guild) {
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(text);
        GuildChannel textChannel;
        if(matcher.matches()) {
            String channelId = matcher.group(1);
            textChannel = guild.getChannelById(GuildChannel.class, channelId);
        } else {
            if(NumberUtils.isParsable(text)) {
                long channelId = Long.parseLong(text);
                return guild.getGuildChannelById(channelId);
            } else {
                List<ISnowflake> potentialMatches = new ArrayList<>();
                potentialMatches.addAll(getByName(guild.getTextChannelCache(), text));
                potentialMatches.addAll(getByName(guild.getCategoryCache(), text));
                potentialMatches.addAll(getByName(guild.getVoiceChannelCache(), text));
                potentialMatches.addAll(getByName(guild.getThreadChannelCache(), text));
                potentialMatches.addAll(getByName(guild.getForumChannelCache(), text));
                potentialMatches.addAll(getByName(guild.getStageChannelCache(), text));
                if(potentialMatches.isEmpty()) {
                    throw new AbstractoTemplatedException("No channel found with name.", "no_channel_found_by_name_exception");
                }
                if(potentialMatches.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple channels found.", "multiple_channels_found_by_name_exception");
                }
                return guild.getGuildChannelById(potentialMatches.get(0).getId());
            }
        }
        return textChannel;
    }

    private static <T extends ISnowflake> List<T> getByName(SnowflakeCacheView<T> cache, String name) {
        return cache.getElementsByName(name, true);
    }
}
