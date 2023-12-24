package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.ValidationErrorModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModMailThreadContainerValidationErrorModel implements ValidationErrorModel {
    private Long currentChannelId;

    @Override
    public String getTemplateName() {
        return "feature_setup_modmail_thread_container_not_setup";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}
