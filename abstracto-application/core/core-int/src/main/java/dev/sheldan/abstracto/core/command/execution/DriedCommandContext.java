package dev.sheldan.abstracto.core.command.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DriedCommandContext {
    private String commandName;
    private Long serverId;
    private Long channelId;
    private Long messageId;
    private Long userId;

    public static DriedCommandContext buildFromCommandContext(CommandContext commandContext) {
        return DriedCommandContext
                .builder()
                .channelId(commandContext.getChannel().getIdLong())
                .messageId(commandContext.getMessage().getIdLong())
                .serverId(commandContext.getGuild().getIdLong())
                .userId(commandContext.getMessage().getAuthor().getIdLong())
                .build();
    }
}
