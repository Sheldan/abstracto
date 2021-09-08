package dev.sheldan.abstracto.invitefilter.model.template.listener;


import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class DeletedInvite {
    private String code;
    private String guildName;
    private Long count;
}
