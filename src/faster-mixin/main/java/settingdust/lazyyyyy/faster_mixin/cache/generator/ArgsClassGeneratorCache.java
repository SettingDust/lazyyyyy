package settingdust.lazyyyyy.faster_mixin.cache.generator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgsClassGenerator;
import settingdust.lazyyyyy.faster_mixin.util.MixinInternals;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArgsClassGeneratorCache implements GeneratorCache {
    private static final Gson GSON = new Gson();
    public static final Map<String, String> CAPTURED_INFOS = new ConcurrentHashMap<>();
    private static volatile boolean dirty = false;

    private final ArgsClassGenerator generator;

    public ArgsClassGeneratorCache() {
        this.generator = MixinInternals.getExtensions().getGenerator(ArgsClassGenerator.class);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void load(Path baseDir) throws IOException {
        Path cacheFile = baseDir.resolve("args-class-map.json");
        if (!Files.exists(cacheFile)) return;

        try (Reader reader = Files.newBufferedReader(cacheFile)) {
            var data = GSON.fromJson(
                    reader, new TypeToken<Map<String, String>>() {
                    });
            if (data == null) return;

            data.forEach((desc, info) -> {
                var mixin = MixinInternals.getMixin(info);
                if (mixin != null) {
                    generator.getArgsClass(desc, mixin);
                }
            });
        }
    }

    @Override
    public void save(Path baseDir) throws IOException {
        try (Writer writer = Files.newBufferedWriter(baseDir.resolve("args-class-map.json"))) {
            GSON.toJson(CAPTURED_INFOS, writer);
        }
        dirty = false;
    }
}
