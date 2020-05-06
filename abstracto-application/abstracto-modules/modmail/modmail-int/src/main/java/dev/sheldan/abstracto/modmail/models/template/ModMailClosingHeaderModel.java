package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@Builder
public class ModMailClosingHeaderModel {
    private String note;
    private ModMailThread closedThread;

    public Duration getDuration() {
        return Duration.between(closedThread.getCreated(), closedThread.getClosed());
    }
}
