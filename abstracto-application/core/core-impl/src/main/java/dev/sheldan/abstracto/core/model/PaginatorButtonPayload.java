package dev.sheldan.abstracto.core.model;

import dev.sheldan.abstracto.core.models.template.button.ButtonPayload;
import dev.sheldan.abstracto.core.templating.model.EmbedConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatorButtonPayload implements ButtonPayload {
    private List<EmbedConfiguration> embedConfigs;
    private String paginatorId;
    private String exitButtonId;
    private String startButtonId;
    private String previousButtonId;
    private String nextButtonId;
    private String lastButtonId;
    private Boolean singlePage;
    private Long allowedUser;
}
