package settingdust.lazyyyyy.faster_mixin;

import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import settingdust.lazyyyyy.faster_mixin.cache.IHashProvider;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;
import settingdust.lazyyyyy.faster_mixin.resource_provider.IResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContainerHandlePath extends ContainerHandleURI implements IHashProvider, IResourceProvider {
    private final String id;
    private final Path path;

    public ContainerHandlePath(Path path) {
        this(path.getFileName().toString(), path);
    }

    public ContainerHandlePath(String id, Path path) {
        super(path.toUri());
        this.id = id;
        this.path = path;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return this.path.toString();
    }

    @Override
    public String toString() {
        return String.format("ContainerHandlePath(%s|%s)", this.getId(), this.path);
    }

    @Override
    public byte[] lazyyyyy$getHash() throws IOException {
        return MixinCacheManager.getFileHash(this.path);
    }

    @Override
    public InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException {
        return Files.newInputStream(this.path.resolve(resource));
    }
}