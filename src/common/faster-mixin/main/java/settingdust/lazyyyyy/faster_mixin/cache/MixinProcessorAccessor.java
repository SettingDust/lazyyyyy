package settingdust.lazyyyyy.faster_mixin.cache;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;

import java.util.List;

public interface MixinProcessorAccessor {
    List<IMixinConfig> lazyyyyy$getConfigs();

    boolean lazyyyyy$applyMixins(MixinEnvironment environment, String name, ClassNode targetClassNode);

    String lazyyyyy$getSessionId();

    void lazyyyyy$setSessionId(String sessionId);
}
