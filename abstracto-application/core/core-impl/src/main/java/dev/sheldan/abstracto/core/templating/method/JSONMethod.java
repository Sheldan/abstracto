package dev.sheldan.abstracto.core.templating.method;

import com.google.gson.Gson;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats the passed {@link Instant} or  {@link OffsetDateTime} object with the given Formatter. The format will be directly passed to {@link DateTimeFormatter}.
 */
@Component
public class JSONMethod implements TemplateMethodModelEx {

    @Autowired
    private Gson gson;

    /**
     * Renders the given {@link Instant} object with the given String. Internally {@link DateTimeFormatter} will be used.
     * @param arguments The list of arguments, first element must be an {@link Instant} or {@link OffsetDateTime} and the second one must be a {@link String}.
     * @return The formatted {@link Instant} as a string.
     * @throws TemplateModelException If there are less or more arguments in the list and if the first element is not a {@link Instant} of {@link OffsetDateTime}
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Incorrect parameters passed.");
        }
        Object o = arguments.get(0);
        Object wrappedObject = ((StringModel) o).getWrappedObject();
        return gson.toJson(wrappedObject);
    }
}
