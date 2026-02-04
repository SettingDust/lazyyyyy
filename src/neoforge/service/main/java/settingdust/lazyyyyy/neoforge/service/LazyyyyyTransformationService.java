package settingdust.lazyyyyy.neoforge.service;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.jetbrains.annotations.NotNull;
import settingdust.lazyyyyy.Lazyyyyy;

import java.util.List;
import java.util.Set;

public class LazyyyyyTransformationService implements ITransformationService {
    public LazyyyyyTransformationService() {
    }

    @NotNull
    @Override
    public String name() {
        return Lazyyyyy.ID;
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) {
    }

    @Override
    public @NotNull List<? extends ITransformer<?>> transformers() {
        return List.of();
    }
}
