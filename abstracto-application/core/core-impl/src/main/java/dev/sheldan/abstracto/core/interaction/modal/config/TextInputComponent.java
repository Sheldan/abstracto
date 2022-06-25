package dev.sheldan.abstracto.core.interaction.modal.config;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TextInputComponent implements ModalComponent {
    private String placeHolder;
    private String id;
    private String label;
    private TextInputComponentStyle style;
    private Integer position;
    private Integer maxLength;
    private Integer minLength;
    private Boolean required;
}
