package dev.sheldan.abstracto.webservices.wikipedia.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class NoWikipediaArticleFoundException extends AbstractoRunTimeException implements Templatable {
    public NoWikipediaArticleFoundException() {
        super("No wikipedia article found.");
    }

    @Override
    public String getTemplateName() {
        return "no_wikipedia_article_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
