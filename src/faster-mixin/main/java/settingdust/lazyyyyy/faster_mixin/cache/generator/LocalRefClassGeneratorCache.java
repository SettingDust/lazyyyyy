package settingdust.lazyyyyy.faster_mixin.cache.generator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRefClassGeneratorCache implements GeneratorCache {
    private static final Gson GSON = new Gson();
    public static final Set<String> CAPTURED_INFOS = ConcurrentHashMap.newKeySet();
    private static volatile boolean dirty = false;

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void load(Path baseDir) throws IOException {
        Path cacheFile = baseDir.resolve("local-ref-map.json");
        if (!Files.exists(cacheFile)) return;

        try (Reader reader = Files.newBufferedReader(cacheFile)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element == null || element.isJsonNull()) return;

            Set<String> data = GSON.fromJson(element, new TypeToken<Set<String>>() {}.getType());
            if (data == null) return;
            CAPTURED_INFOS.addAll(data);
        }
    }

    public static void replay() {
        if (CAPTURED_INFOS.isEmpty()) return;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            throw new RuntimeException("Context class loader is null");
        }

        Class<?> generatorClass;
        try {
            generatorClass = Class.forName(
                    "com.llamalad7.mixinextras.sugar.impl.ref.LocalRefClassGenerator",
                    false,
                    contextClassLoader
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("LocalRefClassGenerator not found in context class loader", e);
        }

        Method getForType;
        try {
            getForType = generatorClass.getDeclaredMethod("getForType", Type.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("LocalRefClassGenerator.getForType(Type) not found", e);
        }

        for (String desc : CAPTURED_INFOS) {
            try {
                getForType.invoke(null, Type.getType(desc));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke LocalRefClassGenerator.getForType(Type)", e);
            }
        }
    }

    @Override
    public void save(Path baseDir) throws IOException {
        try (Writer writer = Files.newBufferedWriter(baseDir.resolve("local-ref-map.json"))) {
            GSON.toJson(CAPTURED_INFOS, writer);
        }
        dirty = false;
    }
}
