package dev.sheldan.abstracto.moderation.model.template.listener;


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
