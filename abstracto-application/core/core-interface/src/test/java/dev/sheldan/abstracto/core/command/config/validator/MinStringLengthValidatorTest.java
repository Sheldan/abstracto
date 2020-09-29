package dev.sheldan.abstracto.core.command.config.validator;


import dev.sheldan.abstracto.core.command.exception.ValidatorConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MinStringLengthValidatorTest {
    @InjectMocks
    private MinStringLengthValidator validator;

    @Before
    public void setup() {
        validator.setMinLength(4L);
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
        Assert.assertFalse(validator.validate(""));
    }

    @Test
    public void validateShortString() {
        Assert.assertFalse(validator.validate("t"));
    }

    @Test
    public void validateExactString() {
        Assert.assertTrue(validator.validate("text"));
    }

    @Test
    public void validateLongEnoughString() {
        Assert.assertTrue(validator.validate("text1"));
    }
}
