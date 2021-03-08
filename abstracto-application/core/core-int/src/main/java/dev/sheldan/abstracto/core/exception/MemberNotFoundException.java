package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.templating.Templatable;

public class MemberNotFoundException extends AbstractoRunTimeException implements Templatable {

    public MemberNotFoundException() {
        super("Member was not found");
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
