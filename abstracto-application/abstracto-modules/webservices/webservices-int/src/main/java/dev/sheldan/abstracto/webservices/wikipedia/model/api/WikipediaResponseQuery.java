package dev.sheldan.abstracto.webservices.wikipedia.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WikipediaResponseQuery {
    private List<WikipediaResponsePage> pages;
}
