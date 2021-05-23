package dev.sheldan.abstracto.profanityfilter.service.management;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import dev.sheldan.abstracto.profanityfilter.repository.ProfanityUseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProfanityUseManagementServiceBean implements ProfanityUseManagementService {

    @Autowired
    private ProfanityUseRepository profanityUseRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public ProfanityUse createProfanityUse(ServerChannelMessage profaneMessage, ServerChannelMessage reportMessage, ProfanityUserInAServer reportedUser, ProfanityGroup usedProfanityGroup) {
        AChannel profaneChannel = channelManagementService.loadChannel(profaneMessage.getChannelId());
        AChannel reportChannel = channelManagementService.loadChannel(reportMessage.getChannelId());
        ProfanityUse profanityUse = ProfanityUse
                .builder()
                .profanityUser(reportedUser)
                .profanityGroup(usedProfanityGroup)
                .profaneMessageId(profaneMessage.getMessageId())
                .profaneChannel(profaneChannel)
                .reportMessageId(reportMessage.getMessageId())
                .reportChannel(reportChannel)
                .verified(false)
                .confirmed(false)
                .server(reportedUser.getServer())
                .build();
        return profanityUseRepository.save(profanityUse);
    }

    @Override
    public Optional<ProfanityUse> getProfanityUseViaReportMessageId(Long messageId) {
        return profanityUseRepository.findById(messageId);
    }

    @Override
    public Long getPositiveReports(ProfanityUserInAServer profanityUserInAServer) {
        return profanityUseRepository.countByProfanityUserAndVerifiedTrueAndConfirmedTrue(profanityUserInAServer);
    }

    @Override
    public Long getFalsePositiveReports(ProfanityUserInAServer profanityUserInAServer) {
        return profanityUseRepository.countByProfanityUserAndVerifiedTrueAndConfirmedFalse(profanityUserInAServer);
    }

    @Override
    public List<ProfanityUse> getMostRecentProfanityReports(ProfanityUserInAServer profanityUserInAServer, int count) {
        return profanityUseRepository.findAllByProfanityUserAndConfirmedTrueOrderByCreatedDesc(profanityUserInAServer, PageRequest.of(0, count));
    }
}
