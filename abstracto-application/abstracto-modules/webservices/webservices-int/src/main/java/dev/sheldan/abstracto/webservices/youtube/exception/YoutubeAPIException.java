package dev.sheldan.abstracto.webservices.youtube.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.webservices.youtube.model.exception.YoutubeAPIExceptionModel;

public class YoutubeAPIException extends AbstractoRunTimeException implements Templatable {

    private final YoutubeAPIExceptionModel model;

    public YoutubeAPIException(Throwable throwable) {
        this.model = YoutubeAPIExceptionModel
                .builder()
                .exception(throwable)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "webservices_youtube_api_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}
