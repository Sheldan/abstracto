package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Emote;

public interface EmoteService {
    boolean isEmoteUsableByBot(Emote emote);
}
