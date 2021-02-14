package dev.sheldan.abstracto.moderation.models.template.listener;


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
