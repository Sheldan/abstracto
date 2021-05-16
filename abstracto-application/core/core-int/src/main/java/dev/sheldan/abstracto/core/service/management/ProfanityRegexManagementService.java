package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;

import java.util.Optional;

public interface ProfanityRegexManagementService {
    ProfanityRegex createProfanityRegex(ProfanityGroup profanityGroup, String name, String regex, String replacement);
    ProfanityRegex createProfanityRegex(ProfanityGroup profanityGroup, String name, String regex);
    void deleteProfanityRegex(ProfanityRegex profanityRegex);
    void deleteProfanityRegex(ProfanityGroup group, String profanityName);
    boolean doesProfanityRegexExist(ProfanityGroup profanityGroup, String name);
    Optional<ProfanityRegex> getProfanityRegexOptional(ProfanityGroup profanityGroup, String name);
    Optional<ProfanityRegex> getProfanityRegexViaIdOptional(Long profanityRegexId);
    ProfanityRegex getProfanityRegexViaId(Long profanityRegexId);
}
