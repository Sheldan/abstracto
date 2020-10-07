package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceAlreadyExistsException;
import dev.sheldan.abstracto.assignableroles.exceptions.AssignableRolePlaceChannelDoesNotExist;
import dev.sheldan.abstracto.assignableroles.exceptions.EmoteNotInAssignableRolePlaceException;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.models.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.models.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.models.templates.*;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRoleManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlaceManagementService;
import dev.sheldan.abstracto.assignableroles.service.management.AssignableRolePlacePostManagementService;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.exception.CommandParameterKeyValueWrongTypeException;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.EmoteNotUsableException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
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
    @Autowired
    private AssignableRolePlaceManagementService rolePlaceManagementService;

    @Autowired
    private AssignableRoleManagementService assignableRoleManagementServiceBean;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private AssignableRolePlaceServiceBean self;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private AssignableRolePlacePostManagementService postManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AssignableRoleService roleService;

    @Override
    public void createAssignableRolePlace(AServer server, String name, AChannel channel, String text) {
        if(rolePlaceManagementService.doesPlaceExist(server, name)) {
            throw new AssignableRolePlaceAlreadyExistsException(name);
        }
        rolePlaceManagementService.createPlace(server, name, channel, text);
    }

    @Override
    public boolean hasAssignableRolePlaceEmote(AServer server, String placeName, AEmote emote) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        return  hasAssignableRolePlaceEmote(assignableRolePlace, emote);
    }

    @Override
    public boolean isPositionUsed(AServer server, String placeName, Integer position) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        return assignableRolePlace.getAssignableRoles().stream().anyMatch(role -> role.getPosition().equals(position));
    }

    @Override
    public void setEmoteToPosition(AServer server, String placeName, FullEmote emote, Integer position) {
        if(isPositionUsed(server, placeName, position)) {
            throw new AbstractoTemplatedException("Position is already used", "assignable_role_place_position_exists_exception");
        }
        if(!hasAssignableRolePlaceEmote(server, placeName, emote.getFakeEmote())) {
            throw new AbstractoTemplatedException("Place does not have emote assigned.", "assignable_role_place_position_exists_exception");
        }
        Integer emoteId = emote.getFakeEmote().getId();
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        log.info("Setting emote {} to position {} in assignable role place {} in server {}.",
                emoteId, position, assignableRolePlace.getId(), assignableRolePlace.getServer().getId());
        Optional<AssignableRole> emoteOptional = assignableRolePlace.getAssignableRoles().stream().filter(role -> role.getEmote().getId().equals(emoteId)).findFirst();
        if(emoteOptional.isPresent()) {
            AssignableRole toChange = emoteOptional.get();
            toChange.setPosition(position);
        } else {
            throw new EmoteNotInAssignableRolePlaceException(emote, placeName);
        }
    }

    @Override
    public boolean hasAssignableRolePlaceEmote(AssignableRolePlace place, AEmote emote) {
        for (AssignableRole assignableRole : place.getAssignableRoles()) {
            if(emoteService.compareAEmote(assignableRole.getEmote(), emote)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompletableFuture<Void> addRoleToAssignableRolePlace(AServer server, String placeName, ARole role, FullEmote fakeEmote, String description) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        Long placeId = assignableRolePlace.getId();
        Long roleId = role.getId();
        Long serverId = server.getId();
        boolean emoteUsable = true;
        if(fakeEmote.getEmote() != null) {
            // it only may be unusable if its a custom emote
            log.trace("Using custom emote {} to create assignable role {} for  assignable role place {} in server {}.",
                    fakeEmote.getEmote().getId(), roleId, placeId, serverId);
            emoteUsable = emoteService.isEmoteUsableByBot(fakeEmote.getEmote()) && fakeEmote.getEmote().isAvailable();
        }
        if(emoteUsable) {
            List<AssignableRolePlacePost> existingMessagePosts = assignableRolePlace.getMessagePosts();
            existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));

            if(!assignableRolePlace.getMessagePosts().isEmpty()){
                log.trace("There are already message posts on for the assignable role place {}.", assignableRolePlace.getId());
                AssignableRolePlacePost latestPost = existingMessagePosts.get(assignableRolePlace.getMessagePosts().size() - 1);
                AssignablePostMessage model = prepareAssignablePostMessageModel(assignableRolePlace);
                boolean forceNewMessage = latestPost.getAssignableRoles().size() >= 20;
                log.info("We need to add a new message post {} for assignable role place {} in server {} in channel {}.",
                        forceNewMessage, placeId, serverId, assignableRolePlace.getChannel().getId());
                AssignablePostRole newAssignableRole = AssignablePostRole
                        .builder()
                        .description(description)
                        .emote(fakeEmote)
                        .forceNewMessage(forceNewMessage)
                        .build();
                model.getRoles().add(newAssignableRole);
                MessageToSend messageToSend = templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model);
                // add it to the last currently existing post
                Optional<TextChannel> channelOptional = channelService.getTextChannelInGuild(server.getId(), latestPost.getUsedChannel().getId());
                if(channelOptional.isPresent()) {
                    TextChannel textChannel = channelOptional.get();
                    if(latestPost.getAssignableRoles().size() < 20) {
                        log.trace("Adding reaction to existing post {} in channel {} in server {} for assignable role place {}.",
                                latestPost.getId(), assignableRolePlace.getChannel().getId(), serverId, placeId);
                        return addReactionToExistingAssignableRolePlacePost(fakeEmote, description, roleId, latestPost, messageToSend, textChannel);
                    } else {
                        log.trace("Adding new post to assignable role place {} in channel {} in server {}.",
                                placeId, assignableRolePlace.getChannel().getId(), server.getId());
                        return addNewMessageToAssignableRolePlace(placeId, fakeEmote, description, roleId, serverId, messageToSend, textChannel);
                    }
                } else {
                    throw new ChannelNotFoundException(latestPost.getUsedChannel().getId());
                }
            } else {
                log.trace("Added emote to assignable place {} in server {}, but no message post yet.", placeId, serverId);
                self.addAssignableRoleInstanceWithoutPost(placeId, roleId, fakeEmote, description, serverId);
            }
        } else {
            throw new EmoteNotUsableException(fakeEmote.getEmote());
        }
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> addReactionToExistingAssignableRolePlacePost(FullEmote fakeEmote, String description, Long roleId, AssignableRolePlacePost latestPost, MessageToSend messageToSend, TextChannel textChannel) {
        // TODO maybe refactor to use the same message object, so we dont need to retrieve it twice and do in parallel
        Long serverId = latestPost.getAssignablePlace().getServer().getId();
        Long placeId = latestPost.getAssignablePlace().getId();
        Long latestPostId = latestPost.getId();
        int messagePostSize = latestPost.getAssignablePlace().getMessagePosts().size();
        return textChannel.retrieveMessageById(latestPost.getId()).submit()
                .thenCompose(message -> {
                    log.trace("Adding reaction to message {} in server {} for assignable role place {}.", message.getId(), serverId, placeId);
                    return messageService.addReactionToMessageWithFuture(fakeEmote.getFakeEmote(), serverId, message);
                }).thenCompose(aVoid -> {
                    log.trace("Editing embed for assignable role place post {} in assignable role place {} in server {}.", latestPostId, placeId, serverId);
                    MessageEmbed embedToUse = messageToSend.getEmbeds().get(messagePostSize - 1);
                    return channelService.editEmbedMessageInAChannel(embedToUse, textChannel, latestPostId);
                }).thenAccept(message ->
                    self.addAssignableRoleInstanceWithPost(message.getIdLong(), placeId, roleId, description, fakeEmote, serverId)
                );
    }

    private CompletableFuture<Void> addNewMessageToAssignableRolePlace(Long placeId, FullEmote fakeEmote, String description, Long roleId, Long serverId, MessageToSend messageToSend, TextChannel textChannel) {
        MessageEmbed embedToUse = messageToSend.getEmbeds().get(messageToSend.getEmbeds().size() - 1);
        return channelService.sendEmbedToChannel(embedToUse, textChannel)
                .thenCompose(message -> {
                    log.trace("Adding reaction for role {} to newly created message {} for assignable role place {} in server {}.", roleId, message.getId(), placeId, serverId);
                    return messageService.addReactionToMessageWithFuture(fakeEmote.getFakeEmote(), serverId, message)
                                .thenAccept(aVoid ->
                                    self.addNewlyCreatedAssignablePlacePost(placeId, description, roleId, serverId, message, fakeEmote)
                                );
                });
    }

    @Transactional
    public void addNewlyCreatedAssignablePlacePost(Long placeId, String description,Long roleId, Long serverId, Message message, FullEmote fakeEmote) {
        log.info("Storing newly created assignable role place post {} for place {} in server {}.", message.getId(), placeId, serverId);
        ARole role = roleManagementService.findRole(roleId);
        AssignableRolePlace loadedPlace = rolePlaceManagementService.findByPlaceId(placeId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.trace("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);

        AssignableRolePlacePost newPost = AssignableRolePlacePost
                .builder()
                .id(message.getIdLong())
                .usedChannel(loadedPlace.getChannel())
                .assignablePlace(loadedPlace)
                .build();

        loadedPlace.getMessagePosts().add(newPost);
        assignableRoleManagementServiceBean.addRoleToPlace(loadedPlace, emote, role, description, newPost);
    }

    @Transactional
    public void addAssignableRoleInstanceWithPost(Long messageId, Long placeId, Long roleId, String description, FullEmote fakeEmote, Long serverId) {
        log.info("Storing newly created assignable role {} to post {} to assignable role place {} in server {}.", roleId, messageId, placeId, serverId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.trace("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);
        assignableRoleManagementServiceBean.addRoleToPlace(placeId, emote.getId(), roleId, description, messageId);
    }

    @Transactional
    public void addAssignableRoleInstanceWithoutPost(Long placeId, Long roleId, FullEmote fakeEmote, String description, Long serverId) {
        log.info("Storing newly created assignable role {} without post to assignable role place {} in server {}.", roleId, placeId, serverId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.trace("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);
        assignableRoleManagementServiceBean.addRoleToPlace(placeId, emote.getId(), roleId, description);
    }

    @Override
    public CompletableFuture<Void> removeRoleFromAssignableRolePlace(AServer server, String placeName, FullEmote emote) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        Long assignableRolePlaceId = assignableRolePlace.getId();
        for (AssignableRole assignableRole : assignableRolePlace.getAssignableRoles()) {
            if(emoteService.compareAEmote(assignableRole.getEmote(), emote.getFakeEmote())) {
                log.info("Removing assignable role {} identified by emote {} from assignable role place {} in server {}.",
                        assignableRole.getId(), assignableRole.getEmote().getId(), assignableRolePlace.getId(), assignableRolePlace.getServer().getId());
                return removeRoleFromAssignablePlace(assignableRole, assignableRolePlace).thenAccept(aVoid ->
                    self.deleteAssignableRoleFromPlace(assignableRolePlaceId, assignableRole.getId())
                );
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void deleteAssignableRoleFromPlace(Long placeId, Long assignableRoleId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(placeId);
        log.info("Deleting the entry for assignable role {} in assignable role place {}.", assignableRoleId, placeId);
        Optional<AssignableRole> roleToRemoveOptional = assignableRolePlace.getAssignableRoles().stream().filter(role -> role.getId().equals(assignableRoleId)).findAny();
        roleToRemoveOptional.ifPresent(assignableRole -> {
            assignableRolePlace.getAssignableRoles().remove(assignableRole);
            assignableRole.setAssignablePlace(null);
        });
    }

    private CompletableFuture<Void> removeRoleFromAssignablePlace(AssignableRole role, AssignableRolePlace assignableRolePlace) {
        AssignableRolePlacePost post = role.getAssignableRolePlacePost();
        if(post != null) {
            AServer server = assignableRolePlace.getServer();
            TextChannel textChannel = botService.getTextChannelFromServer(server.getId(), post.getUsedChannel().getId());
            List<AssignableRole> assignableRoles = assignableRolePlace.getAssignableRoles();
            assignableRoles.sort(Comparator.comparing(AssignableRole::getPosition));
            Long messageId = post.getId();
            log.trace("Removing field describing assignable role {} in assignable role place {} from post {}.", role.getId(), assignableRolePlace.getId(), messageId);
            CompletableFuture<Message> fieldEditing = channelService.removeFieldFromMessage(textChannel, messageId, assignableRoles.indexOf(role));
            log.trace("Clearing reaction for emote {} on assignable role post {} in assignable role place {}.", role.getEmote().getId(), messageId, assignableRolePlace.getId());
            CompletableFuture<Void> reactionRemoval  = messageService.clearReactionFromMessageWithFuture(role.getEmote(), assignableRolePlace.getServer().getId(), role.getAssignableRolePlacePost().getUsedChannel().getId(), role.getAssignableRolePlacePost().getId());
            return CompletableFuture.allOf(fieldEditing, reactionRemoval);
        } else {
            // this case comes from the situation in which, the emote was deleted and he initial post setup failed
            log.warn("Reaction {} to remove does not have a post attached. The post needs to be setup again, it is most likely not functioning currently anyway.", role.getEmote().getEmoteId());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Setting up assignable role place {} in server {} towards channel {}.", assignableRolePlace.getId(), server.getId(), assignableRolePlace.getChannel().getId());
        List<CompletableFuture<Void>> oldPostDeletionFutures = deleteExistingMessagePostsForPlace(assignableRolePlace);
        assignableRolePlace.getMessagePosts().clear();
        assignableRolePlace.getAssignableRoles().forEach(assignableRole ->
            assignableRole.setAssignableRolePlacePost(null)
        );
        Long serverId = server.getId();
        Long assignablePlaceId = assignableRolePlace.getId();
        return CompletableFuture.allOf(oldPostDeletionFutures.toArray(new CompletableFuture[0]))
                .thenCompose(aVoid -> self.createAssignableRolePlacePosts(serverId, assignablePlaceId));
    }

    @Override
    public CompletableFuture<Void> refreshAssignablePlacePosts(AServer server, String name) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, name);
        return refreshAssignablePlacePosts(assignableRolePlace);
    }

    @Override
    public CompletableFuture<Void> refreshAssignablePlacePosts(AssignableRolePlace place) {
        log.info("Refreshing assignable role place posts for assignable role place {} in server {}.", place.getId(), place.getServer().getId());
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        List<AssignableRolePlacePost> existingMessagePosts = place.getMessagePosts();
        existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));
        AssignableRolePlacePost latestPost = existingMessagePosts.get(place.getMessagePosts().size() - 1);
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        Optional<TextChannel> channelOptional = channelService.getTextChannelInGuild(place.getServer().getId(), latestPost.getUsedChannel().getId());
        if(channelOptional.isPresent()) {
            TextChannel textChannel = channelOptional.get();
            Iterator<MessageEmbed> iterator = messageToSend.getEmbeds().iterator();
            place.getMessagePosts().forEach(post -> {
                log.trace("Refreshing the posts for message post {} in channel {} in assignable role place {} in server {}.", post.getId(), textChannel.getId(), place.getId(), place.getServer().getId());
                CompletableFuture<Message> messageCompletableFuture = channelService.editEmbedMessageInAChannel(iterator.next(), textChannel, post.getId());
                futures.add(messageCompletableFuture);
            });

        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place) {
        List<AssignableRolePlacePost> existingMessagePosts = place.getMessagePosts();
        if(!existingMessagePosts.isEmpty()) {
            MessageToSend renderedMessage = renderAssignablePlacePosts(place);
            log.trace("There are {} current posts known for the assignable role place {}.", existingMessagePosts.size(),  place.getId());
            existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));
            AssignableRolePlacePost firstPost = existingMessagePosts.get(0);
            Long channelId = firstPost.getUsedChannel().getId();
            Optional<TextChannel> channelOptional = channelService.getTextChannelInGuild(place.getServer().getId(), channelId);
            if(channelOptional.isPresent()) {
                log.info("Refreshing text for assignable role place {} in channel {} in post {}.", place.getId(), channelId, firstPost.getId());
                return channelService.editEmbedMessageInAChannel(renderedMessage.getEmbeds().get(0), channelOptional.get(), firstPost.getId()).thenCompose(message -> CompletableFuture.completedFuture(null));
            }
            throw new ChannelNotFoundException(channelId);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void setAssignablePlaceActiveTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if(newValue) {
            this.activateAssignableRolePlace(place);
        } else {
            this.deactivateAssignableRolePlace(place);
        }
    }

    private List<CompletableFuture<Void>> deleteExistingMessagePostsForPlace(AssignableRolePlace assignableRolePlace) {
        List<CompletableFuture<Void>> oldPostDeletionFutures = new ArrayList<>();
        assignableRolePlace.getMessagePosts().forEach(assignableRolePlacePost -> {
            log.info("Deleting existing message post with id {} in channel{} for assignable role place {} in server {}.",
                    assignableRolePlacePost.getId(), assignableRolePlacePost.getUsedChannel().getId(), assignableRolePlace.getId(), assignableRolePlace.getServer().getId());
            oldPostDeletionFutures.add(messageService.deleteMessageInChannelInServer(assignableRolePlace.getServer().getId(), assignableRolePlacePost.getUsedChannel().getId(), assignableRolePlacePost.getId()));
        });
        return oldPostDeletionFutures;
    }

    @Override
    public void deactivateAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceActiveTo(server, name, false);
    }

    @Override
    public void deactivateAssignableRolePlace(AssignableRolePlace place) {
        place.setActive(false);
        log.info("Deactivating assignable role place {} in server {}", place.getId(), place.getServer().getId());
    }

    @Override
    public void activateAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceActiveTo(server, name, true);
    }

    @Override
    public void activateAssignableRolePlace(AssignableRolePlace place) {
        place.setActive(true);
        log.info("Activating assignable role place {} in server {}", place.getId(), place.getServer().getId());
    }

    @Override
    public CompletableFuture<Void> setAssignablePlaceInlineTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if(newValue) {
            return this.inlineAssignableRolePlace(place);
        } else {
            return this.spreadAssignableRolePlace(place);
        }
    }

    @Override
    public CompletableFuture<Void> inlineAssignableRolePlace(AServer server, String name) {
        return setAssignablePlaceInlineTo(server, name, true);
    }

    @Override
    public CompletableFuture<Void> inlineAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place inline {} in server {} to {}", place.getId(), place.getServer().getId(), true);
        place.setInline(true);
        return refreshAssignablePlacePosts(place);
    }

    @Override
    public CompletableFuture<Void> spreadAssignableRolePlace(AServer server, String name) {
        return setAssignablePlaceInlineTo(server, name, false);
    }

    @Override
    public CompletableFuture<Void> spreadAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place inline {} in server {} to {}", place.getId(), place.getServer().getId(), false);
        place.setInline(false);
        return refreshAssignablePlacePosts(place);
    }

    @Override
    public void setAssignablePlaceUniqueTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if(newValue) {
            this.uniqueAssignableRolePlace(place);
        } else {
            this.multipleAssignableRolePlace(place);
        }
    }

    @Override
    public void uniqueAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceInlineTo(server, name, true);
    }

    @Override
    public void uniqueAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place uniqueness {} in server {} to {}", place.getId(), place.getServer().getId(), true);
        place.setUniqueRoles(true);
    }

    @Override
    public void multipleAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceInlineTo(server, name, false);
    }

    @Override
    public void multipleAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place uniqueness {} in server {} to {}", place.getId(), place.getServer().getId(), false);
        place.setUniqueRoles(false);
    }

    @Override
    public void setAssignablePlaceAutoRemoveTo(AServer server, String name, Boolean newValue) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        if(newValue) {
            this.autoRemoveAssignableRolePlace(place);
        } else {
            this.keepReactionsAssignableRolePlace(place);
        }
    }

    @Override
    public void autoRemoveAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceAutoRemoveTo(server, name, true);
    }

    @Override
    public void autoRemoveAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place auto remove {} in server {} to {}", place.getId(), place.getServer().getId(), true);
        place.setAutoRemove(true);
    }

    @Override
    public void keepReactionsAssignableRolePlace(AServer server, String name) {
        setAssignablePlaceAutoRemoveTo(server, name, false);
    }

    @Override
    public void keepReactionsAssignableRolePlace(AssignableRolePlace place) {
        log.info("Setting assignable role place auto remove {} in server {} to {}", place.getId(), place.getServer().getId(), false);
        place.setAutoRemove(false);
    }

    @Override
    public void swapPositions(AServer server, String name, FullEmote firstEmote, FullEmote secondEmote) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        Optional<AssignableRole> firstEmoteOptional = place.getAssignableRoles().stream().filter(role -> emoteService.compareAEmote(role.getEmote(), firstEmote.getFakeEmote())).findFirst();
        Optional<AssignableRole> secondEmoteOptional = place.getAssignableRoles().stream().filter(role -> emoteService.compareAEmote(role.getEmote(), secondEmote.getFakeEmote())).findFirst();
        if(firstEmoteOptional.isPresent() && secondEmoteOptional.isPresent()) {
            AssignableRole firstRole = firstEmoteOptional.get();
            AssignableRole secondRole = secondEmoteOptional.get();
            log.info("Swapping positions of emotes {} and {} in assignable role place {} in server {}: first: {} -> {}, second: {} -> {}",
                    firstRole.getEmote().getId(), secondRole.getEmote().getId(), place.getId(), server.getId(), firstRole.getPosition(), secondRole.getPosition(), secondRole.getPosition(), firstRole.getPosition());
            int firstPosition = firstRole.getPosition();
            firstRole.setPosition(secondRole.getPosition());
            secondRole.setPosition(firstPosition);
        } else {
            if(!firstEmoteOptional.isPresent()) {
                throw new EmoteNotInAssignableRolePlaceException(firstEmote, name);
            } else {
                throw new EmoteNotInAssignableRolePlaceException(secondEmote, name);
            }
        }
    }

    @Override
    public CompletableFuture<Void> testAssignableRolePlace(AServer server, String name, MessageChannel channel) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        log.info("Testing assignable role place {} in channel {} on server {}.", place.getId(), channel.getId(), server.getId());
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, channel);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void showAssignablePlaceConfig(AServer server, String name, MessageChannel channel) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        List<AssignablePostConfigRole> roles = new ArrayList<>();
        Guild guild = botService.getGuildByIdNullable(server.getId());
        log.info("Showing assignable role place config for place {} in channel {} on server {}.", place.getId(), channel.getId(), server.getId());
        List<AssignableRole> assignableRoles = place.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        for (AssignableRole role : assignableRoles) {
            AEmote emoteForRole = role.getEmote();
            Emote jdaEmoteForRole = botService.getEmote(emoteForRole).orElse(null);
            Role jdaRole = guild.getRoleById(role.getRole().getId());
            AssignablePostConfigRole postRole = AssignablePostConfigRole
                    .builder()
                    .description(role.getDescription())
                    .emote(FullEmote.builder().fakeEmote(emoteForRole).emote(jdaEmoteForRole).build())
                    .position(role.getPosition())
                    .awardedRole(jdaRole)
                    .build();
            log.trace("Displaying config for role {} with emote {} in position {}.", role.getId(), emoteForRole.getId(), role.getPosition());
            roles.add(postRole);
        }
        AssignableRolePlaceConfig configModel = AssignableRolePlaceConfig
                .builder()
                .roles(roles)
                .place(place)
                .build();
        channelService.sendEmbedTemplateInChannel(ASSIGNABLE_ROLES_CONFIG_POST_TEMPLATE_KEY, configModel, channel);
    }

    @Override
    public void moveAssignableRolePlace(AServer server, String name, TextChannel newChannel) {
        AChannel channel = channelManagementService.loadChannel(newChannel.getIdLong());
        rolePlaceManagementService.moveAssignableRolePlace(server, name, channel);
    }

    @Override
    public void changeAssignablePlaceDescription(AServer server, String name, String newDescription) {
        rolePlaceManagementService.changeAssignableRolePlaceDescription(server, name, newDescription);
    }

    @Override
    public CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        rolePlaceManagementService.deleteAssignablePlace(place);
        deleteEmotesFromAssignableRolePlace(place);
        List<CompletableFuture<Void>> deleteFutures = deleteExistingMessagePostsForPlace(place);
        return CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> changeText(AServer server, String name, String newText) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Changing text of assignable role place {} in server {}.", place.getId(), server.getId());
        place.setText(newText);
        return refreshTextFromPlace(place);
    }

    @Override
    public CompletableFuture<Void> removeExistingReactionsAndRoles(AssignableRolePlace place, AssignedRoleUser user) {
        Member memberInServer = botService.getMemberInServer(user.getUser());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        log.info("Removing all existing reactions and roles by user {} on assignable role place {} in server {}.", user.getId(), place.getId(), user.getUser().getServerReference().getId());
        user.getRoles().forEach(assignableRole -> {
            futures.add(roleService.removeAssignableRoleFromUser(assignableRole, memberInServer));
            log.trace("Removing role {} from user {} in server {} because of assignable role clearing.", assignableRole.getRole().getId(), memberInServer.getId(), place.getServer().getId());
            AEmote emoteToUseObject = emoteManagementService.loadEmote(assignableRole.getEmote().getId());
            AssignableRolePlacePost assignablePlacePost = assignableRole.getAssignableRolePlacePost();
            log.trace("Removing reaction with emote {} from user {} in server {} because of assignable role clearing.", emoteToUseObject.getId(), user.getUser().getUserReference().getId(), place.getServer().getId());
            futures.add(messageService.removeReactionOfUserFromMessageWithFuture(emoteToUseObject, place.getServer().getId(),
                    assignablePlacePost.getUsedChannel().getId(), assignablePlacePost.getId(), memberInServer));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, Object newValue) {
        Boolean booleanValue = BooleanUtils.toBooleanObject(newValue.toString());
        if(booleanValue == null) {
            throwBooleanParameterKeyException();
        }
        switch (keyToChange) {
            case INLINE:
                return setAssignablePlaceInlineTo(server, name, booleanValue);
            case AUTOREMOVE:
                setAssignablePlaceAutoRemoveTo(server, name, booleanValue);
                return CompletableFuture.completedFuture(null);
            case UNIQUE:
                setAssignablePlaceUniqueTo(server, name, booleanValue);
                return CompletableFuture.completedFuture(null);
            case ACTIVE:
                setAssignablePlaceActiveTo(server, name, booleanValue);
                return CompletableFuture.completedFuture(null);
            default:
                throw new AbstractoTemplatedException("Illegal configuration key was passed", "assignable_role_place_illegal_configuration_key_exception");
        }
    }

    @Override
    public CompletableFuture<Void> showAllAssignableRolePlaces(AServer server, MessageChannel channel) {
        List<AssignableRolePlace> assignableRolePlaces = rolePlaceManagementService.findAllByServer(server);
        AssignablePlaceOverview overViewModel = AssignablePlaceOverview.builder().places(assignableRolePlaces).build();
        log.info("Showing overview over all assignable role places for server {} in channel {}.", server.getId(), channel.getId());
        List<CompletableFuture<Message>> promises = channelService.sendEmbedTemplateInChannel(ASSIGNABLE_ROLE_PLACES_OVERVIEW_TEMPLATE_KEY, overViewModel, channel);
        return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0]));
    }

    private void throwBooleanParameterKeyException() {
        throw new CommandParameterKeyValueWrongTypeException(Arrays.asList("yes", "no", "true", "false", "on", "off"));
    }

    private void deleteEmotesFromAssignableRolePlace(AssignableRolePlace place) {
        log.info("Deleting all emotes associated with assignable role place {} in server {}.", place.getId(), place.getServer().getId());
        place.getAssignableRoles().forEach(role ->
            emoteManagementService.deleteEmote(role.getEmote())
        );
    }

    private List<CompletableFuture<Message>> sendAssignablePostMessages(AssignableRolePlace place, MessageChannel channel) {
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        return channelService.sendMessageToSendToChannel(messageToSend, channel);
    }

    private MessageToSend renderAssignablePlacePosts(AssignableRolePlace place) {
        AssignablePostMessage model = prepareAssignablePostMessageModel(place);
        return templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model);
    }

    private AssignablePostMessage prepareAssignablePostMessageModel(AssignableRolePlace place) {
        List<AssignablePostRole> roles = new ArrayList<>();
        List<AssignableRole> rolesToAdd = place.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        int maxPosition = 0;
        if(!rolesToAdd.isEmpty()) {
            maxPosition = rolesToAdd.get(rolesToAdd.size() - 1).getPosition();
            Iterator<AssignableRole> rolesToAddIterator = rolesToAdd.iterator();
            AssignableRole current = rolesToAddIterator.next();
            AssignablePostRole lastAddedRole = null;
            for (int position = 0; position < maxPosition + 1; position++) {
                boolean legitEntry = current.getPosition().equals(position);
                boolean startOfNewMessage = position > 0 && (position % 21) == 0;
                if(legitEntry) {
                    AEmote emoteForRole = current.getEmote();
                    Emote jdaEmoteForRole = botService.getEmote(emoteForRole).orElse(null);
                    FullEmote fullEmote = FullEmote.builder().emote(jdaEmoteForRole).fakeEmote(emoteForRole).build();
                    AssignablePostRole postRole = AssignablePostRole
                            .builder()
                            .description(current.getDescription())
                            .emote(fullEmote)
                            .position(position)
                            .forceNewMessage(startOfNewMessage)
                            .build();
                    roles.add(postRole);
                    lastAddedRole = postRole;
                    if(rolesToAddIterator.hasNext()) {
                        current = rolesToAddIterator.next();
                    }
                } else if(startOfNewMessage && lastAddedRole != null) {
                    log.trace("Forcing new message for post of assignable role place {}.", place.getId());
                    lastAddedRole.setForceNewMessage(true);
                }
            }
        }
        return AssignablePostMessage
                .builder()
                .roles(roles)
                .place(place)
                .maxPosition(maxPosition)
                .build();
    }

    @Transactional
    public CompletableFuture<Void> createAssignableRolePlacePosts(Long serverId, Long assignablePlaceId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        Optional<TextChannel> channelOptional = botService.getTextChannelFromServerOptional(serverId, assignableRolePlace.getChannel().getId());
        if(channelOptional.isPresent()) {
            MessageChannel channel = channelOptional.get();
            log.info("Sending assignable role place posts for place {} in channel {} in server {}.", assignableRolePlace.getId(), channel.getId(), serverId);
            List<CompletableFuture<Message>> messageFutures = sendAssignablePostMessages(assignableRolePlace, channel);
            return CompletableFuture.allOf(messageFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(aVoid -> self.addEmotes(messageFutures, assignablePlaceId));
        } else {
            log.warn("Channel to create assignable role post in does not exist.");
            throw new AssignableRolePlaceChannelDoesNotExist(assignableRolePlace.getChannel().getId(), assignableRolePlace.getKey());
        }
    }


    @Transactional
    public CompletableFuture<Void> addEmotes(List<CompletableFuture<Message>> assignablePlacePostsMessageFutures, Long assignablePlaceId) {
        Message firstMessage = assignablePlacePostsMessageFutures.get(0).join();
        Long serverId = firstMessage.getGuild().getIdLong();

        AssignableRolePlace innerRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        log.info("Adding emotes to assignable role place {}.", innerRolePlace);
        log.trace("We have {} posts and {} roles.", assignablePlacePostsMessageFutures.size(), innerRolePlace.getAssignableRoles().size());

        List<AssignableRole> roleStream = innerRolePlace.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        List<CompletableFuture<Void>> reactionFutures = new ArrayList<>();
        int usedEmotes = 0;
        for (CompletableFuture<Message> messageCompletableFuture : assignablePlacePostsMessageFutures) {
            Message sentMessage = messageCompletableFuture.join();
            // this uses the actual embed count as a limit, so this relies on fields to be used for description, if this changes, this needs to be changed
            MessageEmbed embed = sentMessage.getEmbeds().get(0);
            List<AssignableRole> firstRoles = roleStream.subList(usedEmotes, usedEmotes + embed.getFields().size());
            usedEmotes += embed.getFields().size();
            log.trace("Adding {} emotes to message {} for place {}. In total {} were added.", embed.getFields().size(), sentMessage.getId(), innerRolePlace.getId(), usedEmotes);
            List<Integer> usedEmoteIds = firstRoles.stream().map(assignableRole -> assignableRole.getEmote().getId()).collect(Collectors.toList());
            CompletableFuture<Void> firstMessageFuture = addingReactionsToAssignableRolePlacePost(sentMessage, serverId, usedEmoteIds);
            reactionFutures.add(firstMessageFuture);
        }
        return CompletableFuture.allOf(reactionFutures.toArray(new CompletableFuture[0])).thenCompose(aVoid -> {
            self.storeCreatedAssignableRolePlacePosts(assignablePlaceId, serverId, assignablePlacePostsMessageFutures);
            return CompletableFuture.completedFuture(null);
        });
    }

    @Transactional
    public void storeCreatedAssignableRolePlacePosts(Long assignablePlaceId, Long serverId, List<CompletableFuture<Message>> futures) {
        AssignableRolePlace updatedPlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        log.info("Storing {} messages for assignable role place {} in server {}.", futures.size(), assignablePlaceId, serverId);
        List<AssignableRole> rolesToAdd = updatedPlace.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        int usedEmotes = 0;
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<Message> messageCompletableFuture = futures.get(i);
            try {
                Message message = messageCompletableFuture.get();
                Message sentMessage = messageCompletableFuture.get();
                // this uses the actual embed count as a limit, so this relies on fields to be used for description, if this changes, this needs to be changed
                MessageEmbed embed = sentMessage.getEmbeds().get(0);
                log.trace("Storing post {} with {} fields.", message.getId(), embed.getFields().size());
                List<AssignableRole> firstRoles = rolesToAdd.subList(usedEmotes, usedEmotes +  embed.getFields().size());
                usedEmotes += embed.getFields().size();
                AssignableRolePlacePost post = AssignableRolePlacePost
                        .builder()
                        .id(message.getIdLong())
                        .usedChannel(updatedPlace.getChannel())
                        .assignablePlace(updatedPlace)
                        .build();
                firstRoles.forEach(assignableRole ->
                    assignableRole.setAssignableRolePlacePost(post)
                );
                updatedPlace.getMessagePosts().add(post);
            } catch (Exception e) {
                log.error("Failed to get future.", e);
            }
        }
    }

    @Transactional
    public CompletableFuture<Void> addingReactionsToAssignableRolePlacePost(Message message, Long server, List<Integer> emotesToAdd) {
        // TODO might need to guarantee the order
        List<CompletableFuture<Void>> futures =  new ArrayList<>();
        log.info("Adding {} emotes to assignable role place post {} in server {}.", emotesToAdd.size(), message.getId(), server);
        emotesToAdd.forEach(emotesToUse -> {
            AEmote emoteToUseObject = emoteManagementService.loadEmote(emotesToUse);
            futures.add(messageService.addReactionToMessageWithFuture(emoteToUseObject, server, message));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
