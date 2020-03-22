package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmoteServiceBean implements EmoteService {

    @Autowired
    private Bot botService;

    @Override
    public boolean isEmoteUsableByBot(Emote emote) {
        for (Guild guild : botService.getInstance().getGuilds()) {
            Emote emoteById = guild.getEmoteById(emote.getId());
            if(emoteById != null) {
                return true;
            }
        }
        return false;
    }
}
