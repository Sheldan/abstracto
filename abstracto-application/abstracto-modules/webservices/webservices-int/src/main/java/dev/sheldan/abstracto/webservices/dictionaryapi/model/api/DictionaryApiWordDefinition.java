package dev.sheldan.abstracto.webservices.dictionaryapi.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DictionaryApiWordDefinition {
    private String definition;
    private String example;
}
