package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.modmail.models.dto.ServerChoice;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Container model used to define all the possible {@link ServerChoice} which are presented when the initial message is
 * sent to the bot
 */
@Getter
@Setter
@Builder
public class ModMailServerChooserModel {
    /**
     * A list of {@link ServerChoice} which contains the common servers of the user and the bot, but only those
     * in which the mod mail feature is currently enabled
     */
    private List<ServerChoice> commonGuilds;
}
