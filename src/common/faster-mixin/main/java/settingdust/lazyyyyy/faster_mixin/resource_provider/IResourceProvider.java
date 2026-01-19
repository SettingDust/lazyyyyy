package settingdust.lazyyyyy.faster_mixin.resource_provider;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.io.InputStream;

public interface IResourceProvider {
    ILogger LOGGER = MixinService.getService().getLogger("Lazyyyyy");

    static InputStream getResourceAsStream(
            String resource,
            @Nullable IMixinConfigSource source,
            IMixinService service) throws IOException {
        if (source instanceof IResourceProvider provider) {
            var result = provider.lazyyyyy$getResourceAsStream(resource);
            if (result != null) return result;
            LOGGER.warn("Resource {} not found in source {}. Fallback to slower method", resource, source);
            return service.getResourceAsStream(resource);
        } else {
            LOGGER.debug("Resource {} source {} is not IResourceProvider", resource, source);
        }
        return service.getResourceAsStream(resource);
    }

    InputStream lazyyyyy$getResourceAsStream(String resource) throws IOException;
}
