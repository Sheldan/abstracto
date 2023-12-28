package dev.sheldan.abstracto.webservices.dictionaryapi.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class WordMeaning {
    private String word;
    private List<WordDefinition> definitions;
}
