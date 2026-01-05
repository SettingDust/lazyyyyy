package settingdust.lazyyyyy.faster_module_resolver.transformer;

import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CLocalVariable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(targets = "java.lang.module.Resolver")
public class ResolverTransformer {
    @Shadow
    @Final
    private List<Configuration> parents;

    @CInline
    @Redirect(method = "makeGraph", at = @At(value = "INVOKE", target = "Ljava/lang/module/ModuleDescriptor;isAutomatic()Z"))
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
            @CLocalVariable(name = "nameToResolved") Map<String, ResolvedModule> nameToResolved
    ) {
        for (var entry : g1.entrySet()) {
            var module1 = entry.getKey();
            if (!module1.reference().descriptor().isAutomatic()) continue;

            var module1Reads = entry.getValue();

            module1Reads.addAll(nameToResolved.values());
            module1Reads.remove(module1);

            for (var parent : parents) {
                module1Reads.addAll(parent.modules());
            }
        }

        for (var entry : g1.entrySet()) {
            var module1 = entry.getKey();
            if (module1.reference().descriptor().isAutomatic()) continue;

            var module1Reads = entry.getValue();

            var toAdd = new ArrayList<ResolvedModule>();

            for (var module2 : module1Reads) {
                if (!module2.reference().descriptor().isAutomatic()) continue;

                Set<ResolvedModule> module2Reads;
                if (module2.configuration() == cf) {
                    module2Reads = g1.get(module2);
                } else {
                    module2Reads = module2.reads();
                }

                for (var module : module2Reads) {
                    if (!module.reference().descriptor().isAutomatic()) continue;
                    toAdd.add(module);
                }
            }

            if (!toAdd.isEmpty()) {
                module1Reads.addAll(toAdd);
                toAdd.clear();
            }
        }
    }
}
