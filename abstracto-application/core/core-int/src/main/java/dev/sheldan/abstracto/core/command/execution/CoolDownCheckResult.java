package dev.sheldan.abstracto.core.command.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@Builder
public class CoolDownCheckResult {
    private Duration executeIn;
    private Boolean canExecute;
    private CoolDownReason reason;

    public static CoolDownCheckResult getServerCoolDown(Duration duration) {
        return CoolDownCheckResult
                .builder()
                .canExecute(false)
                .reason(CoolDownReason.SERVER)
                .executeIn(duration)
                .build();
    }

    public static CoolDownCheckResult getChannelGroupCoolDown(Duration duration) {
        return CoolDownCheckResult
                .builder()
                .canExecute(false)
                .reason(CoolDownReason.CHANNEL_GROUP)
                .executeIn(duration)
                .build();
    }

    public static CoolDownCheckResult getMemberCoolDown(Duration duration) {
        return CoolDownCheckResult
                .builder()
                .canExecute(false)
                .reason(CoolDownReason.MEMBER)
                .executeIn(duration)
                .build();
    }

    public static CoolDownCheckResult noCoolDown() {
        return CoolDownCheckResult
                .builder()
                .canExecute(true)
                .build();
    }
}

enum CoolDownReason {
    SERVER, CHANNEL_GROUP, MEMBER
}
