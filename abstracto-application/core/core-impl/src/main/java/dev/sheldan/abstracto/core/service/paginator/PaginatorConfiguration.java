package dev.sheldan.abstracto.core.service.paginator;

import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.templating.model.MessageConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatorConfiguration {
    private List<MessageConfiguration> embedConfigs;
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
