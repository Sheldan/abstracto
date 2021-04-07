package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import dev.sheldan.abstracto.core.models.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Service
@Slf4j
public class BotServiceBean implements BotService {

    private JDA instance;

    @Autowired
    private OkHttpMetrics okHttpMetrics;

    @Override
    public void login() throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(System.getenv("TOKEN"));
        builder.enableIntents(GUILD_VOICE_STATES, GUILD_BANS,
        GUILD_EMOJIS, GUILD_MEMBERS, GUILD_MESSAGES,
        GUILD_MESSAGE_REACTIONS, DIRECT_MESSAGES);

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

    @Override
    public SystemInfo getSystemInfo() {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        Instant startTime = Instant.ofEpochMilli(bean.getStartTime());
        Duration upTime = Duration.ofMillis(bean.getUptime());
        return SystemInfo
                .builder()
                .startTime(startTime)
                .uptime(upTime)
                .build();
    }


}
