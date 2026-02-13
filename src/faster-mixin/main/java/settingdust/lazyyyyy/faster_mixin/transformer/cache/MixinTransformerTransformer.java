package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.injection.CASM;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.transformers.MixinClassWriter;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheManager;
import settingdust.lazyyyyy.faster_mixin.cache.MixinCacheUtil;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MixinTransformer")
public abstract class MixinTransformerTransformer {
    @CASM("transformClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z")
    private static void lazyyyyy$cachedTransformClassNode(MethodNode method) {
        lazyyyyy$redirectApplyMixins(method);
    }

    @CASM("transformClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;[B)[B")
    private static void lazyyyyy$cachedTransformClassBytes(MethodNode method) {
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

    @Inject(
            method = "generateClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;)[B",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/transformer/MixinClassGenerator;generateClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z"))
    private void lazyyyyy$loadFromCache(
            MixinEnvironment environment,
            String name,
            CallbackInfoReturnable<byte[]> cir,
            @Local(name = "classNode") ClassNode node) {
        var cached = MixinCacheManager.generateClass(name, node);
        if (cached) {
            var cw = new MixinClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            node.accept(cw);
            cir.setReturnValue(cw.toByteArray());
        }
    }

    @Inject(
            method = "generateClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z",
            cancellable = true,
            at = @At("HEAD"))
    private void lazyyyyy$loadFromCache(
            MixinEnvironment environment,
            String name,
            ClassNode classNode,
            CallbackInfoReturnable<Boolean> cir) {
        var cached = MixinCacheManager.generateClass(name, classNode);
        if (cached) cir.setReturnValue(true);
    }

    @ModifyExpressionValue(
            method = "generateClass*",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/transformer/MixinClassGenerator;generateClass(Lorg/spongepowered/asm/mixin/MixinEnvironment;Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z"))
    private boolean lazyyyyy$saveGeneratedClass(
            boolean original,
            @CLocalVariable(name = "name") String name,
            @CLocalVariable(name = "classNode") ClassNode node) {
        MixinCacheManager.saveGeneratedClass(name, MixinCacheUtil.getClassBytes(node));
        return original;
    }
}
