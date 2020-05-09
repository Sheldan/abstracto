package dev.sheldan.abstracto.core.service;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;

public interface PaginatorService {
    Paginator createPaginatorFromTemplate(String templateKey, Object model, EventWaiter waiter);
}
