package dev.sheldan.abstracto.giveaway.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class GiveawayKeyNotFoundException extends AbstractoTemplatableException {
  public GiveawayKeyNotFoundException() {
    super("Giveaway key not found.");
  }

  @Override
  public String getTemplateName() {
    return "giveaway_key_not_found_exception";
  }

  @Override
  public Object getTemplateModel() {
    return new Object();
  }
}
