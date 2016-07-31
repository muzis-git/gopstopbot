package ru.gopstop.bot.engine.search.preprocessing;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aam on 31.07.16.
 */
public class SubstringCollapsingReducer {

    private final static Map<String, String> replacements =
            new HashMap<>();

    static {
        replacements.put("сс", "с");
        replacements.put("цк", "ск");
    }

    public static String applyReplacements(String word) {

        String result = word;
        for (Map.Entry<String, String> substring : replacements.entrySet()) {
            result = result.replaceAll(substring.getKey(), substring.getValue());
        }
        return result;
    }
}
