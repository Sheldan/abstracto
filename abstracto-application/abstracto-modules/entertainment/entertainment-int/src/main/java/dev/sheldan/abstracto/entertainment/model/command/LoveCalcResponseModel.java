package dev.sheldan.abstracto.entertainment.model.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoveCalcResponseModel {
    private String firstPart;
    private String secondPart;
    private Integer rolled;
}
