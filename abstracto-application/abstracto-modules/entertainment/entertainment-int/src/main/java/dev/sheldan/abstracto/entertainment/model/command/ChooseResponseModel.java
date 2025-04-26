package dev.sheldan.abstracto.entertainment.model.command;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChooseResponseModel {
    private String chosenValue;
    private List<String> choices;
}
