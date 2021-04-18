package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.listener.sync.entity.ChannelGroupDeletedListener;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.CoolDownChannelGroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.core.command.service.CommandCoolDownServiceBean.COOL_DOWN_CHANNEL_GROUP_TYPE;

@Component
public class CoolDownChannelGroupDeletedListener implements ChannelGroupDeletedListener {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private CoolDownChannelGroupManagementService coolDownChannelGroupManagementService;

    @Override
    public DefaultListenerResult execute(ChannelGroupDeletedListenerModel model) {
        AChannelGroup channelGroup = channelGroupManagementService.findChannelGroupById(model.getChannelGroupId());
        if(channelGroup.getChannelGroupType().getGroupTypeKey().equals(COOL_DOWN_CHANNEL_GROUP_TYPE)) {
            coolDownChannelGroupManagementService.deleteCoolDownChannelGroup(channelGroup);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }
}
