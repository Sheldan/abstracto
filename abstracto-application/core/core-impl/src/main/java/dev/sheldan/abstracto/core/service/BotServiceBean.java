package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.logging.OkHttpLogger;
import dev.sheldan.abstracto.core.metric.OkHttpMetrics;
import dev.sheldan.abstracto.core.models.SystemInfo;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BotServiceBean implements BotService {

    private JDA instance;

    @Autowired
    private OkHttpMetrics okHttpMetrics;

    @Autowired
    private OkHttpLogger okHttpLogger;

    @Value("${abstracto.intents:GUILD_VOICE_STATES,GUILD_MODERATION,MESSAGE_CONTENT,GUILD_EMOJIS_AND_STICKERS,GUILD_MEMBERS,GUILD_MESSAGES,GUILD_MESSAGE_REACTIONS,DIRECT_MESSAGES,GUILD_PRESENCES}")
    private String[] intents;

    @Value("${abstracto.cacheFlags:ACTIVITY,ONLINE_STATUS,VOICE_STATE}")
    private String[] cacheFlags;

    @Value("${abstracto.memberCachePolicy:ALL}")
    private String memberCachePolicy;

    @Autowired
    @Qualifier("genericExecutor")
    private TaskExecutor genericExecutor;

    @Autowired
    private ObservationRegistry observationRegistry;

    private OkHttpObservationInterceptor.Builder defaultInterceptorBuilder() {
        return OkHttpObservationInterceptor.builder(observationRegistry, "okhttp.requests")
                .uriMapper(req -> req.url().encodedPath());
    }

    private static final Map<String, MemberCachePolicy> POSSIBLE_MEMBER_CACHE_POLICIES = new HashMap<>();

    static {
        POSSIBLE_MEMBER_CACHE_POLICIES.put("ALL", MemberCachePolicy.ALL);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("NONE", MemberCachePolicy.NONE);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("OWNER", MemberCachePolicy.OWNER);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("ONLINE", MemberCachePolicy.ONLINE);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("VOICE", MemberCachePolicy.VOICE);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("BOOSTER", MemberCachePolicy.BOOSTER);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("PENDING", MemberCachePolicy.PENDING);
        POSSIBLE_MEMBER_CACHE_POLICIES.put("DEFAULT", MemberCachePolicy.DEFAULT);
    }

    @Override
    public void login() {
        JDABuilder builder = JDABuilder.createDefault(System.getenv("TOKEN"));

        List<GatewayIntent> intentsToEnable = Arrays.stream(intents)
                .map(GatewayIntent::valueOf)
                .toList();

        builder.enableIntents(intentsToEnable);

        List<CacheFlag> cacheFlagsToUse = Arrays.stream(cacheFlags)
                        .map(CacheFlag::valueOf)
                        .toList();

        builder.enableCache(cacheFlagsToUse);

        MemberCachePolicy chosenMemberCachePolicy = POSSIBLE_MEMBER_CACHE_POLICIES.get(memberCachePolicy);
        builder.setMemberCachePolicy(chosenMemberCachePolicy);

        builder.setChunkingFilter(ChunkingFilter.ALL);

        builder.setBulkDeleteSplittingEnabled(false);
        OkHttpClient.Builder defaultBuilder = IOUtil.newHttpClientBuilder();
        defaultBuilder.addInterceptor(okHttpMetrics);
        defaultBuilder.addInterceptor(okHttpLogger);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
        defaultBuilder.dispatcher(new Dispatcher(ContextExecutorService.wrap(executor, ContextSnapshotFactory.builder().build()::captureAll)));
        defaultBuilder.addInterceptor(defaultInterceptorBuilder().build());
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
