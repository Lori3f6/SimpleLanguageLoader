package land.melon.lab.simplelanguageloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.components.Text;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleLanguageLoader {
    private final Gson gson;

    public SimpleLanguageLoader() {
        this(new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping());
    }

    public SimpleLanguageLoader(GsonBuilder gsonBuilder) {
        this.gson = gsonBuilder.registerTypeAdapter(Text.class, Text.gsonSerializer).create();
    }

    public Gson getGson() {
        return gson;
    }

    public <T> T loadLanguageFile(File languageFile, Class<T> type) throws IOException {
        IGNORE_RESULT(languageFile.createNewFile());
        return gson.fromJson(new FileReader(languageFile), type);
    }

    public void saveLanguageFile(File languageFile, Object language) throws IOException {
        var jsonString = gson.toJson(language);
        var writer = new FileWriter(languageFile);
        writer.write(jsonString);
        writer.close();
    }

    @SuppressWarnings("unused")
    private void IGNORE_RESULT(Object o) {
        //ignored
    }

}
