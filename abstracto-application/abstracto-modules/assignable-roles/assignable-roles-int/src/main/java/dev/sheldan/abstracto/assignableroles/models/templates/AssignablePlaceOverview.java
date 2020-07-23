package dev.sheldan.abstracto.assignableroles.models.templates;

import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class AssignablePlaceOverview {
    @Builder.Default
    private List<AssignableRolePlace> places = new ArrayList<>();
}
