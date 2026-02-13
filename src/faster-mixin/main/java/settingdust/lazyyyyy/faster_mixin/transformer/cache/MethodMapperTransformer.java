package settingdust.lazyyyyy.faster_mixin.transformer.cache;

import com.google.common.hash.Hashing;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.charset.StandardCharsets;

@Mixin(targets = "org.spongepowered.asm.mixin.transformer.MethodMapper")
public class MethodMapperTransformer {
    @Redirect(
            method = "Lorg/spongepowered/asm/mixin/transformer/MethodMapper;getUniqueName(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;Lorg/objectweb/asm/tree/FieldNode;Ljava/lang/String;)Ljava/lang/String;",
            at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toHexString(I)Ljava/lang/String;"))
    private String lazyyyyy$stableName(
            int i,
            @CLocalVariable(name = "mixin") IMixinInfo mixin,
            @CLocalVariable(name = "field") FieldNode node) {
        var hasher = Hashing.murmur3_32_fixed().newHasher();
        hasher.putString(mixin.getName(), StandardCharsets.UTF_8);
        hasher.putString(node.name, StandardCharsets.UTF_8);
        return hasher.hash().toString();
    }

    @Redirect(
            method = "Lorg/spongepowered/asm/mixin/transformer/MethodMapper;getUniqueName(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;Lorg/objectweb/asm/tree/MethodNode;Ljava/lang/String;Z)Ljava/lang/String;",
            at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toHexString(I)Ljava/lang/String;"))
    private String lazyyyyy$stableName(
            int i,
            @CLocalVariable(name = "mixin") IMixinInfo mixin,
            @CLocalVariable(name = "method") MethodNode node) {
        var hasher = Hashing.murmur3_32_fixed().newHasher();
        hasher.putString(mixin.getName(), StandardCharsets.UTF_8);
        hasher.putString(node.name, StandardCharsets.UTF_8);
        return hasher.hash().toString();
    }
}
