package land.melon.lab.simplelanguageloader.utils;


import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Used to process color codes.
 */
public final class ColorConverter {
    /**
     * hex color pattern looks like <code>&amp;#RRGGBB</code>
     */
    public static final Pattern hexColorPattern = Pattern.compile("&#[0-9A-Fa-f]{6}");

    private ColorConverter() {
    }

    /**
     * Replace the convenient RGB color code with expanded color code in a string.
     *
     * @param text text to be proceeded
     * @return text with expanded color code
     */
    public static String convertConvenientColorCode(String text) {
        var textParts = hexColorPattern.split(text);
        if(textParts.length == 0)
            textParts = new String[]{"",""};
        var colorCodes = hexColorPattern.matcher(text).results().map(MatchResult::group).toArray(String[]::new);
        var textBuilder = new StringBuilder(textParts[0]);
        for (int i = 0; i < colorCodes.length; i++) {
            textBuilder.append(convertToTraditionalColorCode(colorCodes[i])).append(textParts[i + 1]);
        }
        return textBuilder.toString();
    }

    /**
     * Expand color code looks like <code>&amp;#RRGGBB</code> to <code>&amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B</code>.
     *
     * @param x convenient color code
     * @return expanded color code
     */
    private static String convertToTraditionalColorCode(String x) {
        var hexColorCode = x.substring(2);
        var colorCodeBuilder = new StringBuilder("&x");
        hexColorCode.chars().forEach(c -> colorCodeBuilder.append("&").append((char) c));
        return colorCodeBuilder.toString();
    }

    public static String convertToLegacyColorCode(String text,String colorPrefix) {
        return text.replace(colorPrefix,"§").replace(colorPrefix + colorPrefix,"§");
    }
}