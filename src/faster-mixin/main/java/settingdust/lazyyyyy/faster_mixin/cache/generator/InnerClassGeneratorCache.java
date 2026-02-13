package settingdust.lazyyyyy.faster_mixin.cache.generator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import settingdust.lazyyyyy.faster_mixin.util.MixinInternals;
import settingdust.lazyyyyy.faster_mixin.util.accessor.InnerClassGeneratorReflection;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InnerClassGeneratorCache implements GeneratorCache {
    private static final Gson GSON = new Gson();
    public static final Map<String, InfoData> CAPTURED_INFOS = new ConcurrentHashMap<>();
    private static volatile boolean dirty = false;

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void load(Path baseDir) throws IOException {
        Path cacheFile = baseDir.resolve("inner-class-map.json");
        if (!Files.exists(cacheFile)) return;

        try (Reader reader = Files.newBufferedReader(cacheFile)) {
            var data = GSON.fromJson(
                    reader, new TypeToken<Map<String, InfoData>>() {
                    });
            if (data == null) return;

            data.forEach((name, info) -> {
                var mixin = MixinInternals.getMixin(info.mixinName());
                if (mixin != null)
                    InnerClassGeneratorReflection.registerInnerClass(mixin, ClassInfo.forName(info.targetName()), name);
            });
        }
    }

    @Override
    public void save(Path baseDir) throws IOException {
        try (Writer writer = Files.newBufferedWriter(baseDir.resolve("inner-class-map.json"))) {
            GSON.toJson(CAPTURED_INFOS, writer);
        }
        dirty = false;
    }

    public record InfoData(String mixinName, String targetName) {
    }
}
