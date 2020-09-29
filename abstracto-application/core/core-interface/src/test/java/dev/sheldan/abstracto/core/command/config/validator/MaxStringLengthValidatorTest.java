package dev.sheldan.abstracto.core.command.config.validator;


import dev.sheldan.abstracto.core.command.exception.ValidatorConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaxStringLengthValidatorTest {
    @InjectMocks
    private MaxStringLengthValidator validator;

    @Before
    public void setup() {
        validator.setMaxLength(4L);
    }

    @Test(expected = ValidatorConfigException.class)
    public void incorrectArgument() {
        validator.validate(4L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArgument() {
        validator.validate(null);
    }

    @Test
    public void validateEmptyString() {
        Assert.assertTrue(validator.validate(""));
    }

    @Test
    public void validateShortString() {
        Assert.assertTrue(validator.validate("t"));
    }

    @Test
    public void validateExactString() {
        Assert.assertTrue(validator.validate("text"));
    }

    @Test
    public void validateTooLongString() {
        Assert.assertFalse(validator.validate("text1"));
    }


}
