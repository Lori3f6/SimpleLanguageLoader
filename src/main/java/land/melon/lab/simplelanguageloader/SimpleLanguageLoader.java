package land.melon.lab.simplelanguageloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.components.Text;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Functional main class of SimpleLanguageLoader package.
 * <p>SimpleLanguageLoader is a gson based language file loader, which provides a simple way to create, load and save language files.</p>
 * <p>To get Start, you need to create a new Class as the container for your texts, which uses {@link Text} as member variables for the text sections.</p>
 * <p>Then, you can create Text instance with the default value of your language sections.</p>
 * <p>For example, you could create a class named <code>MyLanguage</code> as follows:</p>
 * <pre>
 *      // MyLanguage.java
 *      package ...;
 *      import land.melon.lab.simplelanguageloader.components.Text;
 *
 *      public class MyLanguage {
 *          // create single line text section with Text
 *          // the parameter of Text is the default value of the text section
 *          public Text myText = new Text("The default value of this section of single line text.");
 *
 *          //Text can also be used to create a multi line text section, using String varargs as parameters.
 *          public Text myTextMultiLine = new Text("line1","line2");
 *
 *          //Text.of() is a shorthand for create new Text instance, which can used as follows:
 *          public Text myTextOf = Text.of("Single line text");
 *          public Text myTextOfMultiLine = Text.of("line1","line2");
 *      }
 *  </pre>
 * <p>Then, you may need to create a language file during program setup stage from the class we create above as follows:</p>
 * <pre>
 *      SimpleLanguageLoader loader = new SimpleLanguageLoader();
 *      //reusable, you could create a SimpleLanguageLoader in static field and use it anywhere.
 *      var myLanguage = new MyLanguage();
 *      var languageFile = new File(...);
 *      loader.saveLanguageFile(myLanguage, languageFile);
 *  </pre>
 * <p>The value you used to initialize Text in <code>MyLanguage</code> class will be saved to <code>languageFile</code> as default values in each text section.</p>
 * <p>The single line text will be saved as a String while multiline text will be saved as an JsonArray of String.</p>
 * <p>Then, you can load the language file and use it as follows:</p>
 * <pre>
 *      SimpleLanguageLoader loader = new SimpleLanguageLoader();
 *      var languageFile = new File(...);
 *
 *      //load language file as below
 *      var myLanguage = loader.loadLanguageFile(languageFile, MyLanguage.class);
 *
 *      //access the texts as below
 *      System.out.println(myLanguage.myText.produce());
 *
 *      //assume that there is a bukkit Player assigned to variable player
 *      player.sendMessage(myLanguage.myText.produce());
 *  </pre>
 * <p>That's all the basic uses of SimpleLanguageLoader, You could see the other classes and methods in javadoc for more details.</p>
 * <p>By the way, SimpleLanguageLoader is a <a href="https://github.com/google/gson">Gson</a>(<a href="https://www.javadoc.io/doc/com.google.code.gson/gson">Gson Javadoc</a>) based object serializer/deserializer, so the operations and annotations used in a gson data class are also usable in SimpleLanguageLoader. You could customize the gson instance inside SimpleLanguageLoader instance by creating new SimpleLanguageLoader instance via <code>new SimpleLanguageLoader(GsonBuilder)</code> method.</p>
 * <p>@see Text</p>
 */
public class SimpleLanguageLoader {
    private final Gson gson;

    /**
     * Create a new SimpleLanguageLoader instance with default Gson instance.
     * <p>The default gson settings are: <code>PrettyPrinting</code>, <code>Lenient</code>, <code>DisableHtmlEscaping</code>.</p>
     */
    public SimpleLanguageLoader() {
        this(new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping());
    }

    /**
     * Create a new SimpleLanguageLoader instance with specified Gson instance.
     *
     * @param gsonBuilder customized GsonBuilder to create Gson instance
     */
    public SimpleLanguageLoader(GsonBuilder gsonBuilder) {
        this(gsonBuilder, false);
    }

    /**
     * Create a new SimpleLanguageLoader instance with specified Gson instance.
     *
     * @param gsonBuilder          customized GsonBuilder to create Gson instance
     * @param applyDefaultSettings apply <code>PrettyPrinting</code>, <code>Lenient</code> and <code>DisableHtmlEscaping</code>.
     */
    public SimpleLanguageLoader(GsonBuilder gsonBuilder, boolean applyDefaultSettings) {
        var builder = gsonBuilder.registerTypeAdapter(Text.class, Text.gsonSerializer);
        if (applyDefaultSettings) {
            builder.setPrettyPrinting().setLenient().disableHtmlEscaping();
        }
        this.gson = builder.create();
    }


