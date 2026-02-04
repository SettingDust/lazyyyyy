package settingdust.lazyyyyy.faster_mixin.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.transformers.MixinClassWriter;
import settingdust.lazyyyyy.faster_mixin.util.LazyyyyyEarlyConfigInvoker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MixinCacheManager {
    private static final ILogger LOGGER = MixinService.getService().getLogger("Lazyyyyy/MixinCache");
    private static final Path CACHE_PATH = Paths.get(".cache", "lazyyyyy", "mixin");
    private static final Path GLOBAL_SESSION_PATH = CACHE_PATH.resolve("session");
    private static final Path PATH_HASH_CACHE_PATH = CACHE_PATH.resolve("path-hash.json.gz");
    private static final Gson GSON = new Gson();

    private static final Map<String, PathHashEntry> pathHashCache = loadPathHashCache();

    private static class PathHashEntry {
        long lastModified;
        String hash;
    }

    private static Map<String, PathHashEntry> loadPathHashCache() {
        if (Files.exists(PATH_HASH_CACHE_PATH)) {
            try (var is = new GZIPInputStream(Files.newInputStream(PATH_HASH_CACHE_PATH));
                 var reader = new InputStreamReader(is)) {
                return GSON.fromJson(
                        reader, new TypeToken<Map<String, PathHashEntry>>() {
                        }.getType());
            } catch (IOException e) {
                LOGGER.warn("Failed to load path hash cache", e);
            }
        }
        return new HashMap<>();
    }

    private static void savePathHashCache() {
        try {
            Files.createDirectories(CACHE_PATH);
            try (var os = new GZIPOutputStream(Files.newOutputStream(PATH_HASH_CACHE_PATH));
                 var writer = new OutputStreamWriter(os)) {
                GSON.toJson(pathHashCache, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to save path hash cache", e);
        }
    }

    public static String loadGlobalSessionId() {
        if (Files.exists(GLOBAL_SESSION_PATH)) {
            try {
                String sessionId = Files.readString(GLOBAL_SESSION_PATH).trim();
                LOGGER.debug("Loaded cached global sessionId: {}", sessionId);
                return sessionId;
            } catch (IOException e) {
                LOGGER.warn("Failed to load global sessionId", e);
            }
        }
        return null;
    }

    public static void saveGlobalSessionId(String sessionId) {
        try {
            Files.createDirectories(CACHE_PATH);
            Files.writeString(GLOBAL_SESSION_PATH, sessionId);
            LOGGER.debug("Saved global sessionId: {}", sessionId);
        } catch (IOException e) {
            LOGGER.warn("Failed to save global sessionId", e);
        }
    }

    public static boolean applyMixinsCached(
            Object processor,
            MixinEnvironment environment,
            String name,
            ClassNode targetClassNode) {
        if (!LazyyyyyEarlyConfigInvoker.isFeatureEnabled("faster_mixin.cache")) {
            return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);
        }
        List<IHashProvider> relevantProviders = new ArrayList<>();
        for (var config : ((MixinProcessorAccessor) processor).lazyyyyy$getConfigs()) {
            MixinConfigAccessor accessor = (MixinConfigAccessor) config;
            if (accessor.hasMixinsFor(name)) {
                var source = config.getSource();
                if (source instanceof IHashProvider provider) {
                    relevantProviders.add(provider);
                }
            }
        }

        if (relevantProviders.isEmpty()) {
            return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);
        }

        try {
            Hasher hasher = Hashing.murmur3_128().newHasher();
            relevantProviders.parallelStream()
                    .map(provider -> {
                        try {
                            var hash = provider.lazyyyyy$getHash();
                            if (hash == null) {
                                LOGGER.warn("Failed to load hash for {} target {}", provider, name);
                            }
                            return hash;
                        } catch (IOException e) {
                            LOGGER.warn("Failed to get hash for {}", provider, e);
                            throw new UncheckedIOException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(hasher::putBytes);

            // Include original class hash to avoid collisions and handle updates
            hasher.putBytes(getClassBytes(targetClassNode));

            byte[] combinedHash = hasher.hash().asBytes();

            if (loadFromCache(name, targetClassNode, combinedHash)) {
                return true;
            }

            boolean transformed = ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(
                    environment,
                    name,
                    targetClassNode);
            saveToCache(name, targetClassNode, combinedHash);
            return transformed;
        } catch (UncheckedIOException e) {
            return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);
        }
    }

    public static byte[] getFileHash(Path path) throws IOException {
        String key = path.toAbsolutePath().toString();
        long lastModified = Files.getLastModifiedTime(path).toMillis();

        PathHashEntry entry = pathHashCache.get(key);
        if (entry != null && entry.lastModified == lastModified) {
            return HashCode.fromString(entry.hash).asBytes();
        }

        byte[] hash;
        try (var is = new HashingInputStream(Hashing.murmur3_128(), Files.newInputStream(path))) {
            is.readAllBytes();
            hash = is.hash().asBytes();
        }

        entry = new PathHashEntry();
        entry.lastModified = lastModified;
        entry.hash = HashCode.fromBytes(hash).toString();
        pathHashCache.put(key, entry);
        savePathHashCache();

        return hash;
    }

    public static boolean loadFromCache(String className, ClassNode classNode, byte[] hash) {
        var hashString = HashCode.fromBytes(hash).toString();
        var classPath = CACHE_PATH.resolve(hashString).resolve(className + ".class");

        if (Files.exists(classPath)) {
            try {
                byte[] classBytes = Files.readAllBytes(classPath);
                ClassReader reader = new ClassReader(classBytes);
                cleanClassNode(classNode);
                reader.accept(classNode, ClassReader.EXPAND_FRAMES);
                LOGGER.debug("Loaded cached class '{}' from hash {}", className, hashString);
                return true;
            } catch (IOException e) {
                LOGGER.warn("Failed to read cache for {} in {}", className, hashString, e);
            }
        }
        return false;
    }

    public static void saveToCache(String className, ClassNode classNode, byte[] hash) {
        var hashString = HashCode.fromBytes(hash).toString();
        var cachePath = CACHE_PATH.resolve(hashString);
        var classPath = cachePath.resolve(className + ".class");

        try {
            Files.createDirectories(cachePath);
            Files.write(classPath, getClassBytes(classNode));
            LOGGER.debug("Saved cached class '{}' to hash {}", className, hashString);
        } catch (IOException e) {
            LOGGER.warn("Failed to save cache for {} in {}", className, hashString, e);
        }
    }

    private static void cleanClassNode(ClassNode classNode) {
        classNode.interfaces = new ArrayList<>();
        classNode.module = null;
        classNode.fields = new ArrayList<>();
        classNode.methods = new ArrayList<>();
        classNode.innerClasses = new ArrayList<>();
        classNode.visibleAnnotations = null;
        classNode.invisibleAnnotations = null;
        classNode.visibleTypeAnnotations = null;
        classNode.invisibleTypeAnnotations = null;
        classNode.attrs = null;
        classNode.nestHostClass = null;
        classNode.nestMembers = null;
        classNode.permittedSubclasses = null;
        classNode.recordComponents = null;
    }

    private static byte[] getClassBytes(ClassNode classNode) {
        MixinClassWriter cw = new MixinClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
