package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.EmoteMissingValidationErrorModel;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.PostTargetValidationErrorModel;
import dev.sheldan.abstracto.core.models.SystemConfigValidationErrorModel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class FeatureValidationServiceBean implements FeatureValidatorService {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ConfigManagementService configService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private BotService botService;

    @Override
    public void checkPostTarget(PostTargetEnum name, AServer server, FeatureValidationResult featureValidationResult) {
        if(!postTargetManagement.postTargetExists(name.getKey(), server)) {
            log.info("Rejecting feature validation because of post target {}.", name.getKey());
            PostTargetValidationErrorModel validationError = PostTargetValidationErrorModel.builder().postTargetName(name.getKey()).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrorModels().add(validationError);
        }
    }

    @Override
    public boolean checkSystemConfig(String name, AServer server, FeatureValidationResult featureValidationResult) {
        if(!configService.configExists(server, name)) {
            log.info("Rejecting feature validation because of system config key {}.", name);
            SystemConfigValidationErrorModel validationError = SystemConfigValidationErrorModel.builder().configKey(name).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrorModels().add(validationError);
            return false;
        }
        return true;
    }

    @Override
    public void checkEmote(String emoteKey, AServer server, FeatureValidationResult featureValidationResult) {
        if(emoteService.getUsableEmoteOrDefault(server.getId(), emoteKey) == null) {
            rejectEmote(emoteKey, featureValidationResult);
            return;
        }
        Optional<AEmote> emoteOptional = emoteManagementService.loadEmoteByName(emoteKey, server.getId());
        if(emoteOptional.isPresent()) {
            AEmote emote = emoteOptional.get();
            if(emote.getCustom()) {
                Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
                if(emoteById == null) {
                    rejectEmote(emoteKey, featureValidationResult);
                } else {
                    Member selfMember = botService.getMemberInServer(server.getId(), botService.getInstance().getSelfUser().getIdLong());
                    if(!emoteById.canInteract(selfMember)) {
                        rejectEmote(emoteKey, featureValidationResult);
                    }
                }
            }
        }
    }

    private void rejectEmote(String emoteKey, FeatureValidationResult featureValidationResult) {
        log.info("Rejecting feature validation because of emote {}", emoteKey);
        EmoteMissingValidationErrorModel validationError = EmoteMissingValidationErrorModel.builder().emoteKey(emoteKey).build();
        featureValidationResult.setValidationResult(false);
        featureValidationResult.getValidationErrorModels().add(validationError);
    }


}
