package dev.sheldan.abstracto.webservices.dictionaryapi.model;

import dev.sheldan.abstracto.webservices.dictionaryapi.model.api.DictionaryApiWordDefinition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class WordDefinition {
    private String definition;
    private String example;

    public static WordDefinition fromResponseDefinition(DictionaryApiWordDefinition responseItem) {
        return WordDefinition
                .builder()
                .example(responseItem.getExample())
                .definition(responseItem.getDefinition())
                .build();
    }
}
