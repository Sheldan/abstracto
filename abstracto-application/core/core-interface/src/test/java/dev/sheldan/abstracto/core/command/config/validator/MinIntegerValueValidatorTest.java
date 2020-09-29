package dev.sheldan.abstracto.core.command.config.validator;

import dev.sheldan.abstracto.core.command.exception.ValidatorConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MinIntegerValueValidatorTest {
    @InjectMocks
    private MinIntegerValueValidator validator;

    @Before
    public void setup() {
        validator.setMinValue(4L);
    }

    @Test(expected = ValidatorConfigException.class)
    public void incorrectArgument() {
        validator.validate("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArgument() {
        validator.validate(null);
    }

    @Test
    public void tooLittleValue() {
        Assert.assertFalse(validator.validate(3L));
    }

    @Test
    public void exactValue() {
        Assert.assertTrue(validator.validate(4L));
    }

    @Test
    public void largeEnoughValue() {
        Assert.assertTrue(validator.validate(5L));
    }
}
