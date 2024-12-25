package dev.sheldan.abstracto.giveaway.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class GiveawayNotPossibleException extends AbstractoTemplatableException {
  public GiveawayNotPossibleException() {
    super("Giveaway not possible.");
  }

  @Override
  public String getTemplateName() {
    return "giveaway_not_possible_exception";
  }

  @Override
  public Object getTemplateModel() {
    return new Object();
  }
}
