package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.exception.*;
import dev.sheldan.abstracto.assignableroles.model.AssignableRolePlacePayload;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.template.*;
import dev.sheldan.abstracto.assignableroles.service.management.*;
import dev.sheldan.abstracto.core.command.exception.CommandParameterKeyValueWrongTypeException;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.EmoteNotUsableException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AssignableRolePlaceServiceBean implements AssignableRolePlaceService {

    public static final String ASSIGNABLE_ROLES_CONFIG_POST_TEMPLATE_KEY = "assignable_roles_config_post";
    public static final String ASSIGNABLE_ROLES_POST_TEMPLATE_KEY = "assignable_roles_post";
    public static final String ASSIGNABLE_ROLE_PLACES_OVERVIEW_TEMPLATE_KEY = "assignable_role_places_overview";
    public static final int MAX_ASSIGNABLE_ROLES_PER_POST = ComponentService.MAX_BUTTONS_PER_ROW * 5;
    public static final String ASSIGNABLE_ROLE_COMPONENT_ORIGIN = "assignableRoleButton";
    @Autowired
    private AssignableRolePlaceManagementService rolePlaceManagementService;

    @Autowired
    private AssignableRoleManagementService assignableRoleManagementServiceBean;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private AssignableRolePlaceServiceBean self;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private AssignableRolePlaceManagementServiceBean assignableRolePlaceManagementServiceBean;

    @Autowired
    private AssignableRoleConditionService assignableRoleConditionService;

    @Override
    public void createAssignableRolePlace(String name, AChannel channel, String text) {
        if (rolePlaceManagementService.doesPlaceExist(channel.getServer(), name)) {
            throw new AssignableRolePlaceAlreadyExistsException(name);
        }
        rolePlaceManagementService.createPlace(name, channel, text);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> addRoleToAssignableRolePlace(AServer server, String placeName, Role role, FullEmote fakeEmote, String description) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        if (assignableRolePlace.getAssignableRoles().size() > MAX_ASSIGNABLE_ROLES_PER_POST) {
            throw new AssignableRolePlaceMaximumRolesException();
        }
        if (assignableRolePlace.getAssignableRoles().stream().anyMatch(assignableRole -> assignableRole.getRole().getId().equals(role.getIdLong()))) {
            throw new AssignableRoleAlreadyDefinedException(role, placeName);
        }
        Long placeId = assignableRolePlace.getId();
        Long serverId = server.getId();
        if (fakeEmote != null && fakeEmote.getEmote() != null) {
            // it only may be unusable if its a custom emote
            log.debug("Using custom emote {} to create assignable role {} for  assignable role place {} in server {}.",
                    fakeEmote.getEmote().getId(), role.getId(), placeId, serverId);
            if (!emoteService.isEmoteUsableByBot(fakeEmote.getEmote()) && fakeEmote.getEmote().isAvailable()) {
                throw new EmoteNotUsableException(fakeEmote.getEmote());
            }
        }
        log.debug("There are already message posts on for the assignable role place {}.", assignableRolePlace.getId());
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(server.getId(), assignableRolePlace.getChannel().getId());
        if (channelOptional.isPresent()) {
            TextChannel textChannel = channelOptional.get();
            String buttonId = componentService.generateComponentId();
            String emoteMarkdown = fakeEmote != null ? fakeEmote.getEmoteRepr() : null;
            if (assignableRolePlace.getMessageId() != null) {
                return componentService.addButtonToMessage(assignableRolePlace.getMessageId(), textChannel, buttonId, description, emoteMarkdown, ButtonStyle.PRIMARY)
                        .thenAccept(message -> self.persistAssignableRoleAddition(placeId, role, description, fakeEmote, buttonId));
            } else {
                self.persistAssignableRoleAddition(placeId, role, description, fakeEmote, buttonId);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            throw new ChannelNotInGuildException(assignableRolePlace.getChannel().getId());
        }
    }

    @Transactional
    public void persistAssignableRoleAddition(Long placeId, Role role, String description, FullEmote fakeEmote, String componentId) {
        AssignableRolePlace place = assignableRolePlaceManagementServiceBean.findByPlaceId(placeId);
        ComponentPayload payload = persistButtonCallback(place, componentId, role.getIdLong());
        assignableRoleManagementServiceBean.addRoleToPlace(fakeEmote, role, description, place, payload);
    }

    @Override
    public CompletableFuture<Void> removeRoleFromAssignableRolePlace(AServer server, String placeName, ARole role) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        Long assignableRolePlaceId = assignableRolePlace.getId();
        for (AssignableRole assignableRole : assignableRolePlace.getAssignableRoles()) {
            if (assignableRole.getRole().getId().equals(role.getId())) {
                return removeButtonFromAssignableRolePlace(assignableRole, assignableRolePlace).thenAccept(aVoid ->
                        self.deleteAssignableRoleFromPlace(assignableRolePlaceId, assignableRole.getId())
                );
            }
        }
        throw new AssignableRoleNotFoundException(role.getId());
    }

    private CompletableFuture<Void> removeButtonFromAssignableRolePlace(AssignableRole assignableRole, AssignableRolePlace assignableRolePlace) {
        String componentId = assignableRole.getComponentPayload().getId();
        return channelService.retrieveMessageInChannel(assignableRolePlace.getServer().getId(), assignableRolePlace.getChannel().getId(), assignableRolePlace.getMessageId())
                .thenCompose(message ->
                        componentService.removeComponentWithId(message, componentId, true)
                );
    }

    @Transactional
    public void deleteAssignableRoleFromPlace(Long placeId, Long assignableRoleId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(placeId);
        log.info("Deleting the entry for assignable role {} in assignable role place {}.", assignableRoleId, placeId);
        Optional<AssignableRole> roleToRemoveOptional = assignableRolePlace
                .getAssignableRoles()
                .stream()
                .filter(role -> role.getId().equals(assignableRoleId))
                .findAny();
        roleToRemoveOptional.ifPresent(assignableRole -> {
            ComponentPayload componentPayload = assignableRole.getComponentPayload();
            assignableRoleManagementServiceBean.deleteAssignableRole(assignableRole);
            componentPayloadManagementService.deletePayload(componentPayload);
        });

        if (!roleToRemoveOptional.isPresent()) {
            log.warn("Assignable role with ID {} was not present in assignable role place {}.", assignableRoleId, placeId);
        }
    }

    @Override
    public CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Setting up assignable role place {} in server {} towards channel {}.", assignableRolePlace.getId(), server.getId(), assignableRolePlace.getChannel().getId());
        CompletableFuture<Void> oldPostDeletionFuture = deleteExistingMessagePostsForPlace(assignableRolePlace);
        Long serverId = server.getId();
        Long assignablePlaceId = assignableRolePlace.getId();
        CompletableFuture<Void> postingFuture = new CompletableFuture<>();
        oldPostDeletionFuture.whenComplete((unused, throwable) -> {
            if (throwable != null) {
                log.warn("Not able to delete old messages of assignable role place {} in server {}.", assignablePlaceId, serverId);
            }
            try {
                self.createAssignableRolePlacePost(serverId, assignablePlaceId)
                        .thenAccept(unused1 -> postingFuture.complete(null))
                        .exceptionally(innerThrowable -> {
                            postingFuture.completeExceptionally(innerThrowable);
                            return null;
                        });
            } catch (Exception ex) {
                postingFuture.completeExceptionally(ex);
            }
        });
        return postingFuture;
    }

    @Override
    public CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place) {
        AssignablePostMessage model = prepareAssignablePostMessageModel(place);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model, place.getServer().getId());
        Long channelId = place.getChannel().getId();
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(place.getServer().getId(), channelId);
        if (channelOptional.isPresent()) {
            log.info("Refreshing text for assignable role place {} in channel {} in post {}.", place.getId(), channelId, place.getMessageId());
            return channelService.editEmbedMessageInAChannel(messageToSend.getEmbeds().get(0), channelOptional.get(), place.getMessageId()).thenCompose(message -> CompletableFuture.completedFuture(null));
        } else {
            throw new ChannelNotInGuildException(channelId);
        }
    }

    @Override
    public CompletableFuture<Void> setAssignablePlaceActiveTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if (newValue) {
            return this.activateAssignableRolePlace(place);
        } else {
            return this.deactivateAssignableRolePlace(place);
        }
    }

    private CompletableFuture<Void> deleteExistingMessagePostsForPlace(AssignableRolePlace assignableRolePlace) {
        if (assignableRolePlace.getMessageId() != null) {
            return messageService.deleteMessageInChannelInServer(assignableRolePlace.getServer().getId(), assignableRolePlace.getChannel().getId(), assignableRolePlace.getMessageId());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> deactivateAssignableRolePlace(AServer server, String name) {
        return setAssignablePlaceActiveTo(server, name, false);
    }

    @Override
    public CompletableFuture<Void> deactivateAssignableRolePlace(AssignableRolePlace place) {
        log.info("Deactivating assignable role place {} in server {}", place.getId(), place.getServer().getId());
        return channelService.retrieveMessageInChannel(place.getServer().getId(), place.getChannel().getId(), place.getMessageId())
                .thenCompose(message ->
                        componentService.disableAllButtons(message)
                );
    }

    @Override
    public CompletableFuture<Void> activateAssignableRolePlace(AServer server, String name) {
        return setAssignablePlaceActiveTo(server, name, true);
    }

    @Override
    public CompletableFuture<Void> activateAssignableRolePlace(AssignableRolePlace place) {
        log.info("Activating assignable role place {} in server {}", place.getId(), place.getServer().getId());
        return channelService.retrieveMessageInChannel(place.getServer().getId(), place.getChannel().getId(), place.getMessageId())
                .thenCompose(message ->
                        componentService.enableAllButtons(message)
                );
    }

    @Override
    public void setAssignablePlaceUniqueTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if (newValue) {
            this.uniqueAssignableRolePlace(place);
        } else {
            this.multipleAssignableRolePlace(place);
        }
    }

    @Override
    public void uniqueAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceUniqueTo(server, name, true);
    }

    @Override
    public void uniqueAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place uniqueness {} in server {} to {}", place.getId(), place.getServer().getId(), true);
        place.setUniqueRoles(true);
    }

    @Override
    public void multipleAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceUniqueTo(server, name, false);
    }

    @Override
    public void multipleAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place uniqueness {} in server {} to {}", place.getId(), place.getServer().getId(), false);
        place.setUniqueRoles(false);
    }

    @Override
    public CompletableFuture<Void> showAssignablePlaceConfig(AServer server, String name, TextChannel channel) {
        Guild guild = guildService.getGuildById(server.getId());
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Showing assignable role place config for place {} in channel {} on server {}.", place.getId(), channel.getId(), server.getId());
        AssignableRolePlaceConfig configModel = convertPlaceToAssignableRolePlaceConfig(guild, place);
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(ASSIGNABLE_ROLES_CONFIG_POST_TEMPLATE_KEY, configModel, channel));
    }

    private AssignableRolePlaceConfig convertPlaceToAssignableRolePlaceConfig(Guild guild, AssignableRolePlace place) {
        List<AssignableRolePlaceConfigRole> roles = new ArrayList<>();
        List<AssignableRole> assignableRoles = place.getAssignableRoles();
        for (AssignableRole role : assignableRoles) {
            Role jdaRole = guild.getRoleById(role.getRole().getId());
            RoleDisplay display = jdaRole != null ? RoleDisplay.fromRole(jdaRole) : RoleDisplay.fromARole(role.getRole());
            AssignableRolePlaceConfigRole postRole = AssignableRolePlaceConfigRole
                    .builder()
                    .description(role.getDescription())
                    .emoteMarkDown(role.getEmoteMarkdown())
                    .conditions(assignableRoleConditionService.getConditionDisplays(role.getConditions()))
                    .roleDisplay(display)
                    .build();
            roles.add(postRole);
        }
        TextChannel placeChannel = guild.getTextChannelById(place.getChannel().getId());
        return AssignableRolePlaceConfig
                .builder()
                .roles(roles)
                .placeName(place.getKey())
                .placeText(place.getText())
                .uniqueRoles(place.getUniqueRoles())
                .channelDisplay(ChannelDisplay.fromChannel(placeChannel))
                .build();
    }

    @Override
    public CompletableFuture<Void> moveAssignableRolePlace(AServer server, String name, TextChannel newChannel) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        CompletableFuture<Void> oldPostDeletionFuture = deleteExistingMessagePostsForPlace(place);
        Long serverId = server.getId();
        Long assignablePlaceId = place.getId();
        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        oldPostDeletionFuture.whenComplete((unused, throwable) -> {
            if (throwable != null) {
                log.warn("Not able to delete old messages of assignable role place {} in server {}.", assignablePlaceId, serverId);
            }
            try {
                self.setupAssignableRolePlaceInChannel(serverId, assignablePlaceId, newChannel)
                        .thenAccept(unused1 -> self.updateAssignableRolePlaceChannel(name, newChannel))
                        .thenAccept(unused1 -> returnFuture.complete(null))
                        .exceptionally(innerThrowable -> {
                            returnFuture.completeExceptionally(innerThrowable);
                            return null;
                        });
            } catch (Exception ex) {
                returnFuture.completeExceptionally(ex);
            }
        });

        return returnFuture;
    }

    @Transactional
    public void updateAssignableRolePlaceChannel(String name, TextChannel textChannel) {
        AChannel channel = channelManagementService.loadChannel(textChannel.getIdLong());
        rolePlaceManagementService.moveAssignableRolePlace(name, channel);
    }

    @Override
    public CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);

        Long placeId = place.getId();
        CompletableFuture<Void> deleteFuture = deleteExistingMessagePostsForPlace(place);
        return deleteFuture.thenAccept(unused -> self.deleteAssignableRolePlaceInDatabase(placeId));
    }

    @Transactional
    public void deleteAssignableRolePlaceInDatabase(Long placeId) {
        AssignableRolePlace place = rolePlaceManagementService.findByPlaceId(placeId);
        place.getAssignableRoles()
                .forEach(assignableRole -> componentPayloadManagementService.deletePayload(assignableRole.getComponentPayload()));
        rolePlaceManagementService.deleteAssignablePlace(place);
    }

    @Override
    public CompletableFuture<Void> changeTextAsync(AServer server, String name, String newText) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Changing text of assignable role place {} in server {}.", place.getId(), server.getId());
        place.setText(newText);
        return refreshTextFromPlace(place);
    }

    @Override
    public CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, String newValue) {
        Boolean booleanValue = BooleanUtils.toBooleanObject(newValue);
        if (booleanValue == null) {
            throw new CommandParameterKeyValueWrongTypeException(Arrays.asList("yes", "no", "true", "false", "on", "off"));
        }
        if (keyToChange == AssignableRolePlaceParameterKey.UNIQUE) {
            setAssignablePlaceUniqueTo(server, name, booleanValue);
            return CompletableFuture.completedFuture(null);
        }
        throw new AssignableRolePlaceIllegalConfigurationException();
    }

    @Override
    public CompletableFuture<Void> showAllAssignableRolePlaces(AServer server, TextChannel channel) {
        List<AssignableRolePlace> assignableRolePlaces = rolePlaceManagementService.findAllByServer(server);
        Guild guild = channel.getGuild();
        List<AssignableRolePlaceConfig> placeConfigs = assignableRolePlaces
                .stream()
                .map(place -> convertPlaceToAssignableRolePlaceConfig(guild, place))
                .collect(Collectors.toList());
        AssignablePlaceOverview overViewModel = AssignablePlaceOverview
                .builder()
                .places(placeConfigs)
                .build();
        log.info("Showing overview over all assignable role places for server {} in channel {}.", server.getId(), channel.getId());
        List<CompletableFuture<Message>> promises = channelService.sendEmbedTemplateInTextChannelList(ASSIGNABLE_ROLE_PLACES_OVERVIEW_TEMPLATE_KEY, overViewModel, channel);
        return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> sendAssignablePostMessage(AssignableRolePlace place, TextChannel channel) {
        AssignablePostMessage model = prepareAssignablePostMessageModel(place);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model, place.getServer().getId());
        CompletableFuture<Message> postFuture = channelService.sendMessageToSendToChannel(messageToSend, channel).get(0);
        Long placeId = model.getPlaceId();
        return postFuture.thenCompose(unused -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            try {
                self.persistAssignablePlaceMessageId(placeId, postFuture);
                future.complete(null);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
                return future;
            }
            return future;
        });
    }

    @Transactional
    public void persistAssignablePlaceMessageId(Long placeId, CompletableFuture<Message> messageFuture) {
        AssignableRolePlace place = assignableRolePlaceManagementServiceBean.findByPlaceId(placeId);
        Message message = messageFuture.join();
        place.setMessageId(message.getIdLong());
    }

    private ComponentPayload persistButtonCallback(AssignableRolePlace place, String buttonId, Long roleId) {
        AssignableRolePlacePayload payload = AssignableRolePlacePayload
                .builder()
                .roleId(roleId)
                .placeId(place.getId())
                .build();
        return componentPayloadService.createButtonPayload(buttonId, payload, ASSIGNABLE_ROLE_COMPONENT_ORIGIN, place.getServer());
    }

    private AssignablePostMessage prepareAssignablePostMessageModel(AssignableRolePlace place) {
        List<AssignablePostRole> roles = new ArrayList<>();
        List<AssignableRole> rolesToAdd = place.getAssignableRoles();
        int maxPosition = 0;
        Map<String, Long> componentIdMap = new HashMap<>();
        if (!rolesToAdd.isEmpty()) {
            rolesToAdd.forEach(assignableRole -> {
                String componentId = assignableRole.getComponentPayload().getId();
                componentIdMap.put(componentId, assignableRole.getRole().getId());
                roles.add(AssignablePostRole
                        .builder()
                        .componentId(componentId)
                        .description(assignableRole.getDescription())
                        .emoteMarkDown(assignableRole.getEmoteMarkdown())
                        .build());
            });
        }
        return AssignablePostMessage
                .builder()
                .roles(roles)
                .placeId(place.getId())
                .componentIdToRole(componentIdMap)
                .placeDescription(place.getText())
                .maxPosition(maxPosition)
                .build();
    }

    @Transactional
    public CompletableFuture<Void> createAssignableRolePlacePost(Long serverId, Long assignablePlaceId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(serverId, assignableRolePlace.getChannel().getId());
        if (channelOptional.isPresent()) {
            TextChannel channel = channelOptional.get();
            log.info("Sending assignable role place posts for place {} in channel {} in server {}.", assignableRolePlace.getId(), channel.getId(), serverId);
            return sendAssignablePostMessage(assignableRolePlace, channel);
        } else {
            log.warn("Channel to create assignable role post in does not exist.");
            throw new AssignableRolePlaceChannelDoesNotExistException(assignableRolePlace.getChannel().getId(), assignableRolePlace.getKey());
        }
    }


    @Transactional
    public CompletableFuture<Void> setupAssignableRolePlaceInChannel(Long serverId, Long assignablePlaceId, TextChannel textChannel) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        log.info("Sending assignable role place posts for place {} in channel {} in server {}.", assignableRolePlace.getId(), textChannel.getId(), serverId);
        return sendAssignablePostMessage(assignableRolePlace, textChannel);
    }


}
