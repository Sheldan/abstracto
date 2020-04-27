package dev.sheldan.abstracto.templating.methods;

import dev.sheldan.abstracto.templating.service.TemplateService;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Component
public class InstantMethod implements TemplateMethodModelEx {

    @Autowired
    private TemplateService service;

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 2) {
            throw new TemplateModelException("Incorrect parameters passed.");
        }
        Object wrappedObject = ((StringModel) arguments.get(0)).getWrappedObject();
        if(!(wrappedObject instanceof Instant)) {
            throw new TemplateModelException("Passed argument was not a instant object");
        }

        String formatString = ((SimpleScalar) arguments.get(1)).getAsString();
        Instant duration = (Instant) wrappedObject;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString)
                .withZone(ZoneId.systemDefault());
        return formatter.format(duration);
    }
}
