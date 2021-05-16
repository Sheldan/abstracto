package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;

import java.util.Optional;

public interface ProfanityService {
    String replaceProfanities(String input, Long serverId);
    String replaceProfanities(String input, Long serverId, String replacement);
    String replaceProfanitiesWithDefault(String input, Long serverId, String replacement);
    boolean containsProfanity(String input, Long serverId);
    Optional<ProfanityRegex> getProfanityRegex(String input, Long serverId);
    ProfanityGroup createProfanityGroup(Long serverId, String profanityGroupName);
    void deleteProfanityGroup(Long serverId, String profanityGroupName);
    void deleteProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName);
    ProfanityRegex createProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName, String regex);
    ProfanityRegex createProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName, String regex, String replacement);
    void reloadRegex();
    void reloadRegex(Long serverId);
}
