package settingdust.lazyyyyy.mixin.forge.weaponmaster.faster_initilize;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.sky.weaponmaster.WeaponMaster;
import com.sky.weaponmaster.datas.Parts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import settingdust.lazyyyyy.forge.weaponmaster.faster_initilize.FasterParts;

@IfModLoaded(WeaponMaster.MODID)
@Mixin(Parts.class)
public class PartsMixin {
    /**
     * @author SettingDust
     * @reason Faster with coroutine and less useless logic
     */
    @Overwrite(remap = false)
    public static long[] countEveryWeaponVarient() {
        return new long[]{FasterParts.INSTANCE.countEveryWeaponVariant()};
    }
}
