package settingdust.lazyyyyy.mixin.entity_sound_features.async_sound_events;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import traben.entity_sound_features.ESF;

@IfModLoaded("entity_sound_features")
@Mixin(value = ESF.class, remap = false)
public interface ESFAccessor {
    @Accessor
    static Logger getLOGGER() {
        throw new UnsupportedOperationException();
    }
}
