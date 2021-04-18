package dev.sheldan.abstracto.core.command.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
public class CommandCoolDownRuntimeStorage {
    // maps server ID to command name to time at which the command can be executed again
    private Map<Long, CommandReUseMap> serverCoolDowns = new HashMap<>();
    // maps server ID to channel group ID to command name to time at which the command can be executed again
    private Map<Long, Map<Long, CommandReUseMap>> channelGroupCoolDowns = new HashMap<>();
    // maps server ID to member ID to command name to time at which the command can be executed again
    private Map<Long, Map<Long, CommandReUseMap>> memberCoolDowns = new HashMap<>();
}

@Getter
@Setter
@Builder
class CommandReUseMap {
    private Map<String, Instant> reUseTimes;
}