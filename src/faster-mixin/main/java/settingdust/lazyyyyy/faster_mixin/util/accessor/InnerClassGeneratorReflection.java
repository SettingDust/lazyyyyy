package settingdust.lazyyyyy.faster_mixin.util.accessor;

import net.lenni0451.reflect.Classes;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.method.MethodWrapper;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;
import settingdust.lazyyyyy.faster_mixin.util.MixinInternals;

public class InnerClassGeneratorReflection {
    public static final Class<? extends IClassGenerator> CLASS = (Class<? extends IClassGenerator>) Classes.byName(
            "org.spongepowered.asm.mixin.transformer.InnerClassGenerator");
    private static final RStream STREAM = RStream.of(CLASS);
    private static final MethodWrapper REGISTER_INNER_CLASS_METHOD = STREAM.methods().by("registerInnerClass");

    public static IClassGenerator INSTANCE = MixinInternals.getExtensions()
            .getGenerator(InnerClassGeneratorReflection.CLASS);

    public static void registerInnerClass(IMixinInfo owner, ClassInfo targetClass, String innerClassName) {
        REGISTER_INNER_CLASS_METHOD.invokeInstance(INSTANCE, owner, targetClass, innerClassName);
    }
}
