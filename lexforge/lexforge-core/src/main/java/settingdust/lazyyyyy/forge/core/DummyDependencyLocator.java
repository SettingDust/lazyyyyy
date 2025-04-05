package settingdust.lazyyyyy.forge.core;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.loading.moddiscovery.JarInJarDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class DummyDependencyLocator extends JarInJarDependencyLocator {
    private final JarInJarDependencyLocator wrapped;

    public DummyDependencyLocator(final JarInJarDependencyLocator wrapped) {this.wrapped = wrapped;}

    @Override
    public List<IModFile> scanMods(final Iterable<IModFile> loadedMods) {
        try {
            var modURI = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            var modPath = Paths.get(modURI);
            IModLocator.ModFileOrException mod = createMod(modPath);
            var newLadedMods = Lists.newArrayList(loadedMods);
            newLadedMods.add(mod.file());
            return wrapped.scanMods(newLadedMods);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "Lazyyyyy wrapper locator{" + wrapped.name() + "}";
    }
}
