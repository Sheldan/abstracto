package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.InfractionFeatureConfig;
import dev.sheldan.abstracto.moderation.config.posttarget.InfractionPostTarget;
import dev.sheldan.abstracto.moderation.listener.manager.InfractionLevelChangedListenerManager;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.InfractionLevelChangeModel;
import dev.sheldan.abstracto.moderation.service.management.InfractionManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InfractionServiceBean implements InfractionService {

    @Autowired
    private InfractionManagementService infractionManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private InfractionServiceBean self;

    @Autowired
    private InfractionLevelChangedListenerManager infractionLevelChangedListenerManager;

    private static final String INFRACTION_NOTIFICATION_TEMPLATE_KEY = "infraction_level_notification";

    @Override
    public void decayInfraction(Infraction infraction) {
        log.info("Decaying infraction {}", infraction.getId());
        infraction.setDecayed(true);
        infraction.setDecayedDate(Instant.now());
    }

    @Override
    public Long getActiveInfractionPointsForUser(AUserInAServer aUserInAServer) {
        List<Infraction> infractions = infractionManagementService.getActiveInfractionsForUser(aUserInAServer);
        log.info("Calculating points for user {} in server {} with {} infractions.",
                aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(), infractions.size());
        return infractions.stream().collect(Collectors.summarizingLong(Infraction::getPoints)).getCount();
    }

    @Override
    public CompletableFuture<Infraction> createInfractionWithNotification(AUserInAServer aUserInAServer, Long points) {
        Infraction createdInfraction = infractionManagementService.createInfraction(aUserInAServer, points);
        Long infractionId = createdInfraction.getId();
        return createInfractionNotification(aUserInAServer, points)
            .thenApply(aBoolean -> self.reloadInfraction(infractionId));
    }

    @Override
    public CompletableFuture<Void> createInfractionNotification(AUserInAServer aUserInAServer, Long points) {
        Long serverId = aUserInAServer.getServerReference().getId();
        Long currentPoints = getActiveInfractionPointsForUser(aUserInAServer);
        Long newPoints = currentPoints + points;
        Pair<Integer, Integer> levelChange = infractionLevelChanged(serverId, newPoints, currentPoints);
        Integer oldLevel = levelChange.getFirst();
        Integer newLevel = levelChange.getSecond();
        if(!oldLevel.equals(newLevel)) {
            InfractionLevelChangeModel model = InfractionLevelChangeModel
                    .builder()
                    .member(MemberDisplay.fromAUserInAServer(aUserInAServer))
                    .newLevel(newLevel)
                    .oldLevel(oldLevel)
                    .oldPoints(currentPoints)
                    .newPoints(newPoints)
                    .build();
            infractionLevelChangedListenerManager.sendInfractionLevelChangedEvent(newLevel, oldLevel, newPoints, currentPoints, ServerUser.fromAUserInAServer(aUserInAServer));
            MessageToSend messageToSend = templateService.renderEmbedTemplate(INFRACTION_NOTIFICATION_TEMPLATE_KEY, model, serverId);
            return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, InfractionPostTarget.INFRACTION_NOTIFICATION, serverId));
        } else {
            return CompletableFuture.completedFuture(null);
        }

    }

    @Transactional
    public Infraction reloadInfraction(Long infractionId) {
        return infractionManagementService.loadInfraction(infractionId);
    }

    private Pair<Integer, Integer> infractionLevelChanged(Long serverId, Long newPoints, Long oldPoints) {
        List<Long> levelConfig = loadInfractionConfig(serverId);
        Integer newLevel = getInfractionLevel(newPoints, levelConfig);
        Integer oldLevel = getInfractionLevel(oldPoints, levelConfig);
        return Pair.of(oldLevel, newLevel);
    }

    private List<Long> loadInfractionConfig(Long serverId) {
        Long levelAmount = configService.getLongValueOrConfigDefault(InfractionFeatureConfig.INFRACTION_LEVELS, serverId);
        List<Long> levelConfig = new ArrayList<>();
        for (long i = 1; i <= levelAmount; i++) {
            String levelKey = InfractionFeatureConfig.INFRACTION_LEVEL_PREFIX + i;
            if(configManagementService.configExists(serverId, levelKey)) {
                levelConfig.add(configService.getLongValue(levelKey, serverId));
            }
        }
        return levelConfig;
    }

    private Integer getInfractionLevel(Long points, List<Long> levelConfig) {
        for (int i = 0; i < levelConfig.size(); i++) {
            if(points >= levelConfig.get(i)) {
                return i;
            }
        }
        return 0;
    }


}
