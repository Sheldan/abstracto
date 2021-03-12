package dev.sheldan.abstracto.entertainment.model;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class RollResponseModel extends SlimUserInitiatedServerContext {
    private Integer rolled;
}
