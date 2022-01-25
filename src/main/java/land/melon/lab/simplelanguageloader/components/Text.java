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

public final class Text {
    public static final TextSerializer gsonSerializer = new TextSerializer();
    private static final Pattern placeholderPattern = Pattern.compile("\\{\\w*}");
    private final List<String> textOriginal;
    private final List<String> textExpanded;
    private final List<String> textColored;
    private final List<List<BaseComponent>> textComponents;
    private final Map<String, List<Pair<Integer, Integer>>> placeHolderLocMap = new HashMap<>();

    public Text(String... texts) {
        textOriginal = new ArrayList<>(texts.length);
        textExpanded = new ArrayList<>(texts.length);
        textColored = new ArrayList<>(texts.length);
        textComponents = new ArrayList<>(texts.length);
        Arrays.stream(texts).forEach(this::addTextLine);
    }

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

    public List<String> originalAsList() {
        return textOriginal;
    }

    public String original() {
        return String.join("\n", originalAsList());
    }

    public List<String> expandedAsList() {
        return textExpanded;
    }

    public String expanded() {
        return String.join("\n", expandedAsList());
    }

    public List<String> coloredAsList() {
        return textColored;
    }

    public String colored() {
        return String.join("\n", coloredAsList());
    }

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

    @SafeVarargs
    public final BaseComponent[] produceWithBaseComponentsAsArray(Pair<String, Object>... pairs) {
        var array = new BaseComponent[textComponents.size()];
        return produceWithBaseComponentsAsList(pairs).toArray(array);
    }

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


    @SafeVarargs
    public final String produce(Pair<String, Object>... pairs) {
        var result = colored();
        for (var pair : pairs) {
            result = result.replace("{" + pair.key() + "}", pair.value().toString());
        }
        return result;
    }

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
