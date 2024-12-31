package settingdust.lazyyyyy.forge.core;

import com.google.common.collect.Lists;
import cpw.mods.niofs.union.UnionFileSystem;
import net.minecraftforge.fml.loading.moddiscovery.JarInJarDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.jarjar.selection.JarSelector;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.List;

public class DummyModLocator extends JarInJarDependencyLocator {
    @Override
    public List<IModFile> scanMods(final Iterable<IModFile> loadedMods) {
        try {
            var modURI = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            var modPath = ((UnionFileSystem) FileSystems.getFileSystem(modURI)).getPrimaryPath();
            IModLocator.ModFileOrException mod = createMod(modPath);

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
