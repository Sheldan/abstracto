package dev.sheldan.abstracto.webservices.wikipedia.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WikipediaArticleSummaryModel {
    private String summary;
    private String title;
    private String fullURL;
}
