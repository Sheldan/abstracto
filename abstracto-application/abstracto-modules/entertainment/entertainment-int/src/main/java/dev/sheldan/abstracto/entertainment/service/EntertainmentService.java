package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.entertainment.exception.ReactDuplicateCharacterException;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public interface EntertainmentService {
    String getEightBallValue(String text);
    Integer getLoveCalcValue(String firstPart, String secondPart);
    Integer calculateRollResult(Integer low, Integer high);
    boolean executeRoulette(Member memberExecuting);
    String takeChoice(List<String> choices, Member memberExecuting);
    String createMockText(String text, Member memberExecuting, Member mockedUser);

    /**
     * Converts the given text to unicode characters (with predefined values from a manual mapping) and returns the matched
     * characters as a list. If the given text is null, an empty list will be returned. This method will actively try
     * to avoid duplicates, and try to use alternatives, and throw an exception in case it was not possible to return unique values.
     * The size of the list might not be equal to the length of the provided string, because sometimes multiple
     * characters are combined into one unicode char.
     * @throws ReactDuplicateCharacterException In case it was not possible to replace all text with appropriate unicode
     * in case there too many duplicated characters
     * @param text The text to convert
     * @return A {@link List} of unicode characters, represented as strings, which look similar to the individual characters
     * from the text
     */
    List<String> convertTextToEmojis(String text);

    /**
     * Converts the given text to unicode characters (with predefined values from a manual mapping) and returns the matched
     * characters as a string. If the given text is null, an empty string will be returned. This method will actively try
     * to avoid duplicates, and try to use alternatives, and throw an exception in case it was not possible to return unique values.
     * The length of the string might not be equal to the length of the provided string, because sometimes multiple
     * characters are combined into one unicode char.
     * @throws ReactDuplicateCharacterException In case it was not possible to replace all text with appropriate unicode
     * in case there too many duplicated characters
     * @param text The text to convert
     * @return A string of unicode characters which look similar to the individual characters from the text
     */
    String convertTextToEmojisAString(String text);

    /**
     * Converts the given text to unicode characters (with predefined values from a manual mapping) and returns the matched
     * characters as a list. If the given text is null, an empty list will be returned. This method will actively try
     * to avoid duplicates (if requested), and try to use alternatives, and throw an exception in case it was not possible
     * to return unique values. In case duplicates are allowed, the first possible replacement value will be used,
     * leading to all 1:1 replacements being of the same character.
     * The size of the list might not be equal to the length of the provided string, because sometimes multiple
     * characters are combined into one unicode char.
     * @throws ReactDuplicateCharacterException In case it was not possible to replace all text with appropriate unicode
     * in case there too many duplicated characters
     * @param text The text to convert
     * @param allowDuplicates Whether or not to allow duplicates
     * @return A list of characters, represented as strings, which look similar to the individual characters
     * from the text, possible with duplicates, if requested
     */
    List<String> convertTextToEmojis(String text, boolean allowDuplicates);

    /**
     * Converts the given text to unicode characters (with predefined values from a manual mapping) and returns the matched
     * characters as a string. If the given text is null, an empty string will be returned. This method will actively try
     * to avoid duplicates (if requested), and try to use alternatives, and throw an exception in case it was not possible
     * to return unique values. In case duplicates are allowed, the first possible replacement value will be used,
     * leading to all 1:1 replacements being of the same character.
     * The length of the string might not be equal to the length of the provided string, because sometimes multiple
     * characters are combined into one unicode char.
     * @throws ReactDuplicateCharacterException In case it was not possible to replace all text with appropriate unicode
     * in case there too many duplicated characters
     * @param text The text to convert
     * @param allowDuplicates Whether or not to allow duplicates
     * @return A string of unicode characters which look similar to the individual characters from the text
     */
    String convertTextToEmojisAsString(String text, boolean allowDuplicates);
}
