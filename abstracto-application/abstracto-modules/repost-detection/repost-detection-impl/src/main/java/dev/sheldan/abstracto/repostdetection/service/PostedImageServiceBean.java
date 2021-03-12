package dev.sheldan.abstracto.repostdetection.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.repostdetection.service.management.PostedImageManagement;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostedImageServiceBean implements PostedImageService {

    @Autowired
    private PostedImageManagement postedImageManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void purgePostedImages(AUserInAServer aUserInAServer) {
        postedImageManagement.removePostedImagesOf(aUserInAServer);
    }

    @Override
    public void purgePostedImages(Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        postedImageManagement.removedPostedImagesIn(server);
    }
}
