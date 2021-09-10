package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.model.CommandServiceBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandServiceBeanTest {

    private static final String COMMAND_NAME = "test";
    private static final String PARAMETER_1_NAME = "param1";
    private static final String PARAMETER_2_NAME = "param2";

    @InjectMocks
    private CommandServiceBean commandServiceBean;

    @Test
    public void testUsageWithoutParameters() {
        CommandConfiguration parameters = getNoParameters();
        executeTest("test", commandWithConfig(parameters));
    }

    @Test
    public void testUsageWithOptionalParameter() {
        CommandConfiguration parameters = getOptionalParameterConfig();
        executeTest("test [param1]", commandWithConfig(parameters));
    }

    @Test
    public void testUsageWithMandatoryParameter() {
        CommandConfiguration getparameters = getMandatoryParameterConfig();
        executeTest("test <param1>", commandWithConfig(getparameters));
    }

    @Test
    public void testUsageWithMixedParameters() {
        CommandConfiguration parameters = getMixedParameterConfig();
        executeTest("test <param1> [param2]", commandWithConfig(parameters));
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
        CommandConfiguration configuration = Mockito.mock(CommandConfiguration.class);
        when(configuration.getName()).thenReturn(COMMAND_NAME);
        return configuration;
    }

    private CommandConfiguration getOptionalParameterConfig() {
        CommandConfiguration configuration = Mockito.mock(CommandConfiguration.class);
        when(configuration.getName()).thenReturn(COMMAND_NAME);
        List<Parameter> parameters = Arrays.asList(getParameter(true));
        when(configuration.getParameters()).thenReturn(parameters);
        return configuration;
    }

    private CommandConfiguration getMandatoryParameterConfig() {
        CommandConfiguration configuration = Mockito.mock(CommandConfiguration.class);
        when(configuration.getName()).thenReturn(COMMAND_NAME);
        List<Parameter> parameters = Arrays.asList(getParameter(false));
        when(configuration.getParameters()).thenReturn(parameters);
        return configuration;
    }

    private CommandConfiguration getMixedParameterConfig() {
        Parameter param1 = getParameter(false);
        Parameter param2 = Mockito.mock(Parameter.class);
        when(param2.getName()).thenReturn(PARAMETER_2_NAME);
        when(param2.getType()).thenReturn(Object.class);
        when(param2.isOptional()).thenReturn(true);
        CommandConfiguration configuration = Mockito.mock(CommandConfiguration.class);
        when(configuration.getName()).thenReturn(COMMAND_NAME);
        List<Parameter> parameters = Arrays.asList(param1, param2);
        when(configuration.getParameters()).thenReturn(parameters);
        return configuration;
    }

    private Parameter getParameter(boolean optional) {
        Parameter parameter = Mockito.mock(Parameter.class);
        when(parameter.getType()).thenReturn(Object.class);
        when(parameter.getName()).thenReturn(PARAMETER_1_NAME);
        when(parameter.isOptional()).thenReturn(optional);
        return parameter;
    }

}
