package settingdust.lazyyyyy.faster_mixin.cache;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;
import settingdust.lazyyyyy.faster_mixin.cache.generator.ArgsClassGeneratorCache;
import settingdust.lazyyyyy.faster_mixin.cache.generator.GeneratorCache;
import settingdust.lazyyyyy.faster_mixin.cache.generator.InnerClassGeneratorCache;
import settingdust.lazyyyyy.faster_mixin.cache.generator.LocalRefClassGeneratorCache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MixinGeneratorManager {
    private static final ILogger LOGGER = MixinService.getService().getLogger("Lazyyyyy/MixinCache/Generator");

    private final Path syntheticPath;
    private final List<GeneratorCache> caches = new ArrayList<>();
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean initialized = new AtomicBoolean();

    public MixinGeneratorManager(Path syntheticPath) {
        this.syntheticPath = syntheticPath;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Lazyyyyy-Cache-Saver");
            t.setDaemon(true);
            return t;
        });
    }

    public void init() {
        if (initialized.get()) return;
        initialized.set(true);

        caches.add(new ArgsClassGeneratorCache());
        caches.add(new InnerClassGeneratorCache());
        caches.add(new LocalRefClassGeneratorCache());

        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));

        scheduler.scheduleAtFixedRate(this::saveDirty, 30, 30, TimeUnit.SECONDS);

        loadAll();
        saveAll();
    }

    private void loadAll() {
        for (GeneratorCache cache : caches) {
            try {
                cache.load(syntheticPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to load cache", e);
            }
        }
    }

    private void saveDirty() {
        for (GeneratorCache cache : caches) {
            if (cache.isDirty()) {
                try {
                    cache.save(syntheticPath);
                } catch (IOException e) {
                    LOGGER.warn("Failed to save cache", e);
                }
            }
        }
    }

    private void saveAll() {
        try {
            Files.createDirectories(syntheticPath);
        } catch (IOException ignored) {
        }
        for (GeneratorCache cache : caches) {
            try {
                cache.save(syntheticPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to save cache", e);
            }
        }
    }

    public boolean generateClass(String name, ClassNode node) {
        init();
        var path = syntheticPath.resolve(name.replace('.', '/') + ".class");
        if (Files.exists(path)) {
            try {
                byte[] classBytes = Files.readAllBytes(path);
                ClassReader reader = new ClassReader(classBytes);
                MixinCacheUtil.cleanClassNode(node);
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                LOGGER.debug("Loaded cached generated class '{}", name);
                return true;
            } catch (IOException e) {
                LOGGER.warn("Failed to read generated cache for {}", name, e);
            }
        }
        return false;
    }

    public void saveGeneratedClass(String name, byte[] bytes) {
        var path = syntheticPath.resolve(name.replace('.', '/') + ".class");
        CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
            } catch (IOException e) {
                LOGGER.warn("Failed to save generated class {}", name, e);
            }
        });
    }
}
