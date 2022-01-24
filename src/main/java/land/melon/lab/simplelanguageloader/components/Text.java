package land.melon.lab.simplelanguageloader.components;

import com.google.gson.*;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;
import land.melon.lab.simplelanguageloader.utils.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Text {
    public static final TextSerializer gsonSerializer = new TextSerializer();
    private final List<String> textOriginal;
    private final List<String> textExpanded;
    private final List<String> textColored;

    public Text(String... texts) {
        textOriginal = new ArrayList<>(texts.length);
        textExpanded = new ArrayList<>(texts.length);
        textColored = new ArrayList<>(texts.length);
        Arrays.stream(texts).forEach(this::addTextItem);
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

        if (jsonElement.isJsonPrimitive()) {
            addTextItem(jsonElement.getAsString());
        } else {
            jsonElement.getAsJsonArray().forEach(e -> addTextItem(e.getAsString()));
        }
    }

    public static Text of(String... texts) {
        return new Text(texts);
    }

    private void addTextItem(String originalString) {
        textOriginal.add(originalString);
        var expanded = ColorConverter.convertConvenientColorCode(originalString);
        textExpanded.add(expanded);
        textColored.add(expanded.replace('&', '§').replace("§§", "&"));
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
