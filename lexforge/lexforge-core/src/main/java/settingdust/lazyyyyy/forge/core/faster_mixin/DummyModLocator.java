package settingdust.lazyyyyy.forge.core.faster_mixin;

import com.google.common.collect.Lists;
import net.minecraftforge.fml.loading.moddiscovery.JarInJarDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.jarjar.selection.JarSelector;
import org.apache.commons.lang3.SystemUtils;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class DummyModLocator extends JarInJarDependencyLocator {
    @Override
    public List<IModFile> scanMods(final Iterable<IModFile> loadedMods) {
        try {
            var path = getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
            if (SystemUtils.IS_OS_WINDOWS) path = path.substring(1, path.lastIndexOf("/"));
            if (path.lastIndexOf("#") != -1) path = path.substring(0, path.lastIndexOf("#"));
            IModLocator.ModFileOrException mod = createMod(Paths.get(path));

            return JarSelector.detectAndSelect(
                Lists.newArrayList(mod.file()),
                this::loadResourceFromModFile,
                this::loadModFileFrom,
                this::identifyMod,
                this::exception
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "Lazyyyyy self loading locator";
    }
}
