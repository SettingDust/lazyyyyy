package settingdust.lazyyyyy.forge.core.faster_mixin.injected.cache;

import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;

public class NamedInnerClassGenerator implements IClassGenerator {
    private final IClassGenerator wrapped;

    public NamedInnerClassGenerator(final IClassGenerator wrapped) {this.wrapped = wrapped;}


}
