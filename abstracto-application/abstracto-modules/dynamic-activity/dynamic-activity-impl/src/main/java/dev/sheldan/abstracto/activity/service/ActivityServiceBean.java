package dev.sheldan.abstracto.activity.service;

import dev.sheldan.abstracto.activity.models.CustomActivity;
import dev.sheldan.abstracto.activity.service.management.ActivityManagementService;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Component
@Slf4j
public class ActivityServiceBean implements ActivityService {

    @Autowired
    private ActivityManagementService activityManagementService;

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Override
    @Transactional
    public void switchToOtherActivity() {
        List<CustomActivity> activities = activityManagementService.getAllActivities();
        if(!activities.isEmpty()) {
            CustomActivity chosen = activities.get(secureRandom.nextInt(activities.size()));
            log.info("Chosen activity {}.", chosen.getId());
            switchToActivity(chosen);
        } else {
            log.info("No activities configured.");
        }
    }

    @Override
    public void switchToActivity(CustomActivity activity) {
        JDA jda = botService.getInstance();
        String text = templateService.renderSimpleTemplate("dynamic_activity_" + activity.getTemplateKey());
        Activity jdaActivity = matchActivity(activity, text);
        jda.getPresence().setActivity(jdaActivity);
    }

    @Override
    public Activity matchActivity(CustomActivity activity, String text) {
        switch (activity.getType()) {
            case STREAMING:
                return Activity.streaming(text, "https://twitch.tv/a");
            case WATCHING:
                return Activity.watching(text);
            case LISTENING:
                return Activity.listening(text);
            case PLAYING:
            default:
                return Activity.playing(text);
        }
    }
}
