package dev.sheldan.abstracto.utility.service;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public interface EntertainmentService {
    String getEightBallValue(String text);
    Integer getLoveCalcValue(String firstPart, String secondPart);
    Integer calculateRollResult(Integer low, Integer high);
    boolean executeRoulette(Member memberExecuting);
    String takeChoice(List<String> choices, Member memberExecuting);
}
