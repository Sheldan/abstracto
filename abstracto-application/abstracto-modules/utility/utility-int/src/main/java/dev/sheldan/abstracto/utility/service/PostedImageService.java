package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Guild;

public interface PostedImageService {
    void purgePostedImages(AUserInAServer member);
    void purgePostedImages(Guild guild);
}
