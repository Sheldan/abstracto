package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;
import java.util.List;

public class ChannelGroupNotFoundException extends AbstractoRunTimeException implements Templatable {

    private String name;
    private List<String> available;

    public ChannelGroupNotFoundException(String key, List<String> available) {
        super("");
        this.name = key;
        this.available = available;
    }
    @Override
    public String getTemplateName() {
        return "channel_group_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("name", this.name);
        param.put("available", String.join(", ", this.available));
        return param;
    }
}
