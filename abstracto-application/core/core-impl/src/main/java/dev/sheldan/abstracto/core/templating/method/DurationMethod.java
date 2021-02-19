package dev.sheldan.abstracto.core.templating.method;

import dev.sheldan.abstracto.core.templating.service.TemplateService;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

/**
 * Method used to format the {@link Duration} object, as its not natively supported by Freemarker.
 * This method only accepts a single {@link Duration} object as the first parameter.
 */
@Component
public class DurationMethod implements TemplateMethodModelEx {

    @Autowired
    private TemplateService service;

    /**
     * This method expects a single Duration object in the list of arguments. It will throw a {@link TemplateModelException}
     * otherwise
     * @param arguments The parameters passed to this method, should be only a single duration.
     * @return The string representation of the {@link Duration} object
     * @throws TemplateModelException In case the amount of parameters is not correct, or the first object was not a {@link Duration}
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Incorrect parameters passed.");
        }
        Object o = arguments.get(0);
        if(!(o instanceof StringModel)) {
            throw new TemplateModelException("Passed object was not a StringModel.");
        }
        Object wrappedObject = ((StringModel) o).getWrappedObject();
        if(!(wrappedObject instanceof Duration)) {
            throw new TemplateModelException("Passed argument was not a duration object");
        }
        Duration duration = (Duration) wrappedObject;
        // upgrading to java 9 makes this nicer
        HashMap<String, Object> parameters = new HashMap<>();
        long days = duration.toDays();
        parameters.put("days", days);
        long hours = duration.toHours() % 24;
        parameters.put("hours", hours);
        long minutes = duration.toMinutes() % 60;
        parameters.put("minutes", minutes);

        long seconds = duration.get(ChronoUnit.SECONDS) % 60;
        parameters.put("seconds", seconds);

        return service.renderTemplateWithMap("duration_formatting", parameters);
    }
}
