package settingdust.lazyyyyy.faster_mixin.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinService;
import settingdust.lazyyyyy.faster_mixin.util.LazyyyyyEarlyConfigInvoker;
import settingdust.lazyyyyy.faster_mixin.util.MixinInternals;
import settingdust.lazyyyyy.faster_mixin.util.accessor.ClassInfoAccessor;
import settingdust.lazyyyyy.faster_mixin.util.accessor.MixinProcessorAccessor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class MixinCacheManager {
    private static final ILogger LOGGER = MixinService.getService().getLogger("Lazyyyyy/MixinCache");
    private static final Path CACHE_PATH = Paths.get(".cache", "lazyyyyy", "mixin");
    private static final Path GLOBAL_SESSION_PATH = CACHE_PATH.resolve("session");
    private static final Path PATH_HASH_CACHE_PATH = CACHE_PATH.resolve("path-hash.json.gz");
    private static final Path SYNTHETIC_PATH = CACHE_PATH.resolve("synthetic");
    private static final Path TARGETS_PATH = CACHE_PATH.resolve("targets");

    private static final PathHashCache pathHashCache = new PathHashCache(PATH_HASH_CACHE_PATH);
    private static final MixinGeneratorManager generatorManager = new MixinGeneratorManager(SYNTHETIC_PATH);

    private static final boolean enabled = LazyyyyyEarlyConfigInvoker.isFeatureEnabled("faster_mixin.cache");

    public static String loadGlobalSessionId() {
        if (Files.exists(GLOBAL_SESSION_PATH)) {
            try {
                return Files.readString(GLOBAL_SESSION_PATH);
            } catch (IOException e) {
                LOGGER.warn("Failed to load global session id", e);
            }
        }
        return null;
    }

    public static void saveGlobalSessionId(String sessionId) {
        try {
            Files.createDirectories(GLOBAL_SESSION_PATH.getParent());
            Files.writeString(GLOBAL_SESSION_PATH, sessionId);
        } catch (IOException e) {
            LOGGER.warn("Failed to save global session id", e);
        }
    }

    public static boolean applyMixinsCached(
            Object processor,
            MixinEnvironment environment,
            String name,
            ClassNode targetClassNode) {
        if (!enabled)
            return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);

        MixinInternals.initMixinIndex(((MixinProcessorAccessor) processor).lazyyyyy$getConfigs());
        generatorManager.init();

        try {
            var hasher = Hashing.murmur3_128().newHasher();
            MixinInternals.setSuppressForNameWarn(true);
            var classInfo = ClassInfo.forName(name);
            MixinInternals.setSuppressForNameWarn(false);
            if (classInfo == null)
                return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);
            for (var mixin : ((ClassInfoAccessor) (Object) classInfo).lazyyyyy$getMixins()) {
                var config = mixin.getConfig();
                var source = config.getSource();
                try {
                    if (source instanceof IHashProvider provider) {
                        hasher.putBytes(provider.lazyyyyy$getHash());
                    } else {
                        LOGGER.warn("Failed to load hash for {} from {} target {}", mixin, source, name);
                    }
                } catch (IOException e) {
                    LOGGER.warn("Failed to get hash for {}", source, e);
                    throw new UncheckedIOException(e);
                }
            }

            hasher.putBytes(MixinCacheUtil.getClassBytes(targetClassNode));

            var combinedHash = hasher.hash();

            if (loadFromCache(name, targetClassNode, combinedHash)) {
                return true;
            }

            boolean transformed = ((MixinProcessorAccessor) processor)
                    .lazyyyyy$applyMixins(environment, name, targetClassNode);
            if (transformed) {
                saveToCache(name, MixinCacheUtil.getClassBytes(targetClassNode), combinedHash);
            }
            return transformed;
        } catch (UncheckedIOException e) {
            return ((MixinProcessorAccessor) processor).lazyyyyy$applyMixins(environment, name, targetClassNode);
        }
    }

    public static boolean generateClass(String name, ClassNode node) {
        return generatorManager.generateClass(name, node);
    }

    public static void saveGeneratedClass(String name, byte[] bytes) {
        if (!enabled) return;
        generatorManager.saveGeneratedClass(name, bytes);
    }

    public static boolean loadFromCache(String name, ClassNode node, HashCode hash) {
        if (!enabled) return false;
        var classPath = TARGETS_PATH.resolve(hash.toString()).resolve(name.replace('.', '/') + ".class");
        if (Files.exists(classPath)) {
            try {
                byte[] classBytes = Files.readAllBytes(classPath);
                ClassReader reader = new ClassReader(classBytes);
                ClassNode tempNode = new ClassNode();
                reader.accept(tempNode, ClassReader.EXPAND_FRAMES);
                MixinCacheUtil.cleanClassNode(node);
                tempNode.accept(node);
                return true;
            } catch (Throwable e) {
                LOGGER.warn("Failed to load cached class {}", name, e);
            }
        }
        return false;
    }

    public static void saveToCache(String name, byte[] bytes, HashCode hash) {
        if (!enabled) return;
        CompletableFuture.runAsync(() -> {
            try {
                var classPath = TARGETS_PATH.resolve(hash.toString()).resolve(name.replace('.', '/') + ".class");
                Files.createDirectories(classPath.getParent());
                Files.write(classPath, bytes);
            } catch (IOException e) {
                LOGGER.warn("Failed to save cached class {}", name, e);
            }
        });
    }

    public static byte[] getFileHash(Path path) {
        return pathHashCache.getFileHash(path);
    }
}
