package settingdust.lazyyyyy.forge.core;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import net.minecraftforge.fml.loading.moddiscovery.JarInJarDependencyLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.forgespi.locating.IDependencyLocator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DummyModLocator extends AbstractJarFileModLocator {
    @Override
    public Stream<Path> scanCandidates() {
        try {
            var modDiscovererField = FMLLoader.class.getDeclaredField("modDiscoverer");
            modDiscovererField.setAccessible(true);
            var modDiscoverer = (ModDiscoverer) modDiscovererField.get(null);
            var dependencyLocatorListField = ModDiscoverer.class.getDeclaredField("dependencyLocatorList");
            dependencyLocatorListField.setAccessible(true);
            var dependencyLocatorList = (List<IDependencyLocator>) dependencyLocatorListField.get(modDiscoverer);
            for (var i = 0; i < dependencyLocatorList.size(); i++) {
                if (dependencyLocatorList.get(i) instanceof JarInJarDependencyLocator locator) {
                    dependencyLocatorList.set(i, new DummyDependencyLocator(locator));
                    break;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Stream.empty();
    }

    @Override
    public String name() {
        return "Lazyyyyy wrapping JiJ early locator";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {

    }
}
