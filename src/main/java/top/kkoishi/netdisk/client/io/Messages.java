package top.kkoishi.netdisk.client.io;

import top.kkoishi.json.JsonElement;
import top.kkoishi.json.Kson;
import top.kkoishi.json.io.JsonReader;
import top.kkoishi.json.reflect.TypeResolver;
import top.kkoishi.netdisk.client.Context;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public final class Messages {
    private final Map<String, String> messages = new LinkedHashMap<>();

    private Messages (Locale locale, Context context) throws IOException {
        final Kson kson = new Kson();
        final String path = "./data/localizations/messages_" + locale.toLanguageTag() + ".json";
        final JsonReader reader = new JsonReader(new FileReader(path, StandardCharsets.UTF_8));
        final JsonElement ele = reader.read();
        reader.close();
        messages.putAll(kson.fromJson(new TypeResolver<LinkedHashMap<String, String>>() {
        }, ele));
        context.set(Messages.class, this);
    }

    public void changeLocale (Locale nLocale) throws IOException {
        final Kson kson = new Kson();
        final String path = "./data/localizations/messages_" + nLocale.toLanguageTag() + ".json";
        final JsonReader reader = new JsonReader(new FileReader(path, StandardCharsets.UTF_8));
        final JsonElement ele = reader.read();
        reader.close();
        messages.putAll(kson.fromJson(new TypeResolver<LinkedHashMap<String, String>>() {
        }, ele));
    }

    public String getMessage (String key, Object... args) {
        return format(messages.get(key), args);
    }

    private String format (String pattern, Object... args) {
        if (pattern == null) {
            return "null";
        }
        return MessageFormat.format(pattern, args);
    }

    public static Messages instance(Context context) throws IOException {
        Messages inst = context.get(Messages.class);
        if (inst == null) {
            inst = new Messages(Locale.getDefault(), context);
        }
        return inst;
    }
}
