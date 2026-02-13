package settingdust.lazyyyyy.faster_mixin.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PathHashCache {
    private static final ILogger LOGGER = MixinService.getService().getLogger("Lazyyyyy/MixinCache/PathHash");
    private static final Gson GSON = new Gson();

    private final Path cacheFile;
    private final Map<String, PathHashEntry> cache;

    public PathHashCache(Path cacheFile) {
        this.cacheFile = cacheFile;
        this.cache = load();
    }

    private Map<String, PathHashEntry> load() {
        if (!Files.exists(cacheFile)) return new HashMap<>();
        try (var reader = new InputStreamReader(new GZIPInputStream(Files.newInputStream(cacheFile)))) {
            return GSON.fromJson(reader, new TypeToken<Map<String, PathHashEntry>>() {
            }.getType());
        } catch (IOException e) {
            LOGGER.warn("Failed to load path hash cache", e);
            return new HashMap<>();
        }
    }

    private void save() {
        try (var writer = new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(cacheFile)))) {
            GSON.toJson(cache, writer);
        } catch (IOException e) {
            LOGGER.warn("Failed to save path hash cache", e);
        }
    }

    public byte[] getFileHash(Path path) {
        try {
            var lastModified = Files.getLastModifiedTime(path).toMillis();
            var entry = cache.get(path.toAbsolutePath().toString());
            if (entry != null && entry.lastModified == lastModified) {
                return HashCode.fromString(entry.hash).asBytes();
            }

            var hasher = Hashing.sha256().newHasher();
            try (var stream = Files.newInputStream(path)) {
                var buffer = new byte[8192];
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    hasher.putBytes(buffer, 0, read);
                }
            }
            var hash = hasher.hash();
            entry = new PathHashEntry();
            entry.lastModified = lastModified;
            entry.hash = hash.toString();
            cache.put(path.toAbsolutePath().toString(), entry);
            save();
            return hash.asBytes();
        } catch (IOException e) {
            LOGGER.warn("Failed to calculate path hash for {}", path, e);
            return new byte[0];
        }
    }

    private static class PathHashEntry {
        long lastModified;
        String hash;
    }
}
