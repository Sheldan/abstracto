package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandServiceBeanTest {

    private static final String COMMAND_NAME = "test";
    private static final String PARAMETER_1_NAME = "param1";
    private static final String PARAMETER_2_NAME = "param2";

    @InjectMocks
    private CommandServiceBean commandServiceBean;

    @Test
    public void testUsageWithoutParameters() {
        executeTest("test", commandWithConfig(getNoParameters()));
    }

    @Test
    public void testUsageWithOptionalParameter() {
        executeTest("test [param1]", commandWithConfig(getOptionalParameterConfig()));
    }

    @Test
    public void testUsageWithMandatoryParameter() {
        executeTest("test <param1>", commandWithConfig(getMandatoryParameterConfig()));
    }

    @Test
    public void testUsageWithMixedParameters() {
        executeTest("test <param1> [param2]", commandWithConfig(getMixedParameterConfig()));
    }

    private void executeTest(String expectedUsage, Command commandToExecute) {
        String generatedUsage = commandServiceBean.generateUsage(commandToExecute);
        Assert.assertEquals(expectedUsage, generatedUsage);
    }

    private Command commandWithConfig(CommandConfiguration commandConfiguration) {
        Command command = Mockito.mock(Command.class);
        when(command.getConfiguration()).thenReturn(commandConfiguration);
        return command;
    }

    private CommandConfiguration getNoParameters() {
        return CommandConfiguration
                .builder()
                .name(COMMAND_NAME)
                .build();
    }

    private CommandConfiguration getOptionalParameterConfig() {
        return CommandConfiguration
                .builder()
                .name(COMMAND_NAME)
                .parameters(Arrays.asList(getOptionalParameter(true)))
                .build();
    }

    private CommandConfiguration getMandatoryParameterConfig() {
        return CommandConfiguration
                .builder()
                .name(COMMAND_NAME)
                .parameters(Arrays.asList(getOptionalParameter(false)))
                .build();
    }

    private CommandConfiguration getMixedParameterConfig() {
        Parameter param1 = Parameter.builder().name(PARAMETER_1_NAME).optional(false).build();
        Parameter param2 = Parameter.builder().name(PARAMETER_2_NAME).optional(true).build();
        return CommandConfiguration
                .builder()
                .name(COMMAND_NAME)
                .parameters(Arrays.asList(param1, param2))
                .build();
    }

    private Parameter getOptionalParameter(boolean b) {
        return Parameter.builder().name(PARAMETER_1_NAME).optional(b).build();
    }

}
