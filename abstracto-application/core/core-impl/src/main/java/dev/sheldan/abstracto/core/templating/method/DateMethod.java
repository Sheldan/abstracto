package dev.sheldan.abstracto.core.templating.method;

import dev.sheldan.abstracto.core.templating.service.TemplateService;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats the passed {@link Instant} or  {@link OffsetDateTime} object with the given Formatter. The format will be directly passed to {@link DateTimeFormatter}.
 */
@Component
public class DateMethod implements TemplateMethodModelEx {

    @Autowired
    private TemplateService service;

    /**
     * Renders the given {@link Instant} object with the given String. Internally {@link DateTimeFormatter} will be used.
     * @param arguments The list of arguments, first element must be an {@link Instant} or {@link OffsetDateTime} and the second one must be a {@link String}.
     * @return The formatted {@link Instant} as a string.
     * @throws TemplateModelException If there are less or more arguments in the list and if the first element is not a {@link Instant} of {@link OffsetDateTime}
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 2) {
            throw new TemplateModelException("Incorrect parameters passed.");
        }
        Object wrappedObject = ((StringModel) arguments.get(0)).getWrappedObject();
        boolean isOffsetDateTime = wrappedObject instanceof OffsetDateTime;
        boolean isInstant = wrappedObject instanceof Instant;
        if(!isInstant && !isOffsetDateTime) {
            throw new TemplateModelException("Passed argument was not a instant object");
        }

        String formatString = ((SimpleScalar) arguments.get(1)).getAsString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString)
                .withZone(ZoneId.systemDefault());
        if(isInstant) {
            Instant timeStamp = (Instant) wrappedObject;
            return formatter.format(timeStamp);
        } else {
            OffsetDateTime offsetDateTime = (OffsetDateTime) wrappedObject;
            return formatter.format(offsetDateTime);
        }
    }
}
