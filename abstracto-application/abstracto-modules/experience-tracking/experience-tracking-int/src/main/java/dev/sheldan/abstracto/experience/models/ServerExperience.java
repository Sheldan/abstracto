package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ServerExperience {
    private Long serverId;
    @Builder.Default
    private List<Long> userInServerIds = new ArrayList<>();
}
