package dev.sheldan.abstracto.core.interactive.setup.action.config;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
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
    // not an actual value stored in the database, just used as a container
    private AConfig value;

    @Override
    public String getTemplateName() {
        return "feature_setup_system_config_action";
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
