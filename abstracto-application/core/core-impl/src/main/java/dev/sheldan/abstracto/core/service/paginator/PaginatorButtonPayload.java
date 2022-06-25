package dev.sheldan.abstracto.core.service.paginator;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import dev.sheldan.abstracto.core.templating.model.MessageConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatorButtonPayload implements ButtonPayload {
    private List<MessageConfiguration> embedConfigs;
    private String paginatorId;
    private String exitButtonId;
    private String startButtonId;
    private String previousButtonId;
    private String nextButtonId;
    private String lastButtonId;
    private Boolean singlePage;
    private Long allowedUser;
}
