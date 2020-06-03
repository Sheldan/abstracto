package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExceptionUtils {

    @Autowired
    private TemplateService templateService;

    @Transactional
    public void handleExceptionIfTemplatable(Throwable throwable, MessageChannel channel) {
        if(throwable != null) {
            if(throwable.getCause() instanceof Templatable) {
                String exceptionText = templateService.renderTemplatable((Templatable) throwable.getCause());
                channel.sendMessage(exceptionText).queue();
            } else {
                channel.sendMessage(throwable.getCause().getMessage()).queue();
            }
        }
    }
}
