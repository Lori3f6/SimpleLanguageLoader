package land.melon.lab.simplelanguageloader.components;

import com.google.gson.*;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Language component that represents a text, which help you to create single or multi line customizable text with basic and RGB color code, format code, custom placeholders and Spigot BaseComponents support.
 *
 * <p>Text integrates both minecraft classic color/format codes and RGB color codes support.</p>
 * <p>You could use ampersand sign(&amp;) to select colors.</p>
 * <p>For example:</p>
 * <pre>
 *     var coloredText = Text.of(
 *          "&amp;cRoses are red,",
 *          "&amp;#66ccffViolets are blue.",
 *          "&amp;fSugar is sweet,",
 *          "&amp;#c75f78And so are you."
 *     );
 * </pre>
 * <p>Format codes are also available:</p>
 * <pre>
 *     var textFormatted = Text.of(
 *     "&amp;oTo be, or not to be, "
 *     "&amp;nthat is &amp;lthe&amp;r&amp;n question."
 *     );
 * </pre>
 *
 * <p>You could create custom placeholders, and use it as below:</p>
 * <pre>
 *     var textWithPlaceholders = Text.of(
 *          "Roses are red,",
 *          "{blueThings} are blue,",
 *          "{thirdSentence}"
 *     );
 *
 *     //assume that there is a bukkit Player assigned to variable player
 *     player.sendMessage(textWithPlaceholders.produce(
 *          Pair.of("blueThings", "The Smurfs"),
 *          Pair.of("thirdSentence", "Unexpected '{' on line 32.")
 *     ));
 *     // Roses are red, The Smurfs are blue, Unexpected '{' on line 32.
 * </pre>
 *
 * <p>You could also use BaseComponents to customize the content of placeholders advanced:</p>
 * <pre>
 *     var textWithPlaceholders = Text.of(
 *          "Roses are red,",
 *          "The Smurfs are blue,",
 *          "{thirdSentence}"
 *     );
 *
 *     var myBaseComponent = new TextComponent("My customized BaseComponent looks pretty cool.");
 *     //set the properties
 *     myBaseComponent.set...;
 *     myBaseComponent.add...;
 *     //assume that there is a bukkit Player assigned to variable player
 *     player.spigot().sendMessage(textWithPlaceholders.produceWithBaseComponentsAsArray(
 *          Pair.of("thirdSentence", myBaseComponent)
 *     ));
 *     // Roses are red,
 *     // The Smurfs are blue,
 *     // My customized BaseComponent looks pretty cool.(with properties)
 * </pre>
 */
public final class Text {
    /**
     * The serializer/deserializer for gson to process class Text.
     */
    public static final TextSerializer gsonSerializer = new TextSerializer();
    private static final Pattern placeholderPattern = Pattern.compile("\\{\\w*}");
    private final List<String> textOriginal;
    private final List<String> textExpanded;
    private final List<String> textColored;
    private final BaseComponent componentLines = new TextComponent("");
    private final Map<String, List<Integer>> placeHolderMap = new HashMap<>();

    /**
     * Create a new Text component with the given text.
     *
     * @param texts the texts to be used
     */
    public Text(String... texts) {
        textOriginal = new ArrayList<>(texts.length);
        textExpanded = new ArrayList<>(texts.length);
        textColored = new ArrayList<>(texts.length);
        Arrays.stream(texts).forEach(this::addTextLine);
    }

    /**
     * Create Text from {@link JsonElement}
     * @param jsonElement jsonElement used to deserialize to Text
     */
    private Text(JsonElement jsonElement) {
        int arraySize;
        if (jsonElement.isJsonPrimitive()) {
            arraySize = 1;
        } else if (jsonElement.isJsonArray()) {
            arraySize = jsonElement.getAsJsonArray().size();
        } else {
            throw new RuntimeException("Invalid text element in your language file, only String and Array<String> are allowed.");
        }

        textOriginal = new ArrayList<>(arraySize);
        textExpanded = new ArrayList<>(arraySize);
        textColored = new ArrayList<>(arraySize);

        if (jsonElement.isJsonPrimitive()) {
            addTextLine(jsonElement.getAsString());
        } else {
            jsonElement.getAsJsonArray().forEach(e -> addTextLine(e.getAsString()));
        }
    }

    /**
     * Create a new Text component with the given text.
     * <p>Shorthand for {@link #Text(String...)}</p>
     *
     * @param texts the text to be used
     * @return new Text instance
     */
    public static Text of(String... texts) {
        return new Text(texts);
    }

    private void addTextLine(String originalString) {
        textOriginal.add(originalString);
        var expanded = ColorConverter.convertConvenientColorCode(originalString);
        textExpanded.add(expanded);
        var colored = expanded.replace('&', '§').replace("§§", "&");
        textColored.add(colored);

        if (componentLines.getExtra() != null) {
            componentLines.addExtra(new TextComponent("\n"));
        }
        var textsInLine = placeholderPattern.split(colored);
        var placeholdersInLine = placeholderPattern.matcher(colored).results().map(MatchResult::group).toArray(String[]::new);
        if (placeholdersInLine.length > 0) {
            for (int i = 0; i < placeholdersInLine.length; i++) {
                var text = TextComponent.fromLegacyText(textsInLine[i]);
                if (!textsInLine[i].startsWith("§")) {
                    text[0].copyFormatting(componentLines.getExtra().get(componentLines.getExtra().size() - 1));
                }
                Arrays.stream(text).forEach(componentLines::addExtra);

                var placeholder = new TextComponent(placeholdersInLine[i]);
                placeholder.copyFormatting(componentLines.getExtra().get(componentLines.getExtra().size() - 1));
                componentLines.addExtra(placeholder);

                var placeholderString = placeholdersInLine[i].substring(1, placeholdersInLine[i].length() - 1);
                if (!placeHolderMap.containsKey(placeholderString)) {
                    placeHolderMap.put(placeholderString, new ArrayList<>());
                }
                var indexList = placeHolderMap.get(placeholderString);
                indexList.add(componentLines.getExtra().size() - 1);
            }
            if (textsInLine.length > placeholdersInLine.length) {
                var componentPart = TextComponent.fromLegacyText(textsInLine[placeholdersInLine.length]);
                if (!textsInLine[placeholdersInLine.length].startsWith("§")) {
                    componentPart[0].copyFormatting(componentLines.getExtra().get(componentLines.getExtra().size() - 1));
                }
                Arrays.stream(componentPart).forEach(componentLines::addExtra);
            }
        } else {
            Arrays.stream(TextComponent.fromLegacyText(colored)).forEach(componentLines::addExtra);
        }
    }

