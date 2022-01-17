package land.melon.lab.simplelanguageloader.utils;


import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ColorConverter {
    static Pattern hexColorPattern = Pattern.compile("[&][#][0-9A-Fa-f]{6}");

    public static String convertConvenientColorCode(String text) {
        var textParts = hexColorPattern.split(text);
        var colorCodes = hexColorPattern.matcher(text).results().map(MatchResult::group).toArray(String[]::new);
        var textBuilder = new StringBuilder(textParts[0]);
        for (int i = 0; i < colorCodes.length; i++) {
            textBuilder.append(convertToTraditionalColorCode(colorCodes[i])).append(textParts[i + 1]);
        }
        return textBuilder.toString();
    }

    private static String convertToTraditionalColorCode(String x) {
        var hexColorCode = x.substring(2);
        var colorCodeBuilder = new StringBuilder("&x");
        hexColorCode.chars().forEach(c -> colorCodeBuilder.append("&").append((char) c));
        return colorCodeBuilder.toString();
    }
}