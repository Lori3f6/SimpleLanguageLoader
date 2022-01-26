package land.melon.lab.simplelanguageloader.components;

import com.google.gson.*;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.lang.reflect.Type;
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
    private final List<List<BaseComponent>> textComponents;
    private final Map<String, List<Pair<Integer, Integer>>> placeHolderLocMap = new HashMap<>();

    /**
     * Create a new Text component with the given text.
     *
     * @param texts the texts to be used
     */
    public Text(String... texts) {
        textOriginal = new ArrayList<>(texts.length);
        textExpanded = new ArrayList<>(texts.length);
        textColored = new ArrayList<>(texts.length);
        textComponents = new ArrayList<>(texts.length);
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
        textComponents = new ArrayList<>(arraySize);

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

        var line = new ArrayList<BaseComponent>();
        var texts = placeholderPattern.split(colored);
        var placeholders = placeholderPattern.matcher(colored).results().map(MatchResult::group).toArray(String[]::new);
        if (texts.length > 1) {
            line.add(new TextComponent(texts[0]));
            for (int i = 1; i < texts.length; i++) {
                var placeholderString = placeholders[i - 1].substring(1, placeholders[i - 1].length() - 1);
                line.add(new TextComponent(placeholderString));
                line.add(new TextComponent(texts[i]));
                var placeHolderLocations = placeHolderLocMap.getOrDefault(placeholderString, new ArrayList<>());
                placeHolderLocations.add(new Pair<>(textComponents.size(), i * 2 - 1));
                placeHolderLocMap.put(placeholderString, placeHolderLocations);
            }
        } else {
            line.add(new TextComponent(colored));
        }
        textComponents.add(line);
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
                result.add(text.replace("{" + pair.key() + "}", pair.value().toString()));
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
    public final List<BaseComponent> produceWithBaseComponentsAsList(Pair<String, Object>... pairs) {
        List<BaseComponent> result = new ArrayList<>(textComponents.size());
        var unReplaced = new ArrayList<>(textComponents);
        for (var pair : pairs) {
            if (!placeHolderLocMap.containsKey(pair.key())) {
                continue;
            }
            var placeHolderLocations = placeHolderLocMap.get(pair.key());
            for (var placeHolderLocation : placeHolderLocations) {
                var line = unReplaced.get(placeHolderLocation.key());
                if (pair.value() instanceof BaseComponent) {
                    line.set(placeHolderLocation.value(), (BaseComponent) pair.value());
                } else {
                    line.set(placeHolderLocation.value(), new TextComponent(pair.value().toString()));
                }
            }
        }

        for (var line : unReplaced) {
            BaseComponent baseComponent = null;
            for (var component : line) {
                if (baseComponent == null) {
                    baseComponent = component;
                } else {
                    baseComponent.addExtra(component);
                }
            }
            result.add(baseComponent);
        }
        return result;
    }

    /**
     * Get the text replaced placeholders(with BaseComponent) as a BaseComponent Array, each element is a single line.
     *
     * @param pairs the pairs of placeholder and its value
     * @return the texts replaced placeholders
     */
    @SafeVarargs
    public final BaseComponent[] produceWithBaseComponentsAsArray(Pair<String, Object>... pairs) {
        var array = new BaseComponent[textComponents.size()];
        return produceWithBaseComponentsAsList(pairs).toArray(array);
    }

    /**
     * Get the text replaced placeholders as a BaseComponent.
     *
     * @param pairs the pairs of placeholder and its value
     * @return the text replaced placeholders with BaseComponent
     */
    @SafeVarargs
    public final BaseComponent produceWithBaseComponents(Pair<String, Object>... pairs) {
        var componentsList = produceWithBaseComponentsAsList(pairs);
        var base = componentsList.get(0);
        for (int i = 1; i < componentsList.size(); i++) {
            base.addExtra("\n");
            base.addExtra(componentsList.get(i));
        }
        return base;
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
            result = result.replace("{" + pair.key() + "}", pair.value().toString());
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
}