    /**
     * Get the original text (which used to create Text instance) as a List, each element is a single line.
     *
     * @return original texts
     */
    public List<String> originalAsList() {
        return textOriginal;
    }

    /**
     * Get the original text (which used to create Text instance), joint to one string.
     *
     * @return the original texts
     */
    public String original() {
        return String.join("\n", originalAsList());
    }

    /**
     * Get the expanded text (which expands the color code in patterns looks like <code>&amp;#66ccff</code> to classic form <code>&amp;x&amp;6&amp;6&amp;c&amp;c&amp;f&amp;f</code>), each element is a single line.
     *
     * @return the expanded texts
     */
    public List<String> expandedAsList() {
        return textExpanded;
    }

    /**
     * Get the expanded text (which expands the color code in patterns looks like <code>&amp;#66ccff</code> to classic form <code>&amp;x&amp;6&amp;6&amp;c&amp;c&amp;f&amp;f</code>), joint to one string.
     *
     * @return the expanded texts
     */
    public String expanded() {
        return String.join("\n", expandedAsList());
    }

    /**
     * Get the colored text (which replaces ampersand sign(&amp;) to section sign(§)), each element is a single line.
     *
     * @return the colored texts
     */
    public List<String> coloredAsList() {
        return textColored;
    }

    /**
     * Get the colored text (which replaces ampersand sign(&amp;) to section sign(§)), joint to one string.
     *
     * @return the colored texts
     */
    public String colored() {
        return String.join("\n", coloredAsList());
    }

    /**
     * Get the text replaced placeholders, each element is a single line.
     *
     * @param pairs the pairs of placeholder and its value
     * @return the texts replaced placeholders
     */
    @SafeVarargs
    public final List<String> produceAsList(Pair<String, Object>... pairs) {
        List<String> result = new ArrayList<>(textColored.size());
        coloredAsList().forEach(text -> {
            for (var pair : pairs) {
                result.add(text.replace("{" + pair.key() + "}", preProcess(pair.value()).toString()));
            }
        });
        return result;
    }

    /**
     * Get the text replaced placeholders, joint to one string.
     *
     * @param pairs the pairs of placeholder and its value
     * @return the texts replaced placeholders
     */
    @SafeVarargs
    public final BaseComponent produceWithBaseComponent(Pair<String, Object>... pairs) {
        for (var pair : pairs) {
            if (!placeHolderMap.containsKey(pair.key())) {
                continue;
            }
            var placeholder = placeHolderMap.get(pair.key());
            for (var index : placeholder) {
                if (pair.value() instanceof BaseComponent) {
                    componentLines.getExtra().set(index, (BaseComponent) pair.value());
                } else {
                    var component = new TextComponent(TextComponent.fromLegacyText(preProcess(pair.value()).toString()));
                    component.copyFormatting(componentLines.getExtra().get(index));
                    componentLines.getExtra().set(index, component);
                }
            }
        }
        return componentLines.duplicate();
    }

    /**
     * Get the text replaced placeholders, joint to one string.
     *
     * @param pairs the pairs of placeholder and its value
     * @return the texts replaced placeholders
     */
    @SafeVarargs
    public final String produce(Pair<String, Object>... pairs) {
        var result = colored();
        for (var pair : pairs) {
            result = result.replace("{" + pair.key() + "}", preProcess(pair.value()).toString());
        }
        return result;
    }

    /**
     * Get the text replaced placeholders, joint to one string.
     * <p>Equals to {@link #colored()}</p>
     *
     * @return the texts replaced placeholders
     */
    @Override
    public String toString() {
        return colored();
    }

    private static class TextSerializer implements JsonSerializer<Text>, JsonDeserializer<Text> {
        @Override
        public JsonElement serialize(Text textInstance, Type type, JsonSerializationContext jsonSerializationContext) {
            if (textInstance.textOriginal.size() == 0) {
                return new JsonPrimitive("");
            } else if (textInstance.textOriginal.size() == 1) {
                return new JsonPrimitive(textInstance.textOriginal.get(0));
            } else {
                var textArray = new JsonArray(textInstance.textOriginal.size());
                for (var text : textInstance.textOriginal) {
                    textArray.add(text);
                }
                return textArray;
            }
        }

        @Override
        public Text deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new Text(jsonElement);
        }
    }

    private Object preProcess(Object object) {
        if(object instanceof Double || object instanceof Float) {
            // format as 2 decimal places BigDecimal
            return BigDecimal.valueOf(((Number) object).doubleValue()).setScale(2, RoundingMode.DOWN);
        } else
            return object;
    }
}
