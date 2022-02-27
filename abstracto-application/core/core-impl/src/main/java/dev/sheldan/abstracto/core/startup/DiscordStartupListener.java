package dev.sheldan.abstracto.core.startup;

import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscordStartupListener implements AsyncStartupListener {

    public static final String DISCORD_GATEWAY_PING = "discord.gateway.ping";
    private static final CounterMetric DISCORD_GATE_WAY_PING_METRIC = CounterMetric
            .builder()
            .name(DISCORD_GATEWAY_PING)
            .build();

    @Autowired
    private MetricService metricService;

    @Autowired
    private BotService botService;

    @Override
    public void execute() {
        metricService.registerGauge(DISCORD_GATE_WAY_PING_METRIC, botService, value -> value.getInstance().getGatewayPing(),
                "Gateway ping");
    }
}
