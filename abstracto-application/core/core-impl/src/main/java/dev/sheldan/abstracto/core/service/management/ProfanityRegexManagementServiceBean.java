package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.ProfanityRegexNotFoundException;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.repository.ProfanityRegexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ProfanityRegexManagementServiceBean  implements ProfanityRegexManagementService {
    @Autowired
    private ProfanityRegexRepository repository;

    @Override
    public ProfanityRegex createProfanityRegex(ProfanityGroup profanityGroup, String name, String regex, String replacement) {
        ProfanityRegex creatingRegex = ProfanityRegex
                .builder()
                .regex(regex)
                .regexName(name)
                .group(profanityGroup)
                .replacement(replacement)
                .build();
        log.info("Creating profanity regex for server {} in group {}.", profanityGroup.getServer().getId(), profanityGroup.getId());
        return repository.save(creatingRegex);
    }

    @Override
    public ProfanityRegex createProfanityRegex(ProfanityGroup profanityGroup, String name, String regex) {
        return createProfanityRegex(profanityGroup, name, regex, null);
    }

    @Override
    public void deleteProfanityRegex(ProfanityRegex profanityRegex) {
        profanityRegex.getGroup().getProfanities().remove(profanityRegex);
        profanityRegex.setGroup(null);
    }

    @Override
    public void deleteProfanityRegex(ProfanityGroup group, String profanityName) {
        log.info("Deleting profanity regex for group {} in server {}.", group.getId(), group.getServer().getId());
        repository.deleteByGroupAndRegexNameIgnoreCase(group, profanityName);
    }

    @Override
    public boolean doesProfanityRegexExist(ProfanityGroup profanityGroup, String name) {
        return getProfanityRegexOptional(profanityGroup, name).isPresent();
    }

    @Override
    public Optional<ProfanityRegex> getProfanityRegexOptional(ProfanityGroup profanityGroup, String name) {
        return repository.findByGroupAndRegexNameIgnoreCase(profanityGroup, name);
    }

    @Override
    public Optional<ProfanityRegex> getProfanityRegexViaIdOptional(Long profanityRegexId) {
        return repository.findById(profanityRegexId);
    }

    @Override
    public ProfanityRegex getProfanityRegexViaId(Long profanityRegexId) {
        return getProfanityRegexViaIdOptional(profanityRegexId).orElseThrow(ProfanityRegexNotFoundException::new);
    }
}
