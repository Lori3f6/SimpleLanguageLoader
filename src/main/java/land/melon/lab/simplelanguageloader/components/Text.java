package land.melon.lab.simplelanguageloader.components;

import com.google.gson.*;
import land.melon.lab.simplelanguageloader.utils.ColorConverter;

import java.lang.reflect.Type;

public class Text {
    public static final TextSerializer gsonSerializer = new TextSerializer();
    private final String textOriginal;
    private final String textExpanded;
    private final String textColored;

    public Text(String text) {
        this.textOriginal = text;
        textExpanded = ColorConverter.convertConvenientColorCode(text);
        textColored = textExpanded.replace("&", "§").replace("§§", "&");
    }

    public String original() {
        return textOriginal;
    }

    public String expanded() {
        return textExpanded;
    }

    public String colored() {
        return textColored;
    }

    private static class TextSerializer implements JsonSerializer<Text>, JsonDeserializer<Text> {
        @Override
        public JsonElement serialize(Text text, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(text.original());
        }

        @Override
        public Text deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new Text(jsonElement.getAsString());
        }
    }
}
