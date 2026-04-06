package settingdust.lazyyyyy.fabric.faster_mixin;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jetbrains.annotations.Nullable;
import settingdust.lazyyyyy.faster_mixin.ContainerHandlePath;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ContainerHandleMod extends ContainerHandlePath {
    private final ModContainer mod;

    public ContainerHandleMod(ModContainer mod) {
        super(mod.getMetadata().getId(), mod.getRootPath());
        this.mod = mod;
    }

    @Override
    public InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException {
        var path = mod.findPath(resource);
        if (path.isPresent()) {
            return Files.newInputStream(path.get());
        } else {
            return null;
        }
    }

    @Override
    public byte @Nullable [] lazyyyyy$getHash() throws IOException {
        ModContainer root = this.mod;
        while (root.getContainingMod().isPresent()) {
            root = root.getContainingMod().get();
        }

        var origin = root.getOrigin();
        if (origin.getKind() == ModOrigin.Kind.PATH) {
            Hasher hasher = Hashing.murmur3_128().newHasher();
            for (var path : origin.getPaths()) {
                hasher.putBytes(MixinCacheManager.getFileHash(path));
            }
            return hasher.hash().asBytes();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("ContainerHandleMod(%s)", this.getId());
    }
}
