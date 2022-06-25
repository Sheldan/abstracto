package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.ImmuneUserConditionDetail;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.EffectConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ImmuneUserCondition implements CommandCondition {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    @Autowired
    private ImmuneUserCondition self;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<ConditionResult> shouldExecuteAsync(CommandContext commandContext, Command command) {
        CommandConfiguration commandConfig = command.getConfiguration();
        if(commandConfig.getEffects().isEmpty()) {
            return ConditionResult.fromAsyncSuccess();
        }
        List<CompletableFuture<Member>> futures = new ArrayList<>();
        List<Object> parameters = commandContext.getParameters().getParameters();
        for (EffectConfig effectConfig : commandConfig.getEffects()) {
            Integer position = effectConfig.getPosition();
            if (position < parameters.size()) {
                Object parameter = parameters.get(position);
                if (parameter instanceof Member) {
                    Member member = (Member) parameter;
                    futures.add(CompletableFuture.completedFuture(member));
                } else if (parameter instanceof User) {
                    User user = (User) parameter;
                    futures.add(commandContext.getGuild().retrieveMember(user).submit());
                }
            } else {
                log.info("Not enough parameters ({}) in command {} to retrieve position {} to check for immunity.",
                        parameters.size(), commandConfig.getName(), position);
            }
        }
        if(!futures.isEmpty()) {
            CompletableFuture<ConditionResult> resultFuture = new CompletableFuture<>();
            CompletableFutureList<Member> futureList = new CompletableFutureList<>(futures);
            futureList.getMainFuture().whenComplete((unused, throwable) -> {
                if(throwable != null) {
                    log.warn("Future for user immune condition failed. Continuing processing.", throwable);
                }
                Map<Long, Member> memberMap = futureList
                        .getObjects()
                        .stream()
                        .collect(Collectors.toMap(Member::getIdLong, Function.identity()));
                self.checkConditions(commandConfig, parameters, resultFuture, memberMap);
            }).exceptionally(throwable -> {
                resultFuture.completeExceptionally(throwable);
                return null;
            });

            return resultFuture;
        } else {
            return ConditionResult.fromAsyncSuccess();
        }
    }

    @Transactional
    public void checkConditions(CommandConfiguration commandConfig, List<Object> parameters, CompletableFuture<ConditionResult> resultFuture, Map<Long, Member> memberMap) {
        for (EffectConfig effectConfig : commandConfig.getEffects()) {
            Integer position = effectConfig.getPosition();
            if (position < parameters.size()) {
                Object parameter = parameters.get(position);
                Member member = null;
                if (parameter instanceof Member) {
                    member = (Member) parameter;
                } else if (parameter instanceof User) {
                    User user = (User) parameter;
                    member = memberMap.get(user.getIdLong());
                }
                if(member != null && checkRoleImmunity(resultFuture, effectConfig, member)) {
                    return;
                }
            } else {
                log.info("Not enough parameters ({}) in command {} to retrieve position {} to check for immunity.",
                        parameters.size(), commandConfig.getName(), position);
            }
        }
        resultFuture.complete(ConditionResult.fromSuccess());
    }

    @Override
    public CompletableFuture<ConditionResult> shouldExecuteAsync(SlashCommandInteractionEvent event, Command command) {
        CommandConfiguration commandConfig = command.getConfiguration();
        if(commandConfig.getEffects().isEmpty()) {
            return ConditionResult.fromAsyncSuccess();
        }
        List<CompletableFuture<Member>> futures = new ArrayList<>();
        for (EffectConfig effectConfig : commandConfig.getEffects()) {
            String parameterName = effectConfig.getParameterName();
            if (slashCommandParameterService.hasCommandOptionWithFullType(parameterName, event, OptionType.USER)) {
                if (slashCommandParameterService.hasCommandOption(parameterName, event, User.class, User.class)) {
                    User user = slashCommandParameterService.getCommandOption(parameterName, event, User.class);
                    futures.add(event.getGuild().retrieveMember(user).submit());
                } else if (slashCommandParameterService.hasCommandOption(parameterName, event, Member.class, Member.class)) {
                    Member member = slashCommandParameterService.getCommandOption(parameterName, event, Member.class);
                    futures.add(CompletableFuture.completedFuture(member));
                }
            } else {
                log.info("Not found parameter {} in slash command event in guild {} in channel {} from user {}.",
                        parameterName, event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getUser().getIdLong());
            }
        }
        if(!futures.isEmpty()) {
            CompletableFuture<ConditionResult> resultFuture = new CompletableFuture<>();
            CompletableFutureList<Member> futureList = new CompletableFutureList<>(futures);
            futureList.getMainFuture().whenComplete((unused, throwable) -> {
                if(throwable != null) {
                    log.warn("Future for user immune condition failed. Continuing processing.", throwable);
                }
                Map<Long, Member> memberMap = futureList
                        .getObjects()
                        .stream()
                        .collect(Collectors.toMap(Member::getIdLong, Function.identity()));
                self.checkConditions(commandConfig, event, resultFuture, memberMap);
            }).exceptionally(throwable -> {
                resultFuture.completeExceptionally(throwable);
                return null;
            });

            return resultFuture;
        } else {
            return ConditionResult.fromAsyncSuccess();
        }
    }

    @Transactional
    public void checkConditions(CommandConfiguration commandConfig, SlashCommandInteractionEvent event, CompletableFuture<ConditionResult> resultFuture, Map<Long, Member> memberMap) {
        for (EffectConfig effectConfig : commandConfig.getEffects()) {
            String parameterName = effectConfig.getParameterName();
            if (slashCommandParameterService.hasCommandOptionWithFullType(parameterName, event, OptionType.USER)) {
                Member member = null;
                if (slashCommandParameterService.hasCommandOption(parameterName, event, User.class, User.class)) {
                    User user = slashCommandParameterService.getCommandOption(parameterName, event, User.class);
                    member = memberMap.get(user.getIdLong());
                } else if (slashCommandParameterService.hasCommandOption(parameterName, event, Member.class, Member.class)) {
                    member = slashCommandParameterService.getCommandOption(parameterName, event, Member.class);
                }
                if(member != null && checkRoleImmunity(resultFuture, effectConfig, member)) {
                    return;
                }
            } else {
                log.info("Not found parameter {} in slash command event in guild {} in channel {} from user {}.",
                        parameterName, event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getUser().getIdLong());
            }
        }
        resultFuture.complete(ConditionResult.fromSuccess());
    }

    private boolean checkRoleImmunity(CompletableFuture<ConditionResult> resultFuture, EffectConfig effectConfig, Member member) {
        Optional<RoleImmunity> immunityOptional = roleImmunityService.getRoleImmunity(member, effectConfig.getEffectKey());
        if (immunityOptional.isPresent()) {
            RoleImmunity immunity = immunityOptional.get();
            ImmuneUserConditionDetail conditionDetail = new ImmuneUserConditionDetail(roleService.getRoleFromGuild(immunity.getRole()),
                    effectConfig.getEffectKey());
            resultFuture.complete(ConditionResult.fromFailure(conditionDetail));
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsSlashCommands() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
