package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.templating.Templatable;

public class MemberNotFoundException extends AbstractoRunTimeException implements Templatable {

    public MemberNotFoundException() {
        super("");
    }

    @Override
    public String getTemplateName() {
        return "member_not_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
