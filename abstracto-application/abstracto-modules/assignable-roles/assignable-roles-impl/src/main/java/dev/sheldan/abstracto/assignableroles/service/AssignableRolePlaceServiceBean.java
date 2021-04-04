package dev.sheldan.abstracto.assignableroles.service;

import dev.sheldan.abstracto.assignableroles.config.AssignableRolePlaceParameterKey;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceAlreadyExistsException;
import dev.sheldan.abstracto.assignableroles.exception.AssignableRolePlaceChannelDoesNotExistException;
import dev.sheldan.abstracto.assignableroles.exception.EmoteNotInAssignableRolePlaceException;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRole;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace;
import dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlacePost;
import dev.sheldan.abstracto.assignableroles.model.database.AssignedRoleUser;
import dev.sheldan.abstracto.assignableroles.model.template.*;
import dev.sheldan.abstracto.assignableroles.service.management.*;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.exception.CommandParameterKeyValueWrongTypeException;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.exception.EmoteNotUsableException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
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
    private ReactionService reactionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private GuildService guildService;

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

    @Autowired
    private AssignableRolePlacePostManagementServiceBean assignableRolePlacePostManagementServiceBean;

    @Override
    public void createAssignableRolePlace(String name, AChannel channel, String text) {
        if(rolePlaceManagementService.doesPlaceExist(channel.getServer(), name)) {
            throw new AssignableRolePlaceAlreadyExistsException(name);
        }
        rolePlaceManagementService.createPlace(name, channel, text);
    }

    @Override
    public boolean hasAssignableRolePlaceEmote(AServer server, String placeName, AEmote emote) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        return hasAssignableRolePlaceEmote(assignableRolePlace, emote);
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
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, placeName);
        log.info("Setting emote to position {} in assignable role place {} in server {}.",
                position, assignableRolePlace.getId(), assignableRolePlace.getServer().getId());
        Optional<AssignableRole> emoteOptional = assignableRolePlace.getAssignableRoles().stream().filter(role -> emoteService.compareAEmote(emote.getFakeEmote(), role.getEmote())).findFirst();
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
            log.debug("Using custom emote {} to create assignable role {} for  assignable role place {} in server {}.",
                    fakeEmote.getEmote().getId(), roleId, placeId, serverId);
            emoteUsable = emoteService.isEmoteUsableByBot(fakeEmote.getEmote()) && fakeEmote.getEmote().isAvailable();
        }
        if(emoteUsable) {
            List<AssignableRolePlacePost> existingMessagePosts = assignableRolePlace.getMessagePosts();
            existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));

            if(!assignableRolePlace.getMessagePosts().isEmpty()){
                log.debug("There are already message posts on for the assignable role place {}.", assignableRolePlace.getId());
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
                MessageToSend messageToSend = templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model, server.getId());
                // add it to the last currently existing post
                Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(server.getId(), latestPost.getUsedChannel().getId());
                if(channelOptional.isPresent()) {
                    TextChannel textChannel = channelOptional.get();
                    if(latestPost.getAssignableRoles().size() < 20) {
                        log.debug("Adding reaction to existing post {} in channel {} in server {} for assignable role place {}.",
                                latestPost.getId(), assignableRolePlace.getChannel().getId(), serverId, placeId);
                        return addReactionToExistingAssignableRolePlacePost(fakeEmote, description, roleId, latestPost, messageToSend, textChannel);
                    } else {
                        log.debug("Adding new post to assignable role place {} in channel {} in server {}.",
                                placeId, assignableRolePlace.getChannel().getId(), server.getId());
                        return addNewMessageToAssignableRolePlace(placeId, fakeEmote, description, roleId, messageToSend, textChannel);
                    }
                } else {
                    throw new ChannelNotInGuildException(latestPost.getUsedChannel().getId());
                }
            } else {
                log.debug("Added emote to assignable place {} in server {}, but no message post yet.", placeId, serverId);
                self.addAssignableRoleInstanceWithoutPost(placeId, roleId, fakeEmote, description, serverId);
            }
        } else {
            throw new EmoteNotUsableException(fakeEmote.getEmote());
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Adds the reaction from {@link FullEmote} to the last {@link AssignableRolePlacePost post} of its {@link AssignableRolePlace place}.
     * It will also update the message representing the last {@link AssignableRolePlacePost post} with the given {@link MessageToSend messageToSend}
     * in the given {@link TextChannel channel}. Afterwards the added {@link AssignableRole assignableRole} will be persisted in the database
     * @param fakeEmote The *fake* {@link FullEmote emote} which represents the reaction which should be added and identifies the {@link AssignableRole role}
     * @param description The description the {@link AssignableRole role} should have
     * @param roleId The ID of the {@link ARole role} which should be given when a reaction is added by a {@link Member member}
     * @param latestPost The last {@link AssignableRolePlacePost post} in the list of posts
     * @param messageToSend The {@link MessageToSend messageToSend} which should be used to update the latest {@link AssignableRolePlacePost post} with. This
     *                      will be done in place.
     * @param textChannel The {@link TextChannel channel} in which the {@link AssignableRolePlacePost post} is
     * @return A {@link CompletableFuture future} which will complete when the reaction was added, the message was edited and the database has been updated
     */
    private CompletableFuture<Void> addReactionToExistingAssignableRolePlacePost(FullEmote fakeEmote, String description, Long roleId, AssignableRolePlacePost latestPost, MessageToSend messageToSend, TextChannel textChannel) {
        // TODO maybe refactor to use the same message object, so we dont need to retrieve it twice and do in parallel
        Long serverId = latestPost.getAssignablePlace().getServer().getId();
        Long placeId = latestPost.getAssignablePlace().getId();
        Long latestPostId = latestPost.getId();
        int messagePostSize = latestPost.getAssignablePlace().getMessagePosts().size();
        return channelService.retrieveMessageInChannel(textChannel, latestPostId)
                .thenCompose(message -> {
                    log.debug("Adding reaction to message {} in server {} for assignable role place {}.", message.getId(), serverId, placeId);
                    return reactionService.addReactionToMessageAsync(fakeEmote.getFakeEmote(), serverId, message);
                }).thenCompose(aVoid -> {
                    log.debug("Editing embed for assignable role place post {} in assignable role place {} in server {}.", latestPostId, placeId, serverId);
                    MessageEmbed embedToUse = messageToSend.getEmbeds().get(messagePostSize - 1);
                    return channelService.editEmbedMessageInAChannel(embedToUse, textChannel, latestPostId);
                }).thenAccept(message ->
                    self.addAssignableRoleInstanceWithPost(message.getIdLong(), placeId, roleId, description, fakeEmote, serverId)
                );
    }

    /**
     * Sends a new {@link Message message}, defined by the given {@link MessageToSend messageToSend}, to the channel the {@link AssignableRolePlace place}
     * is configured for, adds a reaction and stores the update in the database.
     * @param placeId The ID of the {@link AssignableRolePlace place} to add a post to
     * @param fakeEmote The *fake* {@link FullEmote emote} which represents the reaction which should be added and identifies the {@link AssignableRole role}
     * @param description The description the {@link AssignableRole role} should have
     * @param roleId The ID of the {@link ARole role} which should be given when a reaction is added by a {@link Member member}
     * @param messageToSend The {@link MessageToSend messageToSend} which should be used to create the new {@link AssignableRolePlacePost post}
     * @param textChannel The The {@link TextChannel channel} in which the {@link AssignableRolePlacePost post} should be in
     * @return A {@link CompletableFuture future} which competes when the {@link MessageToSend messageToSend} has been sent, a {@link MessageReaction reaction}
     * has been added and the changes have been persisted in the database.
     */
    private CompletableFuture<Void> addNewMessageToAssignableRolePlace(Long placeId, FullEmote fakeEmote, String description, Long roleId, MessageToSend messageToSend, TextChannel textChannel) {
        MessageEmbed embedToUse = messageToSend.getEmbeds().get(messageToSend.getEmbeds().size() - 1);
        Long serverId = textChannel.getGuild().getIdLong();
        return channelService.sendEmbedToChannel(embedToUse, textChannel)
                .thenCompose(message -> {
                    log.debug("Adding reaction for role {} to newly created message {} for assignable role place {} in server {}.", roleId, message.getId(), placeId, serverId);
                    return reactionService.addReactionToMessageAsync(fakeEmote.getFakeEmote(), serverId, message)
                                .thenAccept(aVoid ->
                                    self.addNewlyCreatedAssignablePlacePost(placeId, description, roleId, message, fakeEmote)
                                );
                });
    }

    /**
     * Persisted the newly created {@link AssignableRolePlacePost post} and adds a {@link AssignableRole assignableRolePlace} to this
     * @param placeId The ID of the {@link AssignableRolePlace place} to add a post to
     * @param description The description the {@link AssignableRole role} should have
     * @param roleId The ID of the {@link ARole role} which should be given when a reaction is added by a {@link Member member}
     * @param message The {@link Message message} which was created when sending the {@link MessageToSend messageToSend} representing this
     *                {@link AssignableRolePlacePost post}
     * @param fakeEmote The *fake* {@link FullEmote emote} which is used to identify the {@link AssignableRole role}
     */
    @Transactional
    public void addNewlyCreatedAssignablePlacePost(Long placeId, String description, Long roleId, Message message, FullEmote fakeEmote) {
        Long serverId = message.getGuild().getIdLong();
        log.info("Storing newly created assignable role place post {} for place {} in server {}.", message.getId(), placeId, serverId);
        ARole role = roleManagementService.findRole(roleId);
        AssignableRolePlace loadedPlace = rolePlaceManagementService.findByPlaceId(placeId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.debug("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);

        AssignableRolePlacePost newPost = assignableRolePlacePostManagementServiceBean.createAssignableRolePlacePost(loadedPlace, message.getIdLong());
        assignableRoleManagementServiceBean.addRoleToPlace(loadedPlace, emote, role, description, newPost);
    }

    /**
     * Actually creates the {@link AEmote emote} defined by the given {@link FullEmote emote} and adds a new instance
     * of an {@link AssignableRole assignableRole} to the {@link AssignableRolePlace place}. This is used in case
     * there already exists an {@link AssignableRolePlacePost post}
     * @param messageId The ID of the {@link Message message} which identifies the {@link AssignableRolePlacePost post}
     *                  at which this {@link AssignableRole role} is available via a {@link MessageReaction reaction}
     * @param placeId The ID of the {@link AssignableRolePlace place} to add the {@link AssignableRole assignableRole} to
     * @param roleId The ID of the {@link ARole role} which should be given when a reaction is added by a {@link Member member}
     * @param description The description the {@link AssignableRole role} should have
     * @param fakeEmote The *fake* {@link FullEmote emote} which is used to identify the {@link AssignableRole role}
     * @param serverId The ID of the {@link AServer server} this should be persisted for
     */
    @Transactional
    public void addAssignableRoleInstanceWithPost(Long messageId, Long placeId, Long roleId, String description, FullEmote fakeEmote, Long serverId) {
        log.info("Storing newly created assignable role {} to post {} to assignable role place {} in server {}.", roleId, messageId, placeId, serverId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.debug("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);
        assignableRoleManagementServiceBean.addRoleToPlace(placeId, emote.getId(), roleId, description, messageId);
    }

    /**
     * Actually creates the {@link AEmote emote} defined by the given {@link FullEmote emote} and adds a new instance
     * of an {@link AssignableRole assignableRole} to the {@link AssignableRolePlace place}. This is used in case
     * there doesnt exists an {@link AssignableRolePlacePost post}
     * @param placeId The ID of the {@link AssignableRolePlace place} to add the {@link AssignableRole assignableRole} to
     * @param roleId The ID of the {@link ARole role} which should be given when a reaction is added by a {@link Member member}
     * @param description The description the {@link AssignableRole role} should have
     * @param fakeEmote The *fake* {@link FullEmote emote} which is used to identify the {@link AssignableRole role}
     * @param serverId The ID of the {@link AServer server} this should be persisted for
     */
    @Transactional
    public void addAssignableRoleInstanceWithoutPost(Long placeId, Long roleId, FullEmote fakeEmote, String description, Long serverId) {
        log.info("Storing newly created assignable role {} without post to assignable role place {} in server {}.", roleId, placeId, serverId);
        AEmote emote = emoteManagementService.createEmote(null, fakeEmote.getFakeEmote(), serverId, false);
        emote.setChangeable(false);
        log.debug("Setting emote {} to not changeable, because it is part of an assignable role place {} in server {}.", emote.getId(), placeId, serverId);
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

    /**
     * This method deletes the {@link AssignableRole assignableRole} from the given {@link AssignableRolePlace place}
     * in the database
     * @param placeId The ID of the {@link AssignableRolePlace place} to remove the {@link AssignableRole assignableRole} from
     * @param assignableRoleId The ID of the {@link AssignableRole assignableRole} to remove from the {@link AssignableRolePlace place}
     */
    @Transactional
    public void deleteAssignableRoleFromPlace(Long placeId, Long assignableRoleId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(placeId);
        log.info("Deleting the entry for assignable role {} in assignable role place {}.", assignableRoleId, placeId);
        Optional<AssignableRole> roleToRemoveOptional = assignableRolePlace.getAssignableRoles().stream().filter(role -> role.getId().equals(assignableRoleId)).findAny();
        roleToRemoveOptional.ifPresent(assignableRole -> {
            assignableRolePlace.getAssignableRoles().remove(assignableRole);
            assignableRole.getAssignedUsers().forEach(assignedRoleUser -> assignedRoleUser.getRoles().remove(assignableRole));
            assignableRole.getAssignedUsers().clear();
            assignableRole.setAssignablePlace(null);
        });
    }

    /**
     * The removes the {@link AssignableRole assignableRole} from the given {@link AssignableRolePlace place}. If
     * there already is a {@link AssignableRolePlacePost post}, this removes the field from the post
     * and also clears all {@link MessageReaction reactions}
     * @param assignableRole The {@link AssignableRole role} to remove
     * @param assignableRolePlace The {@link AssignableRolePlace place} to remove the role from
     * @return A {@link CompletableFuture future} which completes when the message was updated and the reaction was removed, if
     * no post was present it completes immediately.
     */
    private CompletableFuture<Void> removeRoleFromAssignablePlace(AssignableRole assignableRole, AssignableRolePlace assignableRolePlace) {
        AssignableRolePlacePost post = assignableRole.getAssignableRolePlacePost();
        if(post != null) {
            AServer server = assignableRolePlace.getServer();
            TextChannel textChannel = channelService.getTextChannelFromServer(server.getId(), post.getUsedChannel().getId());
            List<AssignableRole> assignableRoles = assignableRolePlace.getAssignableRoles();
            assignableRoles.sort(Comparator.comparing(AssignableRole::getPosition));
            Long messageId = post.getId();
            log.debug("Removing field describing assignable role {} in assignable role place {} from post {}.", assignableRole.getId(), assignableRolePlace.getId(), messageId);
            CompletableFuture<Message> fieldEditing = channelService.removeFieldFromMessage(textChannel, messageId, assignableRoles.indexOf(assignableRole));
            log.debug("Clearing reaction for emote {} on assignable role post {} in assignable role place {}.", assignableRole.getEmote().getId(), messageId, assignableRolePlace.getId());
            CompletableFuture<Void> reactionRemoval  = reactionService.clearReactionFromMessageWithFuture(assignableRole.getEmote(), assignableRolePlace.getServer().getId(), assignableRole.getAssignableRolePlacePost().getUsedChannel().getId(), assignableRole.getAssignableRolePlacePost().getId());
            return CompletableFuture.allOf(fieldEditing, reactionRemoval);
        } else {
            // this case comes from the situation in which, the emote was deleted and he initial post setup failed
            log.warn("Reaction {} to remove does not have a post attached. The post needs to be setup again, it is most likely not functioning currently anyway.", assignableRole.getEmote().getEmoteId());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> setupAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Setting up assignable role place {} in server {} towards channel {}.", assignableRolePlace.getId(), server.getId(), assignableRolePlace.getChannel().getId());
        List<CompletableFuture<Void>> oldPostDeletionFutures = deleteExistingMessagePostsForPlace(assignableRolePlace);
        assignableRolePlace.getMessagePosts().forEach(assignableRolePlacePost -> assignableRolePlacePost.setAssignablePlace(null));
        assignableRolePlace.getMessagePosts().clear();
        assignableRolePlace.getAssignableRoles().forEach(assignableRole ->
            assignableRole.setAssignableRolePlacePost(null)
        );
        Long serverId = server.getId();
        Long assignablePlaceId = assignableRolePlace.getId();
        CompletableFuture<Void> messageFuture = FutureUtils.toSingleFutureGeneric(oldPostDeletionFutures);
        return messageFuture.whenComplete((unused, throwable) -> {
            if(throwable != null) {
                log.warn("Not able to delete old messages of assignable role place {} in server {}.", assignablePlaceId, serverId);
            }
            self.createAssignableRolePlacePosts(serverId, assignablePlaceId);
        });
    }

    @Override
    public CompletableFuture<Void> refreshAssignablePlacePosts(AServer server, String name) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByServerAndKey(server, name);
        return refreshAssignablePlacePosts(assignableRolePlace);
    }

    @Override
    public CompletableFuture<Void> refreshAssignablePlacePosts(AssignableRolePlace place) {
        if(place.getMessagePosts().isEmpty()) {
            log.info("Trying to refresh an assignable place {} in server {} without any posts.", place.getId(), place.getServer().getId());
            return CompletableFuture.completedFuture(null);
        }
        log.info("Refreshing assignable role place posts for assignable role place {} in server {}.", place.getId(), place.getServer().getId());
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        List<AssignableRolePlacePost> existingMessagePosts = place.getMessagePosts();
        existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));
        AssignableRolePlacePost latestPost = existingMessagePosts.get(place.getMessagePosts().size() - 1);
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(place.getServer().getId(), latestPost.getUsedChannel().getId());
        if(channelOptional.isPresent()) {
            TextChannel textChannel = channelOptional.get();
            Iterator<MessageEmbed> iterator = messageToSend.getEmbeds().iterator();
            place.getMessagePosts().forEach(post -> {
                log.debug("Refreshing the posts for message post {} in channel {} in assignable role place {} in server {}.", post.getId(), textChannel.getId(), place.getId(), place.getServer().getId());
                CompletableFuture<Message> messageCompletableFuture = channelService.editEmbedMessageInAChannel(iterator.next(), textChannel, post.getId());
                futures.add(messageCompletableFuture);
            });
        } else {
            throw new ChannelNotInGuildException(latestPost.getUsedChannel().getId());
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> refreshTextFromPlace(AssignableRolePlace place) {
        List<AssignableRolePlacePost> existingMessagePosts = place.getMessagePosts();
        if(!existingMessagePosts.isEmpty()) {
            MessageToSend renderedMessage = renderAssignablePlacePosts(place);
            log.debug("There are {} current posts known for the assignable role place {}.", existingMessagePosts.size(),  place.getId());
            existingMessagePosts.sort(Comparator.comparingLong(AssignableRolePlacePost::getId));
            AssignableRolePlacePost firstPost = existingMessagePosts.get(0);
            Long channelId = firstPost.getUsedChannel().getId();
            Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(place.getServer().getId(), channelId);
            if(channelOptional.isPresent()) {
                log.info("Refreshing text for assignable role place {} in channel {} in post {}.", place.getId(), channelId, firstPost.getId());
                return channelService.editEmbedMessageInAChannel(renderedMessage.getEmbeds().get(0), channelOptional.get(), firstPost.getId()).thenCompose(message -> CompletableFuture.completedFuture(null));
            } else {
                throw new ChannelNotInGuildException(channelId);
            }
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

    /**
     * Delete all {@link Message messages} which are stored as {@link AssignableRolePlacePost posts} from the given
     * {@link AssignableRolePlace place}
     * @param assignableRolePlace The {@link AssignableRolePlace place} to delete the posts of
     * @return A list of {@link CompletableFuture futures} each representing one deleted {@link Message message}
     */
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
    public CompletableFuture<Void> testAssignableRolePlace(AServer server, String name, TextChannel channel) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        log.info("Testing assignable role place {} in channel {} on server {}.", place.getId(), channel.getId(), server.getId());
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(messageToSend, channel);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> showAssignablePlaceConfig(AServer server, String name, TextChannel channel) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        List<AssignableRolePlaceConfigRole> roles = new ArrayList<>();
        Guild guild = guildService.getGuildById(server.getId());
        log.info("Showing assignable role place config for place {} in channel {} on server {}.", place.getId(), channel.getId(), server.getId());
        List<AssignableRole> assignableRoles = place.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        for (AssignableRole role : assignableRoles) {
            AEmote emoteForRole = role.getEmote();
            Emote jdaEmoteForRole = emoteService.getEmote(emoteForRole).orElse(null);
            Role jdaRole = guild.getRoleById(role.getRole().getId());
            AssignableRolePlaceConfigRole postRole = AssignableRolePlaceConfigRole
                    .builder()
                    .description(role.getDescription())
                    .emote(FullEmote.builder().fakeEmote(emoteForRole).emote(jdaEmoteForRole).build())
                    .position(role.getPosition())
                    .awardedRole(jdaRole)
                    .build();
            log.debug("Displaying config for role {} with emote {} in position {}.", role.getId(), emoteForRole.getId(), role.getPosition());
            roles.add(postRole);
        }
        AssignableRolePlaceConfig configModel = AssignableRolePlaceConfig
                .builder()
                .roles(roles)
                .place(place)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(ASSIGNABLE_ROLES_CONFIG_POST_TEMPLATE_KEY, configModel, channel));
    }

    @Override
    public void moveAssignableRolePlace(String name, TextChannel newChannel) {
        AChannel channel = channelManagementService.loadChannel(newChannel.getIdLong());
        rolePlaceManagementService.moveAssignableRolePlace(name, channel);
    }

    @Override
    public void changeText(AServer server, String name, String newText) {
        rolePlaceManagementService.changeAssignableRolePlaceDescription(server, name, newText);
    }

    @Override
    public CompletableFuture<Void> deleteAssignableRolePlace(AServer server, String name) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);

        Long placeId = place.getId();
        List<CompletableFuture<Void>> deleteFutures = deleteExistingMessagePostsForPlace(place);
        return CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]))
                .thenAccept(unused -> self.deleteAssignableRolePlaceInDatabase(placeId));
    }

    /**
     * Deletes the {@link AssignableRolePlace place} in the database and all the
     * {@link AEmote emotes} which were used to identify the {@link AssignableRole roles}
     * @param placeId The ID of the {@link AssignableRolePlace place} to delete
     */
    @Transactional
    public void deleteAssignableRolePlaceInDatabase(Long placeId) {
        AssignableRolePlace place = rolePlaceManagementService.findByPlaceId(placeId);
        rolePlaceManagementService.deleteAssignablePlace(place);
        deleteEmotesFromAssignableRolePlace(place);
    }

    @Override
    public CompletableFuture<Void> changeTextAsync(AServer server, String name, String newText) {
        AssignableRolePlace place = rolePlaceManagementService.findByServerAndKey(server, name);
        log.info("Changing text of assignable role place {} in server {}.", place.getId(), server.getId());
        place.setText(newText);
        return refreshTextFromPlace(place);
    }

    @Override
    public CompletableFuture<Void> removeExistingReactionsAndRoles(AssignableRolePlace place, AssignedRoleUser user) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        log.info("Removing all existing reactions and roles by user {} on assignable role place {} in server {}.", user.getId(), place.getId(), user.getUser().getServerReference().getId());
        user.getRoles().forEach(assignableRole -> {
            futures.add(roleService.removeAssignableRoleFromUser(assignableRole, user.getUser()));
            log.debug("Removing role {} from user {} in server {} because of assignable role clearing.", assignableRole.getRole().getId(), user.getUser().getUserReference().getId(), place.getServer().getId());
            AEmote emoteToUseObject = emoteManagementService.loadEmote(assignableRole.getEmote().getId());
            AssignableRolePlacePost assignablePlacePost = assignableRole.getAssignableRolePlacePost();
            log.debug("Removing reaction with emote {} from user {} in server {} because of assignable role clearing.", emoteToUseObject.getId(), user.getUser().getUserReference().getId(), place.getServer().getId());
            futures.add(reactionService.removeReactionOfUserFromMessageWithFuture(emoteToUseObject, place.getServer().getId(),
                    assignablePlacePost.getUsedChannel().getId(), assignablePlacePost.getId(), user.getUser().getUserReference().getId()));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> changeConfiguration(AServer server, String name, AssignableRolePlaceParameterKey keyToChange, Object newValue) {
        Boolean booleanValue = BooleanUtils.toBooleanObject(newValue.toString());
        if(booleanValue == null) {
            throw new CommandParameterKeyValueWrongTypeException(Arrays.asList("yes", "no", "true", "false", "on", "off"));
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
    public CompletableFuture<Void> showAllAssignableRolePlaces(AServer server, TextChannel channel) {
        List<AssignableRolePlace> assignableRolePlaces = rolePlaceManagementService.findAllByServer(server);
        AssignablePlaceOverview overViewModel = AssignablePlaceOverview.builder().places(assignableRolePlaces).build();
        log.info("Showing overview over all assignable role places for server {} in channel {}.", server.getId(), channel.getId());
        List<CompletableFuture<Message>> promises = channelService.sendEmbedTemplateInTextChannelList(ASSIGNABLE_ROLE_PLACES_OVERVIEW_TEMPLATE_KEY, overViewModel, channel);
        return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0]));
    }

    /**
     * Deletes the {@link AEmote emotes} for each {@link AssignableRole assignableRole} from a {@link AssignableRolePlace place}
     * @param place The {@link AssignableRolePlace place} to delete the {@link AEmote emotes} of
     */
    private void deleteEmotesFromAssignableRolePlace(AssignableRolePlace place) {
        log.info("Deleting all emotes associated with assignable role place {} in server {}.", place.getId(), place.getServer().getId());
        place.getAssignableRoles().forEach(role ->
            emoteManagementService.deleteEmote(role.getEmote())
        );
    }

    /**
     * Renders the {@link AssignableRolePlace place} to a {@link MessageToSend messageToSend}, and
     * sends it in the given {@link TextChannel channel}
     * @param place The {@link AssignableRolePlace place} to render
     * @param channel The {@link TextChannel channel} in which the {@link MessageToSend messageToSend} should be sent to
     * @return A list of {@link CompletableFuture futures} which represent each message which was sent
     */
    private List<CompletableFuture<Message>> sendAssignablePostMessages(AssignableRolePlace place, TextChannel channel) {
        MessageToSend messageToSend = renderAssignablePlacePosts(place);
        return channelService.sendMessageToSendToChannel(messageToSend, channel);
    }

    /**
     * Renders the {@link AssignableRolePlace place} with the appropriate template and returns a {@link MessageToSend messageToSend}
     * @param place The {@link AssignableRolePlace place} to render
     * @return A {@link MessageToSend messageToSend} which can be sent, containing the individual posts
     */
    private MessageToSend renderAssignablePlacePosts(AssignableRolePlace place) {
        AssignablePostMessage model = prepareAssignablePostMessageModel(place);
        return templateService.renderEmbedTemplate(ASSIGNABLE_ROLES_POST_TEMPLATE_KEY, model, place.getServer().getId());
    }

    /**
     * Converts individual parts of the given {@link AssignableRolePlace place} into a model which is usable
     * in the template responsible to render the {@link AssignableRolePlace place}. This requires to load
     * the {@link Emote emotes} and ordering according to their position
     * @param place The {@link AssignableRolePlace place} which should be made available for rendering
     * @return The {@link AssignablePostMessage model} which holds the necessary values to render the template
     */
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
                // TODO is this still required?
                boolean startOfNewMessage = position > 0 && (position % 21) == 0;
                if(legitEntry) {
                    AEmote emoteForRole = current.getEmote();
                    Emote jdaEmoteForRole = emoteService.getEmote(emoteForRole).orElse(null);
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
                    log.debug("Forcing new message for post of assignable role place {}.", place.getId());
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

    /**
     * Creates the {@link Message messages} for the posts required for the {@link AssignableRolePlace place} and
     * adds the reactions afterwards.
     * @param serverId The ID of the {@link AServer server} in which the {@link AssignableRolePlace place} posts should
     *                 be created in
     * @param assignablePlaceId The ID of the {@link AssignableRolePlace place} which should have its {@link Message messages} posted
     * @return A {@link CompletableFuture future} which completes when the {@link Message messages} were posted and the {@link MessageReaction reactions}
     * added
     */
    @Transactional
    public CompletableFuture<Void> createAssignableRolePlacePosts(Long serverId, Long assignablePlaceId) {
        AssignableRolePlace assignableRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        Optional<TextChannel> channelOptional = channelService.getTextChannelFromServerOptional(serverId, assignableRolePlace.getChannel().getId());
        if(channelOptional.isPresent()) {
            TextChannel channel = channelOptional.get();
            log.info("Sending assignable role place posts for place {} in channel {} in server {}.", assignableRolePlace.getId(), channel.getId(), serverId);
            List<CompletableFuture<Message>> messageFutures = sendAssignablePostMessages(assignableRolePlace, channel);
            return CompletableFuture.allOf(messageFutures.toArray(new CompletableFuture[0]))
                    .thenCompose(aVoid -> self.addEmotes(messageFutures, assignablePlaceId));
        } else {
            log.warn("Channel to create assignable role post in does not exist.");
            throw new AssignableRolePlaceChannelDoesNotExistException(assignableRolePlace.getChannel().getId(), assignableRolePlace.getKey());
        }
    }

    /**
     * Adds the appropriate {@link MessageReaction reactions} defined by the {@link AssignableRole assignableRoles} of the
     * {@link AssignableRolePlace place} to the {@link Message messages} which are provided via the {@link CompletableFuture futures}.
     * The mapping of reaction to assignable role is done via the index of the fields. Depending on the amount of fields
     * which were created in the {@link Message messages}. Afterwards it will store the created
     * {@link AssignableRolePlacePost posts}
     * @param assignablePlacePostsMessageFutures A list of {@link CompletableFuture futures} which represent the individual
     *                                           {@link Message messages} which were posted when setting up
     *                                           the {@link AssignableRolePlace place}
     * @param assignablePlaceId The ID of the {@link AssignableRolePlace place} which was setup
     * @return A {@link CompletableFuture future} which completes when all reactions were added and the database
     * was updated.
     */
    @Transactional
    public CompletableFuture<Void> addEmotes(List<CompletableFuture<Message>> assignablePlacePostsMessageFutures, Long assignablePlaceId) {
        AssignableRolePlace innerRolePlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        log.info("Adding emotes to assignable role place {}.", innerRolePlace);
        log.debug("We have {} posts and {} roles.", assignablePlacePostsMessageFutures.size(), innerRolePlace.getAssignableRoles().size());

        List<AssignableRole> roleStream = innerRolePlace.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        List<CompletableFuture<Void>> reactionFutures = new ArrayList<>();
        int usedEmotes = 0;
        for (CompletableFuture<Message> messageCompletableFuture : assignablePlacePostsMessageFutures) {
            Message sentMessage = messageCompletableFuture.join();
            // this uses the actual embed count as a limit, so this relies on fields to be used for description, if this changes, this needs to be changed
            MessageEmbed embed = sentMessage.getEmbeds().get(0);
            List<AssignableRole> firstRoles = roleStream.subList(usedEmotes, usedEmotes + embed.getFields().size());
            usedEmotes += embed.getFields().size();
            log.debug("Adding {} emotes to message {} for place {}. In total {} were added.", embed.getFields().size(), sentMessage.getId(), innerRolePlace.getId(), usedEmotes);
            List<Integer> usedEmoteIds = firstRoles.stream().map(assignableRole -> assignableRole.getEmote().getId()).collect(Collectors.toList());
            CompletableFuture<Void> firstMessageFuture = addingReactionsToAssignableRolePlacePost(sentMessage, usedEmoteIds);
            reactionFutures.add(firstMessageFuture);
        }
        return CompletableFuture.allOf(reactionFutures.toArray(new CompletableFuture[0])).thenCompose(aVoid -> {
            try {
                self.storeCreatedAssignableRolePlacePosts(assignablePlaceId, assignablePlacePostsMessageFutures);
            } catch (Exception e) {
                log.error("Failed to persist assignable role place posts. ", e);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Creates the {@link AssignableRolePlacePost posts} for the created {@link Message messages} and stores them in the
     * database. This also uses the fields in the {@link MessageEmbed embed} in order to determine which
     * {@link AssignableRole role} is in which {@link AssignableRolePlacePost post}
     * @param assignablePlaceId The ID of the {@link AssignableRolePlace place} to which the {@link AssignableRolePlacePost posts}
     *                           should be added to
     * @param futures A list of {@link CompletableFuture futures} which holds the {@link Message messages} which were created
     *                when setting up the {@link AssignableRolePlace place}
     */
    @Transactional
    public void storeCreatedAssignableRolePlacePosts(Long assignablePlaceId, List<CompletableFuture<Message>> futures) {
        AssignableRolePlace updatedPlace = rolePlaceManagementService.findByPlaceId(assignablePlaceId);
        log.info("Storing {} messages for assignable role place {} in server {}.", futures.size(), assignablePlaceId, updatedPlace.getServer().getId());
        List<AssignableRole> rolesToAdd = updatedPlace.getAssignableRoles().stream().sorted(Comparator.comparingInt(AssignableRole::getPosition)).collect(Collectors.toList());
        int usedEmotes = 0;
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<Message> messageCompletableFuture = futures.get(i);
            Message message = messageCompletableFuture.join();
            // this uses the actual embed count as a limit, so this relies on fields to be used for description, if this changes, this needs to be changed
            MessageEmbed embed = message.getEmbeds().get(0);
            log.debug("Storing post {} with {} fields.", message.getId(), embed.getFields().size());
            List<AssignableRole> firstRoles = rolesToAdd.subList(usedEmotes, usedEmotes +  embed.getFields().size());
            usedEmotes += embed.getFields().size();
            AssignableRolePlacePost post = assignableRolePlacePostManagementServiceBean.createAssignableRolePlacePost(updatedPlace, message.getIdLong());
            firstRoles.forEach(assignableRole ->
                assignableRole.setAssignableRolePlacePost(post)
            );
        }
    }

    /**
     * Adds the given {@link AEmote emotes} identified by the ID to the given Message
     * @param message The {@link Message message} on which the {@link MessageReaction reactions} should be added on
     * @param emotesToAdd A list of {@link Integer integers} which contains the ID of the {@link AEmote emotes} which should be added
     * @return A {@link CompletableFuture future} which complets when all reactions have been added
     */
    @Transactional
    public CompletableFuture<Void> addingReactionsToAssignableRolePlacePost(Message message, List<Integer> emotesToAdd) {
        // TODO might need to guarantee the order
        List<CompletableFuture<Void>> futures =  new ArrayList<>();
        Long serverId = message.getGuild().getIdLong();
        log.info("Adding {} emotes to assignable role place post {} in server {}.", emotesToAdd.size(), message.getId(), serverId);
        emotesToAdd.forEach(emotesToUse -> {
            AEmote emoteToUseObject = emoteManagementService.loadEmote(emotesToUse);
            futures.add(reactionService.addReactionToMessageAsync(emoteToUseObject, serverId, message));
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
