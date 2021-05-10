package dev.sheldan.abstracto.invitefilter.model.template.listener;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeletedInvite {
    private String code;
    private Long count;
}
