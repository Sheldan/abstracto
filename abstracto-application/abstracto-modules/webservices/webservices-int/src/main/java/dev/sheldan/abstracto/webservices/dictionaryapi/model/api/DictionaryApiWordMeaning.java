package dev.sheldan.abstracto.webservices.dictionaryapi.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DictionaryApiWordMeaning {
    private String partOfSpeech;
    private List<DictionaryApiWordDefinition> definitions;
}
