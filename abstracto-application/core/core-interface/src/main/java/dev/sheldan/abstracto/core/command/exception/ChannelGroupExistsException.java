package dev.sheldan.abstracto.core.command.exception;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.templating.Templatable;

import java.util.HashMap;

public class ChannelGroupExistsException extends AbstractoRunTimeException implements Templatable {

    private String name;

    public ChannelGroupExistsException(String name) {
        super("");
        this.name = name;
    }

    @Override
    public String getTemplateName() {
        return "channel_group_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        HashMap<String, String> param = new HashMap<>();
        param.put("name", this.name);
        return param;
    }
}
