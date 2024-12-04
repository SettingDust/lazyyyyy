package settingdust.lazyyyyy.forge.core.faster_mixin.hack;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.service.IClassProvider;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public final class FasterClassProviderWrapper implements IClassProvider {
    public final IClassProvider wrapped;

    public FasterClassProviderWrapper(IClassProvider wrapped) {this.wrapped = wrapped;}

    @Deprecated
    @Override
    public URL[] getClassPath() {return wrapped.getClassPath();}

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {return wrapped.findClass(name);}

    /**
     * For hook into {@link org.spongepowered.asm.mixin.transformer.MixinConfig#onSelect()} to get the refmap currently
     */
    @Override
    public Class<?> findClass(final String name, final boolean initialize) throws ClassNotFoundException {
        var clazz = wrapped.findClass(name, initialize);
        var isPluginClazz = clazz.isInstance(IMixinConfigPlugin.class);
        if (!isPluginClazz) return clazz;
        var stackTrace = Thread.currentThread().getStackTrace();
        var isPlugin = stackTrace[1].getClassName().equals("org.spongepowered.asm.mixin.transformer.PluginHandle") &&
                       stackTrace[2].getMethodName().equals("onSelect");
        if (!isPlugin) return clazz;
        var configs = FasterMixinServiceWrapper.pluginToConfigs.get(name);
        if (configs == null || configs.isEmpty()) return clazz;
        try {
            // FIXME Init the plugin twice may cause problems
            var plugin = (IMixinConfigPlugin) clazz.getDeclaredConstructor().newInstance();
            var refmap = plugin.getRefMapperConfig();
            for (final var config : configs) {
                FasterMixinServiceWrapper.configToRefmap.computeIfAbsent(
                    config, (ignored) -> {
                        FasterMixinServiceWrapper.LOGGER.debug(
                            "Config {} find refmap {} from plugin {}",
                            config.getName(),
                            refmap,
                            name
                        );
                        return refmap;
                    }
                );
            }
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            FasterMixinServiceWrapper.LOGGER.debug("Failed to initialize plugin {}", name, e);
        }
        return clazz;
    }

    @Override
    public Class<?> findAgentClass(final String name, final boolean initialize) throws ClassNotFoundException {
        return wrapped.findAgentClass(
            name,
            initialize
        );
    }
}
