package dev.sheldan.abstracto.core.exception;

public class EntityGuildMismatchException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "entity_guild_mismatch_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
