package ru.gopstop.bot.engine.search.preprocessing;

/**
 * Created by aam on 31.07.16.
 */
public class BasicPreprocessor {

    public static String postfix(String line) {

        String normalLine = line.trim();
        String processedLine = new StringBuilder(normalLine.toLowerCase()).reverse().toString();
        //todo augmentation
        return processedLine;
    }
}