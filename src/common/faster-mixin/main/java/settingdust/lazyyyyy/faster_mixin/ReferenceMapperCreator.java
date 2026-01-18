package settingdust.lazyyyyy.faster_mixin;

import com.google.common.io.Closeables;
import com.google.gson.JsonParseException;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.logging.MessageRouter;

import javax.tools.Diagnostic;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class ReferenceMapperCreator {
    public static ReferenceMapper read(String resourcePath, IMixinConfigSource source) {
        Reader reader = null;
        try {
            IMixinService service = MixinService.getService();
            InputStream resource = IResourceProvider.getResourceAsStream(resourcePath, source, service);
            if (resource != null) {
                reader = new InputStreamReader(resource);
                return ReferenceMapper.read(reader, resourcePath);
            }
        } catch (JsonParseException ex) {
            MessageRouter.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, String.format(
                            "Invalid REFMAP JSON in %s: %s %s",
                            resourcePath, ex.getClass().getName(), ex.getMessage()));
        } catch (Exception ex) {
            MessageRouter.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, String.format(
                            "Failed reading REFMAP JSON from %s: %s %s",
                            resourcePath, ex.getClass().getName(), ex.getMessage()));
        } finally {
            Closeables.closeQuietly(reader);
        }

        return ReferenceMapper.DEFAULT_MAPPER;
    }
}
