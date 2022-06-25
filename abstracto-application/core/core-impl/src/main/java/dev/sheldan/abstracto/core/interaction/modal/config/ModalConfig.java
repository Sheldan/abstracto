package dev.sheldan.abstracto.core.interaction.modal.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ModalConfig {
    private List<TextInputComponent> textInputs;
    private String id;
    private String title;
}
