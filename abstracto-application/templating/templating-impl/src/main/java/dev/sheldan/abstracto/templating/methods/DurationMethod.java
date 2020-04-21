package dev.sheldan.abstracto.templating.methods;

import dev.sheldan.abstracto.templating.service.TemplateService;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

@Component
public class DurationMethod implements TemplateMethodModelEx {

    @Autowired
    private TemplateService service;

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Incorrect parameters passed.");
        }
        Object wrappedObject = ((StringModel) arguments.get(0)).getWrappedObject();
        if(!(wrappedObject instanceof Duration)) {
            throw new TemplateModelException("Passed argument was not a duration object");
        }
        Duration duration = (Duration) wrappedObject;
        StringBuilder stringBuilder = new StringBuilder();
        // upgrading to java 9 makes this nicer
        long days = duration.toDays();
        if(days > 0) {
            stringBuilder.append(service.renderTemplate("day", getParam(days)));
        }
        long hours = duration.toHours() % 24;
        if(hours > 0) {
            stringBuilder.append(service.renderTemplate("hour", getParam(hours)));
        }
        long minutes = duration.toMinutes() % 60;
        if(minutes > 0) {
            stringBuilder.append(service.renderTemplate("minute", getParam(minutes)));
        }

        long seconds = duration.get(ChronoUnit.SECONDS) % 60;
        if(seconds > 0) {
            stringBuilder.append(service.renderTemplate("second", getParam(seconds)));
        }

        return stringBuilder.toString();
    }

    private HashMap<String, Object> getParam(Long value) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("amount", value);
        return params;
    }
}
