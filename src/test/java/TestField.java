import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.components.Text;
import land.melon.lab.simplelanguageloader.utils.Pair;

import javax.print.attribute.standard.JobMediaSheets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class TestField {
    public static void main(String[] args) {
        var gson = new GsonBuilder().registerTypeAdapter(Text.class, Text.gsonSerializer).setPrettyPrinting().disableHtmlEscaping().setLenient().create();
        var text = new Text(
                "&#66ccff Hello {color} world!",
                "another message",
                "and again");
        var textSingleLine = new Text("&#ff9900 Hello colorful world!");
        var json = gson.toJson(text);
        var json2 = gson.toJson(textSingleLine);
        System.out.println(json);
        System.out.println(json2);
        var text2 = gson.fromJson(json, Text.class);
        var textsl2 = gson.fromJson(json2, Text.class);
        var jsonAgain = gson.toJson(text2);
        var jsonAgain2 = gson.toJson(textsl2);
        System.out.println(jsonAgain);
        System.out.println(jsonAgain2);

        var listA = new ArrayList<String>();
        var mapA = new HashMap<String,List<String>>();
        listA.add("a");
        mapA.put("listA",listA);
        listA.add("wow");
        System.out.println(mapA);


        Pattern placeholderPattern = Pattern.compile("\\{\\w*}");
        var str1 = "{color} apple!";
        var str = "wow, {something}";
        var str3 = "plain text";
        var spl1 = placeholderPattern.split(str);
        var spl = placeholderPattern.split(str1);
        var spl3 = placeholderPattern.split(str3);
        var spl3placeholders = placeholderPattern.matcher(str3).results().map(m -> m.group(0)).toArray(String[]::new);
    }
}
