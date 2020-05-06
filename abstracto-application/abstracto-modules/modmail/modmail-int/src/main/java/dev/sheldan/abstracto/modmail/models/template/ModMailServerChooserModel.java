package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.modmail.models.dto.ServerChoice;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ModMailServerChooserModel {
    private List<ServerChoice> commonGuilds;
}
