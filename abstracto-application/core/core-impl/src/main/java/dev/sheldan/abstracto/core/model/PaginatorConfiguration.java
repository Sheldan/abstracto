package dev.sheldan.abstracto.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatorConfiguration {
    private String headerText;
    private List<String> items;
    private Long timeoutSeconds;
    private Boolean showPageNumbers;
    private Boolean useNumberedItems;
}
