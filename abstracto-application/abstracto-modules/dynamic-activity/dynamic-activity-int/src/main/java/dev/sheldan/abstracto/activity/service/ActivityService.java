package dev.sheldan.abstracto.activity.service;

import dev.sheldan.abstracto.activity.models.CustomActivity;

public interface ActivityService {
    void switchToOtherActivity();
    void switchToActivity(CustomActivity activity);
    net.dv8tion.jda.api.entities.Activity matchActivity(CustomActivity activity, String text);
}
