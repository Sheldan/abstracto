package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.SnowFlake;
import net.dv8tion.jda.api.entities.ISnowflake;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SnowflakeUtils {
    private SnowflakeUtils() {

    }

    public static Set<Long> getOwnItemsIds(List<? extends SnowFlake> elements){
       return elements.stream().map(SnowFlake::getId).collect(Collectors.toSet());
    }

    public static Set<Long> getSnowflakeIds(List<? extends ISnowflake> elements){
        return elements.stream().map(ISnowflake::getIdLong).collect(Collectors.toSet());
    }

    // just so we can use it for reference, this is not a valid snowflake
    public static Long createSnowFlake(Instant pointInTime) {
        return (pointInTime.toEpochMilli() - 1420070400000L) << 22;
    }

    public static Long createSnowFlake() {
        return createSnowFlake(Instant.now());
    }

}
