package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.EmoteMissingValidationError;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.PostTargetValidationError;
import dev.sheldan.abstracto.core.models.SystemConfigValidationError;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
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
            PostTargetValidationError validationError = PostTargetValidationError.builder().postTargetName(name.getKey()).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrors().add(validationError);
        }
    }

    @Override
    public boolean checkSystemConfig(String name, AServer server, FeatureValidationResult featureValidationResult) {
        if(!configService.configExists(server, name)) {
            SystemConfigValidationError validationError = SystemConfigValidationError.builder().configKey(name).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrors().add(validationError);
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
        EmoteMissingValidationError validationError = EmoteMissingValidationError.builder().emoteKey(emoteKey).build();
        featureValidationResult.setValidationResult(false);
        featureValidationResult.getValidationErrors().add(validationError);
    }


}
