package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultEmote;

import java.util.List;

public interface DefaultEmoteManagementService {
    DefaultEmote getDefaultEmote(String key);
    List<DefaultEmote> getAllDefaultEmotes();
    List<String> getDefaultEmoteNames();
}
