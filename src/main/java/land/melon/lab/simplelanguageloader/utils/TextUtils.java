package land.melon.lab.simplelanguageloader.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    /**
     * Counts the number of words in a string, except for color symbols that look like "&amp;7" and "&amp;#66ccff", '&amp;' is a color prefix and can be customized.
     *
     * @param text        text to count
     * @param colorPrefix the color prefix
     * @return number of words
     */
    public static int countWord(String text, char colorPrefix) {
        int count = 0;
        var convenientColorCodePattern = Pattern.compile(colorPrefix + "#[0-9A-Fa-f]{6}");
        var legacyColorCodePattern = Pattern.compile(colorPrefix + "[0-9A-Fa-fLlMmNnOoRrXxKk]");
        for (String noConvenient : convenientColorCodePattern.split(text)) {
            for (String noColor : legacyColorCodePattern.split(noConvenient)) {
                count += noColor.length();
            }
        }
        return count;
    }

    public static List<String> extractPlaceholders(String message) {
        List<String> placeholders = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    public static String[] splitMessage(String message) {
        return message.split("\\{.*?}");
    }
}
