package dev.sheldan.abstracto.core.service.paginator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaginatorModel {
    private Object innerModel;
    private String exitButtonId;
    private String startButtonId;
    private String previousButtonId;
    private String nextButtonId;
    private String lastButtonId;
}
