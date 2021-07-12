package dev.sheldan.abstracto.activity.listener;

import dev.sheldan.abstracto.activity.service.ActivityService;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityStartupListener implements AsyncStartupListener {

    @Autowired
    private ActivityService activityService;

    @Override
    public void execute() {
        activityService.switchToOtherActivity();
    }
}
