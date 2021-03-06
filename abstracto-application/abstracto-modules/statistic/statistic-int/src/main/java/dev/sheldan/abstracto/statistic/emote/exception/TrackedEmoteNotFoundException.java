package dev.sheldan.abstracto.statistic.emote.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.templating.Templatable;

/**
 * Exception which is cased in a case the {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote} could not be found.
 * The cases for this are currently wrapped with exist checks, but it will be raised if you call the method directly.
 */
public class TrackedEmoteNotFoundException extends AbstractoRunTimeException implements Templatable {

    public TrackedEmoteNotFoundException(String message) {
        super(message);
    }

    public TrackedEmoteNotFoundException() {
        super("Tracked emote not found.");
    }

    @Override
    public String getTemplateName() {
        return "emote_stats_tracked_emote_not_found";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
