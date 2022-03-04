package land.melon.lab.simplelanguageloader.utils;

import java.util.regex.Pattern;

public class TextUtils {
    /**
     * Counts the number of words in a string, except for color symbols that look like "&amp;7" and "&amp;#66ccff", '&amp;' is a color prefix and can be customized.
     *
     * @param text        text to count
     * @param colorPrefix the color prefix
     * @return number of words
     */
    public static int countWord(String text, String colorPrefix) {
        int count = 0;
        var convenientColorCodePattern = Pattern.compile(colorPrefix + "#[0-9A-Fa-f]{6}");
        var legacyColorCodePattern = Pattern.compile(colorPrefix + "[0-9A-Fa-fXx]");
        for (String noConvenient : convenientColorCodePattern.split(text)) {
            for (String noColor : legacyColorCodePattern.split(noConvenient)) {
                count += noColor.length();
            }
        }
        return count;
    }
}
