package dev.sheldan.abstracto.webservices.dictionaryapi.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DictionaryMeaning {
    private String word;
    private List<DictionaryDefinition> definitions;
}
