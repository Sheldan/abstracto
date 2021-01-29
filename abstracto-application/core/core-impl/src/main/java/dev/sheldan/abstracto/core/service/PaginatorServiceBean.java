package dev.sheldan.abstracto.core.service;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import dev.sheldan.abstracto.core.model.PaginatorConfiguration;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PaginatorServiceBean implements PaginatorService {

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Gson gson;

    @Autowired
    private MessageService messageService;

    @Override
    public Paginator createPaginatorFromTemplate(String templateKey, Object model, EventWaiter waiter) {
        String embedConfig = templateService.renderTemplate(templateKey + "_paginator", model);
        PaginatorConfiguration configuration = gson.fromJson(embedConfig, PaginatorConfiguration.class);
        List<String> items = configuration.getItems();
        int itemsPerPage = findAppropriateCountPerPage(items);

        return new Paginator.Builder()
                .setItemsPerPage(itemsPerPage)
                .setText(configuration.getHeaderText())
                .showPageNumbers(ObjectUtils.defaultIfNull(configuration.getShowPageNumbers(), false))
                .setItems(configuration.getItems().toArray(new String[0]))
                .useNumberedItems(ObjectUtils.defaultIfNull(configuration.getUseNumberedItems(), false))
                .setEventWaiter(waiter)
                .waitOnSinglePage(true)
                .setTimeout(ObjectUtils.defaultIfNull(configuration.getTimeoutSeconds(), 120L), TimeUnit.SECONDS)
                .setFinalAction(message -> messageService.deleteMessage(message))
                .build();
    }

    private int findAppropriateCountPerPage(List<String> items) {
        int currentMin = Integer.MAX_VALUE;
        // to be sure, because the paginator adds some characters here and there
        int carefulMax = MessageEmbed.TEXT_MAX_LENGTH - 50;
        for (int i = 0; i < items.size(); i++) {
            int count = 0;
            int length = 0;
            for (String innerItem : items) {
                length += innerItem.length();
                if (length > carefulMax) {
                    currentMin = Math.min(currentMin, count);
                    break;
                }
                count++;
            }
        }
        return currentMin;
    }
}
