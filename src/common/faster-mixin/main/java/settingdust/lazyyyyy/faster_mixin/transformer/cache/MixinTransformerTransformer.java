package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import net.lenni0451.classtransform.annotations.injection.CASM;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinTransformer")
public abstract class MixinTransformerTransformer {
    @CASM("transformClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z")
    public static void lazyyyyy$cachedTransformClassNode(MethodNode method) {
        lazyyyyy$redirectApplyMixins(method);
    }

    @CASM("transformClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B")
    public static void lazyyyyy$cachedTransformClassBytes(MethodNode method) {
        lazyyyyy$redirectApplyMixins(method);
    }

    @Unique
    private static void lazyyyyy$redirectApplyMixins(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode min && min.name.equals("applyMixins")) {
                min.setOpcode(Opcodes.INVOKESTATIC);
                min.owner = "settingdust/lazyyyyy/faster_mixin/cache/MixinCacheManager";
                min.name = "applyMixinsCached";
                min.desc = "(Ljava/lang/Object;Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z";
            }
        }
    }
}
