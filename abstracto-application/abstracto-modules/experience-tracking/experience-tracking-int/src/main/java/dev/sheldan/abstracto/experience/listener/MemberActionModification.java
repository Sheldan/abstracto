package dev.sheldan.abstracto.experience.listener;

import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
public class MemberActionModification {
    @Builder.Default
    private Set<Long> rolesToRemove = new HashSet<>();

    @Builder.Default
    private Set<Long> rolesToAdd =  new HashSet<>();

    @Builder.Default
    private Set<Long> channelsToRemove =  new HashSet<>();

    @Builder.Default
    private Set<Long> channelsToAdd =  new HashSet<>();
}
