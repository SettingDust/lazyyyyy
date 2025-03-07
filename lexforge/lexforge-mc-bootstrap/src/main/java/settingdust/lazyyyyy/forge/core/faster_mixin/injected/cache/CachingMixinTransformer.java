package settingdust.lazyyyyy.forge.core.faster_mixin.injected.cache;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinInitialisationError;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.mixin.transformer.ext.IExtensionRegistry;
import org.spongepowered.asm.transformers.MixinClassWriter;
import settingdust.lazyyyyy.forge.core.faster_mixin.injected.FasterMixinServiceWrapper;
import settingdust.lazyyyyy.forge.core.faster_mixin.injected.MixinConfigReflection;
import settingdust.lazyyyyy.forge.core.faster_mixin.injected.MixinPlatformAgentDefault;
import settingdust.lazyyyyy.forge.core.faster_mixin.injected.SecureJarResourceReflection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Transformer which manages the mixin configuration and application process
 */
public class CachingMixinTransformer implements IMixinTransformer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static IMixinTransformer wrapped;
    private static final Map<Path, byte[]> JAR_TO_HASHES = new ConcurrentHashMap<>();
    private static final Path CACHE_PATH = FMLPaths.GAMEDIR.get().resolve(".lazyyyyy").resolve("mixin-cache");
    private static final Set<MixinEnvironment> SELECTED = ConcurrentHashMap.newKeySet();
    private static boolean selecting = false;

    /**
     * Impl of mixin transformer factory
     */
    public static class Factory implements IMixinTransformerFactory {

        /* (non-Javadoc)
         * @see org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory
         *      #createTransformer()
         */
        @Override
        public IMixinTransformer createTransformer() throws MixinInitialisationError {
            return new CachingMixinTransformer();
        }
    }

    @Override
    public void audit(final MixinEnvironment environment) {wrapped.audit(environment);}

    @Override
    public List<String> reload(final String mixinClass, final ClassNode classNode) {
        return wrapped.reload(
            mixinClass,
            classNode
        );
    }

    @Override
    public boolean computeFramesForClass(
        final MixinEnvironment environment,
        final String name,
        final ClassNode classNode
    ) {return wrapped.computeFramesForClass(environment, name, classNode);}

    @Override
    public byte[] transformClassBytes(
        final String name,
        final String transformedName,
        final byte[] basicClass
    ) {return wrapped.transformClassBytes(name, transformedName, basicClass);}

    @Override
    public byte[] transformClass(
        final MixinEnvironment environment,
        final String name,
        final byte[] classBytes
    ) {return wrapped.transformClass(environment, name, classBytes);}

    private static void cleanClassNode(final ClassNode classNode) {
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

    @Override
    public boolean transformClass(
        final MixinEnvironment environment,
        final String name,
        final ClassNode classNode
    ) {
        var needCache = new AtomicBoolean(true);
        var hashes = new ArrayList<CompletableFuture<byte[]>>();
        if (FasterMixinServiceWrapper.ENV_TO_CONFIGS.isEmpty()) {
            needCache.set(false);
        } else {
            if (SELECTED.add(environment)) {
                var processor = MixinTransformerReflection.getProcessor(wrapped);
                selecting = true;
                MixinProcessorReflection.checkSelect(processor, environment);
                selecting = false;
            }
            var hasMixin = false;
            for (final var config : FasterMixinServiceWrapper.ENV_TO_CONFIGS.get(environment)) {
                if (!MixinConfigReflection.hasMixinsFor(config, name)) continue;
                hasMixin = true;
                var container = MixinPlatformAgentDefault.CONFIG_TO_CONTAINER.get(config.getName());
                if (container == null) {
                    LOGGER.warn(
                        "Failed to get container for config '{}'. I can't cache '{}'",
                        config.getName(),
                        name
                    );
                    continue;
                }
                try {
                    var jar = (SecureJar) SecureJarResourceReflection.secureJarField.get(container);
                    hashes.add(CompletableFuture.supplyAsync(() -> tryGetHash(config, jar, needCache)));
                } catch (IllegalAccessException e) {
                    LOGGER.warn(
                        "Failed to get jar from container {} for config '{}'. I can't cache '{}'",
                        container,
                        config.getName(),
                        name
                    );

                    LOGGER.debug(
                        "Failed to get jar from container {} for config '{}'. I can't cache '{}'",
                        container,
                        config.getName(),
                        name,
                        e
                    );
                    needCache.set(false);
                    break;
                }
            }
            if (!hasMixin) needCache.set(false);
        }
        if (!needCache.get()) {
            for (final var future : hashes) {
                future.cancel(true);
            }
        }
        var cached = true;
        var filepath = name.replace('.', '/');
        var hashPath = CACHE_PATH.resolve(filepath + ".hash");
        var classPath = CACHE_PATH.resolve(filepath + ".class");
        byte[] hash = new byte[0];
        var transformed = needCache.get();
        if (needCache.get()) {
            var hasMixin = !hashes.isEmpty();
            if (!hasMixin) {
                transformed = false;
            } else {
                hash = DigestUtils.md5(hashes.stream()
                                             .map(CompletableFuture::join)
                                             .reduce(new byte[0], ArrayUtils::addAll));
                try {
                    if (Files.exists(hashPath) && Arrays.equals(Files.readAllBytes(hashPath), hash)) {
                        try {
                            var cachedClass = Files.readAllBytes(classPath);
                            var classReader = new ClassReader(cachedClass);
                            cleanClassNode(classNode);
                            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
                            LOGGER.debug("Loaded cached class '{}'", name);
                        } catch (IOException e) {
                            LOGGER.debug(
                                "Failed to read cached class '{}'",
                                name,
                                e
                            );
                            cached = false;
                        }
                    } else {
                        cached = false;
                    }
                } catch (IOException e) {
                    LOGGER.debug(
                        "Failed to read hash from '{}'",
                        hashPath,
                        e
                    );
                    cached = false;
                }
            }
        }
        if (!selecting && (!cached || !transformed)) {
            transformed = wrapped.transformClass(environment, name, classNode);
            if (!needCache.get()) needCache.set(transformed);
            if (needCache.get()) {
                try {
                    Files.createDirectories(classPath.getParent());
                    Files.write(hashPath, hash);
                    Files.write(classPath, getClassBytes(classNode, true));
                } catch (IOException e) {
                    LOGGER.debug(
                        "Failed to write cached class '{}'",
                        name,
                        e
                    );
                }
            }
        }
        return transformed;
    }


    public static byte[] getClassBytes(ClassNode classNode, boolean computeFrames) {
        try {
            MixinClassWriter cw = new MixinClassWriter(computeFrames ? ClassWriter.COMPUTE_FRAMES : 0);
            classNode.accept(cw);
            return cw.toByteArray();
        } catch (Exception ex) {
            LOGGER.error("Caching class {} failed!", classNode.name, ex);
            throw ex;
        }
    }

    private static byte @Nullable [] tryGetHash(
        final IMixinConfig config,
        final SecureJar jar,
        final AtomicBoolean needCache
    ) {
        return JAR_TO_HASHES.computeIfAbsent(
            jar.getPrimaryPath(), path -> {
                try {
                    return DigestUtils.md5(Files.newInputStream(path));
                } catch (IOException e) {
                    LOGGER.warn(
                        "Failed to calculate hash for '{}'. I can't cache '{}'",
                        path,
                        config.getName()
                    );

                    LOGGER.debug(
                        "Failed to calculate hash for '{}'. I can't cache '{}'",
                        path,
                        config.getName(),
                        e
                    );
                    needCache.set(false);
                    return null;
                }
            }
        );
    }

    @Override
    public byte[] generateClass(
        final MixinEnvironment environment,
        final String name
    ) {return wrapped.generateClass(environment, name);}

    @Override
    public boolean generateClass(
        final MixinEnvironment environment,
        final String name,
        final ClassNode classNode
    ) {return wrapped.generateClass(environment, name, classNode);}

    @Override
    public IExtensionRegistry getExtensions() {return wrapped.getExtensions();}
}
