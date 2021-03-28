package dev.sheldan.abstracto.webservices.youtube.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

public class YoutubeVideoNotFoundException extends AbstractoRunTimeException implements Templatable {

    @Override
    public String getTemplateName() {
        return "webservices_youtube_video_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
