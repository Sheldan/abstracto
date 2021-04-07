package dev.sheldan.abstracto.entertainment.service;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public interface EntertainmentService {
    String getEightBallValue(String text);
    Integer getLoveCalcValue(String firstPart, String secondPart);
    Integer calculateRollResult(Integer low, Integer high);
    boolean executeRoulette(Member memberExecuting);
    String takeChoice(List<String> choices, Member memberExecuting);
    String createMockText(String text, Member memberExecuting, Member mockedUser);
}
