package settingdust.lazyyyyy.faster_mixin.transformer;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.service.IClassBytecodeProvider;

import java.io.IOException;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinInfo")
public class MixinInfoTransformer {
    @Dynamic
    @Redirect(
            method = "loadMixinClass",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/service/IClassBytecodeProvider;getClassNode(Ljava/lang/String;ZI)Lorg/objectweb/asm/tree/ClassNode;"))
    private ClassNode lazyyyyy$backport(
            IClassBytecodeProvider instance,
            String name,
            boolean runTransformers,
            int readerFlags) throws IOException, ClassNotFoundException {
        return instance.getClassNode(name, runTransformers);
    }
}
