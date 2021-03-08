package dev.sheldan.abstracto.core.test.command;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import org.junit.Assert;

public class CommandConfigValidator {

    private CommandConfigValidator() {

    }

    public static void validateCommandConfiguration(CommandConfiguration toValidate) {
        Assert.assertNotNull(toValidate.getModule());
        Assert.assertNotNull(toValidate.getName());
        if(toValidate.isTemplated()) {
            Assert.assertNull(toValidate.getDescription());
        } else {
            Assert.assertNotNull(toValidate.getDescription());
        }
        toValidate.getParameters().forEach(parameter -> {
            Assert.assertNotNull(parameter.getName());
            Assert.assertNotNull(parameter.getType());
            if(parameter.getTemplated()) {
                Assert.assertNull(parameter.getDescription());
            } else {
                Assert.assertNotNull(parameter.getDescription());
            }
        });
        HelpInfo helpInfo = toValidate.getHelp();
        Assert.assertNotNull(helpInfo);
        if(helpInfo.isTemplated()) {
            Assert.assertNull(helpInfo.getLongHelp());
            Assert.assertNull(helpInfo.getExample());
        } else {
            Assert.assertNotNull(helpInfo.getLongHelp());
            if(helpInfo.isHasExample()) {
                Assert.assertNotNull(helpInfo.getExample());
            } else {
                Assert.assertNull(helpInfo.getExample());
            }
        }
    }
}
