package settingdust.lazyyyyy.mixin.forge.kiwi.faster_annotation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.forgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.forge.FasterKiwiAnnotatedTypeLoaderKt;
import snownee.kiwi.loader.AnnotatedTypeLoader;

import java.io.InputStream;
import java.util.Optional;

@IfModLoaded("kiwi")
@Mixin(value = AnnotatedTypeLoader.class, remap = false)
public class AnnotatedTypeLoaderMixin {
    @ModifyExpressionValue(
        method = "get()Lsnownee/kiwi/loader/KiwiConfiguration;",
        at = @At(
            value = "INVOKE",
            ordinal = 0,
            target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"
        )
    )
    private Optional<IModInfo> lazyyyyy$recordMod(
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<IModInfo> original,
        @Share("modInfo") LocalRef<IModInfo> modInfoRef
    ) {
        original.ifPresent(modInfoRef::set);
        return original;
    }

    @Redirect(
        method = "get()Lsnownee/kiwi/loader/KiwiConfiguration;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"
        )
    )
    private InputStream lazyyyyy$fasterGetResource(
        final ClassLoader instance,
        final String name,
        @Share("modInfo") LocalRef<IModInfo> modInfoRef
    ) {
        return FasterKiwiAnnotatedTypeLoaderKt.getResource(modInfoRef.get(), name);
    }
}
