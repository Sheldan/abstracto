package dev.sheldan.abstracto.activity.service.management;

import dev.sheldan.abstracto.activity.repository.ActivityRepository;
import dev.sheldan.abstracto.activity.models.CustomActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityManagementServiceBean implements ActivityManagementService {

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public List<CustomActivity> getAllActivities() {
        return activityRepository.findAll();
    }
}
