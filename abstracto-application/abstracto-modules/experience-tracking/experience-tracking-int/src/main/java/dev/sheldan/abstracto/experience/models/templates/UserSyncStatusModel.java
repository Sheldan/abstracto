package dev.sheldan.abstracto.experience.models.templates;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSyncStatusModel {
    private Integer currentCount;
    private Integer totalUserCount;
}
