package settingdust.lazyyyyy.forge.core.faster_module;

import java.lang.module.ResolvedModule;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class BootstrapHooks {
    public static Collection<Set<ResolvedModule>> filterAutomaticModules(Map<ResolvedModule, Set<ResolvedModule>> g1) {
        return g1.entrySet().stream()
                 .filter(e -> !e.getKey().reference().descriptor().isAutomatic())
                 .map(Map.Entry::getValue)
                 .toList();
    }
}