    /**
     * get the Gson instance used in this SimpleLanguageLoader instance.
     *
     * @return Gson instance
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Serialize an object to Json string.
     *
     * @param object object to be serialized
     * @return serialized string
     */
    public String getJsonString(Object object) {
        return gson.toJson(object);
    }

    /**
     * Deserialize a Json string to an object.
     * <p>Equals to {@link #loadFromString(String, Type)}</p>
     *
     * @param json Json string to be deserialized
     * @param type type of the object
     * @param <T>  type of the object
     * @return deserialized object
     */
    public <T> T loadFromString(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * Deserialize a Json string to an object.
     *
     * @param json Json string to be deserialized
     * @param type type of the object
     * @param <T>  type of the object
     * @return deserialized object
     */
    public <T> T loadFromString(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Read json from a file, and deserialize it to an Object.
     * <p>Equals to {@link #loadFromFile(File, Type)}</p>
     *
     * @param file file to be read
     * @param type type of the object
     * @param <T>  type of the object
     * @return deserialized object
     * @throws IOException if an I/O error occurs
     */
    public <T> T loadFromFile(File file, Class<T> type) throws IOException {
        return loadFromFile(file, (Type) type);
    }

    /**
     * Read json from a file, and deserialize it to an Object.
     *
     * @param file file to be read
     * @param type type of the object
     * @param <T>  type of the object
     * @return deserialized object
     * @throws IOException if an I/O error occurs
     */
    public <T> T loadFromFile(File file, Type type) throws IOException {
        IGNORE_RESULT(file.createNewFile());
        var inputStream = new FileInputStream(file);
        return gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), type);
    }

    /**
     * Serialize an object to String, and save it to a file.
     *
     * @param file   file to be saved
     * @param object object to be serialized
     * @throws IOException if an I/O error occurs
     */
    public void saveToFile(File file, Object object) throws IOException {
        var jsonString = this.getJsonString(object);
        var outputStream = new FileOutputStream(file);
        outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    /**
     * <p>attempt to load an object from file</p>
     * <p>create new file and return value constructed by supplier if the file doesn't exist or file is empty.</p>
     *
     * @param file     file to be read
     * @param type     type of the object
     * @param <T>      type of the object
     * @param supplier default constructor
     * @return deserialized object
     * @throws IOException if the file is corrupted or cannot be read
     */
    public <T> T loadOrConstruct(File file, Type type, Supplier<T> supplier) throws IOException {
        T attempt = loadFromFile(file, type);
        return attempt != null ? attempt : supplier.get();
    }

    /**
     * <p>attempt to load an object from file</p>
     * <p>create new file and return value constructed by supplier if the file doesn't exist or file is empty.</p>
     *
     * @param file     file to be read
     * @param type     type of the object
     * @param <T>      type of the object
     * @param supplier default constructor
     * @return deserialized object
     * @throws IOException if the file is corrupted or cannot be read
     */
    public <T> T loadOrConstruct(File file, Class<T> type, Supplier<T> supplier) throws IOException {
        return loadOrConstruct(file, (Type) type, supplier);
    }

    /**
     * <p>attempt to load an object from file</p>
     * <p>create new file and return value constructed by supplier</p>
     * <p>write the serialized object to file immediately while loading</p>
     *
     * @param file     file to be read
     * @param type     type of the object
     * @param supplier default constructor
     * @return deserialized object
     * @throws IOException if the file is corrupted or cannot be read
     */
    public <T> T loadOrInitialize(File file, Class<T> type, Supplier<T> supplier) throws IOException {
        var loaded = loadOrConstruct(file, type, supplier);
        saveToFile(file, loaded);
        return loaded;
    }


    /**
     * <p>attempt to load an object from file</p>
     * <p>create new file and return value constructed by supplier</p>
     * <p>write the serialized object to file immediately while loading</p>
     *
     * @param file     file to be read
     * @param type     type of the object
     * @param supplier default constructor
     * @return deserialized object
     * @throws IOException if the file is corrupted or cannot be read
     */
    public <T> T loadOrInitialize(File file, Type type, Supplier<T> supplier) throws IOException {
        var loaded = loadOrConstruct(file, type, supplier);
        saveToFile(file, loaded);
        return loaded;
    }

    @SuppressWarnings("unused")
    private void IGNORE_RESULT(Object o) {
        //ignored
    }

}
