package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Service
@Slf4j
public class BotServiceBean implements BotService {

    private JDA instance;

    @Autowired
    private OkHttpMetrics okHttpMetrics;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = JDABuilder.create(System.getenv("TOKEN"), GUILD_VOICE_STATES,
                GUILD_EMOJIS, GUILD_MEMBERS, GUILD_MESSAGE_REACTIONS, GUILD_MESSAGES,
                GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGE_REACTIONS, DIRECT_MESSAGES, GUILD_PRESENCES);

        builder.setBulkDeleteSplittingEnabled(false);
        builder.setMemberCachePolicy(MemberCachePolicy.DEFAULT);
        OkHttpClient.Builder defaultBuilder = IOUtil.newHttpClientBuilder();
        defaultBuilder.addInterceptor(okHttpMetrics);
        builder.setHttpClientBuilder(defaultBuilder);

        this.instance = builder.build();
    }

    @Override
    public JDA getInstance() {
        return instance;
    }


}
