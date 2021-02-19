package dev.sheldan.abstracto.core.templating.config;

import freemarker.cache.TemplateLookupContext;
import freemarker.cache.TemplateLookupResult;
import freemarker.cache.TemplateLookupStrategy;

import java.io.IOException;

public class AbstractoTemplateLookupStrategy extends TemplateLookupStrategy {
    @Override
    public TemplateLookupResult lookup(TemplateLookupContext ctx) throws IOException {
        if(ctx.getCustomLookupCondition() != null) {
            return ctx.lookupWithLocalizedThenAcquisitionStrategy(ctx.getCustomLookupCondition() + "/" + ctx.getTemplateName(), ctx.getTemplateLocale());
        } else {
            return ctx.lookupWithLocalizedThenAcquisitionStrategy(ctx.getTemplateName(), ctx.getTemplateLocale());
        }
    }
}
