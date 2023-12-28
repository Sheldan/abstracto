package dev.sheldan.abstracto.webservices.wikipedia.model.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class WikipediaResponsePage {
    @SerializedName("pageid")
    private Long pageId;
    private String title;
    private String extract;
    @SerializedName("fullurl")
    private String fullUrl;
    private Map<String, String> pageprops;

    public boolean isDisambiguation() {
        return pageprops != null && pageprops.containsKey("disambiguation");
    }
}
