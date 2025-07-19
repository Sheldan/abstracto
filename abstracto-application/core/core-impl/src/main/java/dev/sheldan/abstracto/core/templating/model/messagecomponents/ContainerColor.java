package dev.sheldan.abstracto.core.templating.model.messagecomponents;


import java.awt.Color;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ContainerColor {
    private Integer r;
    private Integer g;
    private Integer b;

    public Color toColor() {
        return new Color(r, g, b);
    }
}
