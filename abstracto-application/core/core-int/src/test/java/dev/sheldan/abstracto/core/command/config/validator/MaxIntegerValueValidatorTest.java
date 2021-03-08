package dev.sheldan.abstracto.core.command.config.validator;

import dev.sheldan.abstracto.core.command.exception.ValidatorConfigException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaxIntegerValueValidatorTest {
    @InjectMocks
    private MaxIntegerValueValidator validator;

    @Before
    public void setup() {
        validator.setMaxValue(4L);
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
    public void littleEnoughValue() {
        Assert.assertTrue(validator.validate(3L));
    }

    @Test
    public void exactValue() {
        Assert.assertTrue(validator.validate(4L));
    }

    @Test
    public void tooLargeValue() {
        Assert.assertFalse(validator.validate(5L));
    }
}
