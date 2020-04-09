package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AEmote;
import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmoteConverter {

    @Autowired
    private ServerConverter serverConverter;

    public EmoteDto fromEmote(AEmote emote) {
        return EmoteDto
                .builder()
                .animated(emote.getAnimated())
                .custom(emote.getCustom())
                .emoteId(emote.getEmoteId())
                .emoteKey(emote.getEmoteKey())
                .name(emote.getName())
                .Id(emote.getId())
                .server(serverConverter.convertServer(emote.getServerRef()))
                .build();
    }
}
