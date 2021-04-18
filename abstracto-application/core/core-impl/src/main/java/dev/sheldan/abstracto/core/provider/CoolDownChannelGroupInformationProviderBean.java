package dev.sheldan.abstracto.core.provider;

import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import dev.sheldan.abstracto.core.models.provider.ChannelGroupInformationRequest;
import dev.sheldan.abstracto.core.models.provider.CoolDownChannelInformation;
import dev.sheldan.abstracto.core.models.provider.InformationRequest;
import dev.sheldan.abstracto.core.models.provider.ProviderInformation;
import dev.sheldan.abstracto.core.service.management.CoolDownChannelGroupManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.core.command.service.CommandCoolDownServiceBean.COOL_DOWN_CHANNEL_GROUP_TYPE;

@Component
public class CoolDownChannelGroupInformationProviderBean implements ChannelGroupInformationProvider {

    @Autowired
    private CoolDownChannelGroupManagementService coolDownChannelGroupManagementService;

    @Override
    public boolean handlesRequest(InformationRequest informationRequest) {
        if(informationRequest instanceof ChannelGroupInformationRequest) {
            ChannelGroupInformationRequest request = (ChannelGroupInformationRequest) informationRequest;
            return request.getChannelGroupType().equals(COOL_DOWN_CHANNEL_GROUP_TYPE);
        }
        return false;
    }

    @Override
    public ProviderInformation retrieveInformation(InformationRequest informationRequest) {
        ChannelGroupInformationRequest request = (ChannelGroupInformationRequest) informationRequest;
        CoolDownChannelGroup coolDownChannelGroup = coolDownChannelGroupManagementService.findByChannelGroupId(request.getChannelGroupId());
        return CoolDownChannelInformation
                .builder()
                .channelCoolDown(coolDownChannelGroup.getChannelCoolDown())
                .memberCoolDown(coolDownChannelGroup.getMemberCoolDown())
                .build();
    }
}
