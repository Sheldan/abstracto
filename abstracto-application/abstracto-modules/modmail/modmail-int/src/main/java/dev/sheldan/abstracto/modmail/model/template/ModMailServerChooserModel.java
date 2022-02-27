package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Container model used to define all the possible {@link ServerChoice} which are presented when the initial message is
 * sent to the bot
 */
@Getter
@Setter
@Builder
public class ModMailServerChooserModel {
    private ServerChoices choices;
}
