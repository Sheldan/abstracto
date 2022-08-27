package dev.sheldan.abstracto.utility.model;

import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class UserInfoModel {
    private Long id;
    private MemberNameDisplay memberDisplay;
    private String onlineStatus;
    @Builder.Default
    private List<String> activities = new ArrayList<>();
    private String customStatus;
    private String customEmoji;
    @Builder.Default
    private List<RoleDisplay> roles = new ArrayList<>();
    private Instant joinDate;
    private Instant creationDate;
}
