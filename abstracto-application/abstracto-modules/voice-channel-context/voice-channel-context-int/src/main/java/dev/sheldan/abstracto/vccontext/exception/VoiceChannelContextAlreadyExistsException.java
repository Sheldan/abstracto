package dev.sheldan.abstracto.vccontext.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class VoiceChannelContextAlreadyExistsException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "voice_channel_context_already_exists_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
