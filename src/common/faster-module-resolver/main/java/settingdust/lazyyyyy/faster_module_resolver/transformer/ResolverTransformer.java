package settingdust.lazyyyyy.faster_module_resolver.transformer;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.CReplaceCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ResolvedModule;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base on <a href="https://github.com/openjdk/jdk/pull/15926/changes">Technici4n's PR</a>
 */

@CReplaceCallback
@Mixin(targets = "java.lang.module.Resolver")
public class ResolverTransformer {
    @Shadow
    @Final
    private List<Configuration> parents;

    @CInline
    @Redirect(
            method = "makeGraph",
            at = @At(value = "INVOKE", target = "Ljava/lang/module/ModuleDescriptor;isAutomatic()Z"))
    private boolean lazyyyyy$removeAutomaticLogic(ModuleDescriptor instance) {
        return false;
    }

    @CInline
    @Inject(method = "makeGraph", at = @At("RETURN"))
    @SuppressWarnings("InvalidInjectorMethodSignature")
    private void lazyyyyy$resolveAutomaticModules(
            Configuration cf,
            CallbackInfoReturnable<Map<ResolvedModule, Set<ResolvedModule>>> cir,
            @CLocalVariable(name = "g1") Map<ResolvedModule, Set<ResolvedModule>> g1,
            @CLocalVariable(name = "g2") Map<ResolvedModule, Set<ResolvedModule>> g2,
            @CLocalVariable(name = "nameToResolved") Map<String, ResolvedModule> nameToResolved
    ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var methodConfigurations = Configuration.class.getDeclaredMethod("configurations");
        var allModules = new HashSet<ResolvedModule>();
        var allAutomaticModules = new HashSet<ResolvedModule>();
        for (ResolvedModule m : nameToResolved.values()) {
            if (m.reference().descriptor().isAutomatic()) {
                allAutomaticModules.add(m);
            }
        }
        for (var c : parents) {
            for (var c1 : ((Stream<Configuration>) methodConfigurations.invoke(c)).toList()) {
                for (var module : c1.modules()) {
                    allModules.add(module);
                    if (module.reference().descriptor().isAutomatic()) allAutomaticModules.add(module);
                }
            }
        }

        for (Map.Entry<ResolvedModule, Set<ResolvedModule>> entry : g1.entrySet()) {
            ResolvedModule m1 = entry.getKey();
            if (m1.reference().descriptor().isAutomatic()) {
                Set<ResolvedModule> reads = entry.getValue();
                Set<ResolvedModule> requiresTransitive = g2.get(m1);

                // Reads: all selected modules
                reads.addAll(nameToResolved.values());
                reads.remove(m1);

                // Reads: all parent modules
                reads.addAll(allModules);

                // Transitive: all automatic modules
                if (requiresTransitive != null) {
                    requiresTransitive.addAll(allAutomaticModules);
                    requiresTransitive.remove(m1);
                }
            }
        }
    }
}
