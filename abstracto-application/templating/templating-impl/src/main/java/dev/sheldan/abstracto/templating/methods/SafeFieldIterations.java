package dev.sheldan.abstracto.templating.methods;

import dev.sheldan.abstracto.templating.service.TemplateService;
import freemarker.template.DefaultListAdapter;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class SafeFieldIterations implements TemplateMethodModelEx {

    @Autowired
    private TemplateService service;

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        List wrappedObject = (List) ((DefaultListAdapter) arguments.get(0)).getWrappedObject();
        String appliedTemplate = ((SimpleScalar) arguments.get(1)).getAsString();
        String nameTemplate = ((SimpleScalar) arguments.get(2)).getAsString();
        String inline = ((SimpleScalar) arguments.get(3)).getAsString();


        List<StringBuilder> result = new ArrayList<>();
        StringBuilder currentBuilder = new StringBuilder();
        String firstEmbedTitle = service.renderTemplateWithMap(nameTemplate, getEmbedCountParameters(1, wrappedObject));
        currentBuilder.append(newFieldHeader(firstEmbedTitle, inline));
        String finalClosingString = "\"}";
        String closingString = finalClosingString + ",";
        int splitFieldCounts = 1;
        for (Object ob: wrappedObject) {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("object", ob);
            String s = service.renderTemplateWithMap(appliedTemplate, parameters);
            if((currentBuilder.toString().length() + s.length() > 1024)) {
                currentBuilder.append(closingString);
                result.add(currentBuilder);
                currentBuilder = new StringBuilder();
                splitFieldCounts += 1;
                String renderedName = service.renderTemplateWithMap(nameTemplate, getEmbedCountParameters(splitFieldCounts, wrappedObject));
                currentBuilder.append(newFieldHeader(renderedName, inline));
            }
            currentBuilder.append(s);

        }
        currentBuilder.append(finalClosingString);
        result.add(currentBuilder);
        StringBuilder bigBuilder = new StringBuilder();
        for (StringBuilder innerBuilder: result) {
            bigBuilder.append(innerBuilder.toString());
        }
        return bigBuilder;
    }

    private String newFieldHeader(String name, String inline) {
        return String.format("{ \"name\": \"%s\", \"inline\": \"%s\", \"value\": \"", name, inline);
    }

    private HashMap<String, Object> getEmbedCountParameters(Integer count, List objects) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("count", count);
        parameters.put("list", objects);
        return parameters;
    }


}
