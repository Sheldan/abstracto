package dev.sheldan.abstracto.webservices.dictionaryapi.model.template;

import dev.sheldan.abstracto.webservices.dictionaryapi.model.WordDefinition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DictionaryDefinition {
    private String definition;
    private String example;

    public static DictionaryDefinition fromWordDefinition(WordDefinition definition) {
        return DictionaryDefinition
                .builder()
                .example(definition.getExample())
                .definition(definition.getDefinition())
                .build();
    }
}
