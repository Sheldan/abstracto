package dev.sheldan.abstracto.core.model;

import dev.sheldan.abstracto.core.models.template.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.templating.model.EmbedConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatorConfiguration {
    private List<EmbedConfiguration> embedConfigs;
    private String paginatorId;
    private Long timeoutSeconds;
    private Boolean restrictUser;
    private ButtonConfigModel exitButton;
    private ButtonConfigModel startButton;
    private ButtonConfigModel previousButton;
    private ButtonConfigModel nextButton;
    private ButtonConfigModel lastButton;
    private Boolean singlePage;
}
