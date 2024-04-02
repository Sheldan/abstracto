package dev.sheldan.abstracto.core.service.paginator;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.templating.model.EmbedConfiguration;
import dev.sheldan.abstracto.core.templating.model.MessageConfiguration;
import dev.sheldan.abstracto.core.templating.model.EmbedFooter;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.templating.service.TemplateServiceBean;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class PaginatorServiceBean implements PaginatorService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Gson gson;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private PaginatorServiceBean self;

    @Autowired
    private TemplateServiceBean templateServiceBean;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private InteractionService interactionService;

    private static final Map<String, PaginatorInfo> PAGINATORS = new ConcurrentHashMap<>();
    public static final String PAGINATOR_BUTTON = "PAGINATOR_BUTTON";
    public static final String PAGINATOR_FOOTER_TEMPLATE_KEY = "paginator_footer";
    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, GuildMessageChannel messageChannel, Long userId) {
        Long serverId = messageChannel.getGuild().getIdLong();
        PaginatorSetup setup = getPaginatorSetup(templateKey, model, userId, serverId);
        log.info("Setting up paginator in channel {} in server {} with {} pages.", messageChannel.getIdLong(),
                messageChannel.getGuild().getIdLong(), setup.getConfiguration().getEmbedConfigs().size());
        List<CompletableFuture<Message>> paginatorFutures = channelService.sendMessageToSendToChannel(setup.getMessageToSend(), messageChannel);
        return FutureUtils.toSingleFutureGeneric(paginatorFutures)
                .thenAccept(unused -> self.setupButtonPayloads(paginatorFutures.get(0).join(), setup, serverId));
    }

    private PaginatorSetup getPaginatorSetup(String templateKey, Object model, Long userId, Long serverId) {
        String exitButtonId = componentService.generateComponentId(serverId);
        String startButtonId = componentService.generateComponentId(serverId);
        String previousButtonId = componentService.generateComponentId(serverId);
        String nextButtonId = componentService.generateComponentId(serverId);
        String lastButtonId = componentService.generateComponentId(serverId);
        PaginatorModel wrapperModel = PaginatorModel
                .builder()
                .exitButtonId(exitButtonId)
                .startButtonId(startButtonId)
                .previousButtonId(previousButtonId)
                .nextButtonId(nextButtonId)
                .lastButtonId(lastButtonId)
                .innerModel(model)
                .build();
        String embedConfig = templateService.renderTemplate(templateKey + "_paginator", wrapperModel, serverId);
        PaginatorConfiguration configuration = gson.fromJson(embedConfig, PaginatorConfiguration.class);
        setupFooters(configuration, serverId);

        configuration.setPaginatorId(componentService.generateComponentId(serverId));
        configuration.setSinglePage(configuration.getEmbedConfigs().size() < 2);
        PaginatorButtonPayload buttonPayload = getButtonPayload(configuration, exitButtonId, startButtonId, previousButtonId, nextButtonId, lastButtonId);
        if(configuration.getRestrictUser() != null && configuration.getRestrictUser()) {
            buttonPayload.setAllowedUser(userId);
        }
        configuration.setExitButton(initializeButton(exitButtonId, buttonPayload));
        if(!configuration.getSinglePage()) {
            log.debug("Adding additional buttons for pagination to paginator {}.", configuration.getPaginatorId());
            configuration.setStartButton(initializeButton(startButtonId, buttonPayload));
            configuration.setPreviousButton(initializeButton(previousButtonId, buttonPayload));
            configuration.setNextButton(initializeButton(nextButtonId, buttonPayload));
            configuration.setLastButton(initializeButton(lastButtonId, buttonPayload));
        }

        MessageConfiguration messageConfiguration = configuration.getEmbedConfigs().get(0);
        MessageToSend messageToSend = templateServiceBean.convertEmbedConfigurationToMessageToSend(messageConfiguration);
        return PaginatorSetup
                .builder()
                .messageToSend(messageToSend)
                .configuration(configuration)
                .payload(buttonPayload)
                .build();
    }

    @Override
    public CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, IReplyCallback callback) {
        Long serverId = ContextUtils.serverIdOrNull(callback);
        PaginatorSetup setup = getPaginatorSetup(templateKey, model, callback.getUser().getIdLong(), serverId);
        return interactionService.replyMessageToSend(setup.getMessageToSend(), callback)
                .thenCompose(interactionHook -> interactionHook.retrieveOriginal().submit())
                .thenAccept(message -> self.setupButtonPayloads(message, setup, serverId));
    }

    @Override
    public CompletableFuture<Void> sendPaginatorToInteraction(String templateKey, Object model, InteractionHook interactionHook) {
        Long serverId = interactionHook.getInteraction().getGuild().getIdLong();
        PaginatorSetup setup = getPaginatorSetup(templateKey, model, interactionHook.getInteraction().getUser().getIdLong(), serverId);
        CompletableFutureList<Message> futures =
            new CompletableFutureList<>(interactionService.sendMessageToInteraction(setup.getMessageToSend(), interactionHook));
        return futures
            .getMainFuture().thenAccept(aVoid -> self.setupButtonPayloads(futures.getFutures().get(0).join(), setup, serverId));
    }

    private void setupFooters(PaginatorConfiguration configuration, Long serverId) {
        for (int i = 0; i < configuration.getEmbedConfigs().size(); i++) {
            PaginatorFooterModel paginatorModel = PaginatorFooterModel
                    .builder()
                    .page(i + 1)
                    .pageCount(configuration.getEmbedConfigs().size())
                    .build();
            String footerText = templateService.renderTemplate(PAGINATOR_FOOTER_TEMPLATE_KEY, paginatorModel, serverId);
            MessageConfiguration messageConfig = configuration.getEmbedConfigs().get(i);
            if(messageConfig.getEmbeds() == null || messageConfig.getEmbeds().isEmpty()) {
                messageConfig.setEmbeds(new ArrayList<>(Arrays.asList(EmbedConfiguration.builder().build())));
            }
            EmbedConfiguration messageConfiguration = messageConfig.getEmbeds().get(0);
            if(messageConfiguration.getFooter() == null) {
                messageConfiguration.setFooter(EmbedFooter.builder().text(footerText).build());
            } else {
                messageConfiguration.getFooter().setText(footerText);
            }
        }
    }

    public void cleanupPaginatorPayloads(PaginatorButtonPayload configuration) {
        List<String> payloadIds = getAllPayloadIdsFromPayload(configuration);
        componentPayloadManagementService.deletePayloads(payloadIds);
    }

    private List<String> getAllPayloadIdsFromPayload(PaginatorButtonPayload configuration) {
        List<String> payloadIds = new ArrayList<>(Arrays.asList(configuration.getExitButtonId()));
        if(!configuration.getSinglePage()) {
            payloadIds.add(configuration.getStartButtonId());
            payloadIds.add(configuration.getPreviousButtonId());
            payloadIds.add(configuration.getNextButtonId());
            payloadIds.add(configuration.getLastButtonId());
        }
        return payloadIds;
    }


    private ButtonConfigModel initializeButton(String buttonId, PaginatorButtonPayload paginatorButtonPayload) {
        return ButtonConfigModel
                .builder()
                .buttonId(buttonId)
                .buttonPayload(paginatorButtonPayload)
                .payloadType(PaginatorButtonPayload.class)
                .origin(PAGINATOR_BUTTON)
                .build();
    }

    private PaginatorButtonPayload getButtonPayload(PaginatorConfiguration configuration, String exitButtonId,
                                                    String startButtonId, String previousButtonId,
                                                    String nextButtonId, String lastButtonId) {
        return PaginatorButtonPayload
                .builder()
                .paginatorId(configuration.getPaginatorId())
                .exitButtonId(exitButtonId)
                .startButtonId(startButtonId)
                .previousButtonId(previousButtonId)
                .nextButtonId(nextButtonId)
                .lastButtonId(lastButtonId)
                .embedConfigs(configuration.getEmbedConfigs())
                .singlePage(configuration.getSinglePage())
                .build();
    }

    public Integer getCurrentPage(String paginatorId) {
        return PAGINATORS.get(paginatorId).currentPage;
    }

    public PaginatorInfo getPaginatorInfo(String paginatorId) {
        return PAGINATORS.get(paginatorId);
    }

    public void updateCurrentPage(String paginatorId, Integer newPage, String newAccessorId) {
        try {
            lock.lock();
            PaginatorInfo paginatorInfo = PAGINATORS.get(paginatorId);
            if(paginatorInfo != null) {
                paginatorInfo.setCurrentPage(newPage);
                paginatorInfo.setLastAccessor(newAccessorId);
            }
        } catch (Exception exception) {
            lock.unlock();
            log.error("Failed to update current page for paginator {} to page {}", paginatorId, newPage, exception);
        }
    }

    public void schedulePaginationDeletion(String paginatorId, String accessorId) {
        PaginatorServiceBean.PaginatorInfo paginatorInfo = PAGINATORS.get(paginatorId);
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("paginatorId", paginatorId);
        parameters.put("accessorId", accessorId);
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        Instant targetDate = Instant.now().plus(paginatorInfo.getTimeoutSeconds(), ChronoUnit.SECONDS);
        schedulerService.executeJobWithParametersOnce("paginatorCleanupJob", "core", jobParameters, Date.from(targetDate));
        log.debug("Scheduled job to delete the paginator {} in {} seconds.", paginatorId, paginatorInfo.getTimeoutSeconds());
    }

    @Transactional
    public void setupButtonPayloads(Message paginatorMessage, PaginatorSetup setup, Long serverId) {
        PaginatorConfiguration configuration = setup.getConfiguration();
        PaginatorButtonPayload payload = setup.getPayload();
        savePayload(configuration.getExitButton(), serverId);
        if(!configuration.getSinglePage()) {
            savePayload(configuration.getStartButton(), serverId);
            savePayload(configuration.getPreviousButton(), serverId);
            savePayload(configuration.getNextButton(), serverId);
            savePayload(configuration.getLastButton(), serverId);
        }

        String accessorId = UUID.randomUUID().toString();

        PaginatorInfo info = PaginatorInfo
                .builder()
                .currentPage(0)
                .serverId(serverId)
                .channelId(paginatorMessage.getChannel().getIdLong())
                .messageId(paginatorMessage.getIdLong())
                .timeoutSeconds(configuration.getTimeoutSeconds())
                .paginatorId(configuration.getPaginatorId())
                .payloadIds(getAllPayloadIdsFromPayload(payload))
                .lastAccessor(accessorId)
                .build();
        log.debug("We are using the accessor id {} for paginator {} initially.", accessorId, configuration.getPaginatorId());
        PAGINATORS.put(configuration.getPaginatorId(), info);

        schedulePaginationDeletion(configuration.getPaginatorId(), accessorId);
    }

    private void savePayload(ButtonConfigModel model, Long serverId) {
        componentPayloadManagementService.createButtonPayload(model, serverId);
    }

    @Transactional
    public void cleanupPaginator(PaginatorInfo paginatorInfo) {
        // TODO not sure how this is supposed to work, maybe .... not sure
        log.info("Cleaning up paginator {} in server {} channel {} message {}.", paginatorInfo.getPaginatorId(),
                paginatorInfo.getServerId(), paginatorInfo.getChannelId(), paginatorInfo.getMessageId());
        if(paginatorInfo.getServerId() != null) { // user commands store them with null, and we cannot cleanup those
            messageService.deleteMessageInChannelInServer(paginatorInfo.getServerId(), paginatorInfo.getChannelId(), paginatorInfo.getMessageId());
        }
        componentPayloadManagementService.deletePayloads(paginatorInfo.getPayloadIds());
    }

    @Getter
    @Builder
    public static class PaginatorInfo {
        @Setter
        private Integer currentPage;
        private Long serverId;
        private Long channelId;
        private Long messageId;
        private String paginatorId;
        @Setter
        private String lastAccessor;
        private Long timeoutSeconds;
        private List<String> payloadIds;
    }

    @Getter
    @Builder
    public static class PaginatorSetup {
        private PaginatorConfiguration configuration;
        private PaginatorButtonPayload payload;
        private MessageToSend messageToSend;
    }

}
