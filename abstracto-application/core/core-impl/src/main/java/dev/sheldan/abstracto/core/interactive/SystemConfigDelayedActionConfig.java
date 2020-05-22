package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.template.commands.SystemConfigActionModel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SystemConfigDelayedActionConfig implements DelayedActionConfig {
    private String configKey;
    private Long serverId;
    private AConfig value;

    @Override
    public String getTemplateName() {
        return "setup_system_config_action";
    }

    @Override
    public Object getTemplateModel() {
        return SystemConfigActionModel
                .builder()
                .configKey(this.configKey)
                .newValue(value.getValueAsString())
                .build();
    }
}
