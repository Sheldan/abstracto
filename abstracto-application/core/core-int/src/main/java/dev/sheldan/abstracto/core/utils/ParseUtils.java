package dev.sheldan.abstracto.core.utils;


import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.exception.DurationFormatException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Duration;
import java.util.ArrayList;
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
                    throw new AbstractoTemplatedException("Multiple channels found..", "multiple_channels_found_by_name_exception");
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
