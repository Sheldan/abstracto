package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ProfanityGroupExistsException;
import dev.sheldan.abstracto.core.exception.ProfanityRegexExistsException;
import dev.sheldan.abstracto.core.exception.ProfanityRegexNotFoundException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.service.management.ProfanityGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ProfanityRegexManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ProfanityServiceBean implements ProfanityService {

    @Autowired
    private ProfanityGroupManagementService profanityGroupManagementService;

    @Autowired
    private ProfanityRegexManagementService profanityRegexManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    private Map<Long, List<PatternReplacement>> regex = new HashMap<>();

    @Override
    public String replaceProfanities(String input, Long serverId) {
        return replaceProfanitiesWithDefault(input, serverId, "");
    }

    @Override
    public String replaceProfanities(String input, Long serverId, String replacement) {
        if(regex.containsKey(serverId)) {
            List<PatternReplacement> regexes = regex.get(serverId);
            log.debug("Checking {} regexes for server {} with static replacement.", regexes.size(), serverId);
            for (PatternReplacement pattern: regexes) {
                Matcher matcher = pattern.getPattern().matcher(input);
                input = matcher.replaceAll(replacement);
            }
        }
        return input;
    }

    @Override
    public String replaceProfanitiesWithDefault(String input, Long serverId, String defaultReplacement) {
        if(regex.containsKey(serverId)) {
            List<PatternReplacement> regexes = regex.get(serverId);
            log.debug("Checking {} regexes for server {} with dynamic replacement.", regexes.size(), serverId);
            for (PatternReplacement pattern: regexes) {
                Matcher matcher = pattern.getPattern().matcher(input);
                String replacement = pattern.getReplacement() != null ? pattern.getReplacement() : defaultReplacement;
                input = matcher.replaceAll(replacement);
            }
        }
        return input;
    }

    @Override
    public boolean containsProfanity(String input, Long serverId) {
        return getProfanityRegex(input, serverId).isPresent();
    }

    @Override
    public Optional<ProfanityRegex> getProfanityRegex(String input, Long serverId) {
        if(regex.containsKey(serverId)) {
            List<PatternReplacement> regexes = regex.get(serverId);
            log.debug("Checking existence of {} regexes for server {}.", regexes.size(), serverId);
            for (PatternReplacement pattern: regexes) {
                Matcher matcher = pattern.getPattern().matcher(input);
                if(matcher.find()) {
                    return profanityRegexManagementService.getProfanityRegexViaIdOptional(pattern.profanityRegexId);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public ProfanityGroup createProfanityGroup(Long serverId, String profanityGroupName) {
        AServer server = serverManagementService.loadServer(serverId);
        if(profanityGroupManagementService.doesProfanityGroupExist(server, profanityGroupName)) {
            throw new ProfanityGroupExistsException();
        }
        return profanityGroupManagementService.createProfanityGroup(server, profanityGroupName);
    }

    @Override
    public void deleteProfanityGroup(Long serverId, String profanityGroupName) {
        AServer server = serverManagementService.loadServer(serverId);
        profanityGroupManagementService.deleteProfanityGroup(server, profanityGroupName);
        this.reloadRegex(serverId);
    }

    @Override
    public void deleteProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName) {
        AServer server = serverManagementService.loadServer(serverId);
        ProfanityGroup group = profanityGroupManagementService.getProfanityGroup(server, profanityGroupName);
        Optional<ProfanityRegex> profanityRegexOptional = profanityRegexManagementService.getProfanityRegexOptional(group, profanityRegexName);
        if(!profanityRegexOptional.isPresent()) {
            throw new ProfanityRegexNotFoundException();
        }
        profanityRegexManagementService.deleteProfanityRegex(profanityRegexOptional.get());
        this.reloadRegex(serverId);
    }

    @Override
    public ProfanityRegex createProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName, String regex) {
        return createProfanityRegex(serverId, profanityGroupName, profanityRegexName, regex, null);
    }

    @Override
    public ProfanityRegex createProfanityRegex(Long serverId, String profanityGroupName, String profanityRegexName, String regex, String replacement) {
        AServer server = serverManagementService.loadServer(serverId);
        ProfanityGroup group = profanityGroupManagementService.getProfanityGroup(server, profanityGroupName);
        if(profanityRegexManagementService.doesProfanityRegexExist(group, profanityRegexName)) {
            throw new ProfanityRegexExistsException();
        }
        ProfanityRegex created = profanityRegexManagementService.createProfanityRegex(group, profanityRegexName, regex, replacement);
        this.reloadRegex(serverId);
        return created;
    }

    @Override
    public void reloadRegex() {
        log.info("Reloading regex for all servers.");
        regex = new HashMap<>();
        List<ProfanityGroup> allGroups = profanityGroupManagementService.getAllGroups();
        allGroups.forEach(profanityGroup -> profanityGroup.getProfanities().forEach(profanityRegex -> {
            Long serverId = profanityGroup.getServer().getId();
            loadProfanityRegex(profanityRegex, serverId);
        }));
    }

    private void loadProfanityRegex(ProfanityRegex profanityRegex, Long serverId) {
        Pattern pattern = Pattern.compile(profanityRegex.getRegex(), Pattern.CASE_INSENSITIVE);
        List<PatternReplacement> newPatterns = new ArrayList<>();
        PatternReplacement patternReplacement = PatternReplacement
                .builder()
                .pattern(pattern)
                .replacement(profanityRegex.getReplacement())
                .profanityRegexId(profanityRegex.getId())
                .build();
        if (regex.containsKey(serverId)) {
            regex.get(serverId).add(patternReplacement);
        } else {
            newPatterns.add(patternReplacement);
            regex.put(serverId, newPatterns);
        }
    }

    @Override
    public void reloadRegex(Long serverId) {
        log.info("Reloading regex for server {}.", serverId);
        if(regex == null) {
            regex = new HashMap<>();
        }
        regex.remove(serverId);
        List<ProfanityGroup> allGroups = profanityGroupManagementService.getAllForServer(serverId);
        allGroups
                .forEach(profanityGroup -> profanityGroup.getProfanities()
                .forEach(profanityRegex -> loadProfanityRegex(profanityRegex, serverId)));
    }


    @Getter
    @Builder
    private static class PatternReplacement {
        private final Long profanityRegexId;
        private final Pattern pattern;
        private final String replacement;
    }
}
