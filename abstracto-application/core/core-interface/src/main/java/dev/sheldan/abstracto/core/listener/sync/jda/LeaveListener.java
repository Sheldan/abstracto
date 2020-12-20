package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public interface LeaveListener extends FeatureAware, Prioritized {
    void execute(Member member, Guild guild);
}
