package settingdust.lazyyyyy.forge.game.mixin.pack_resources_cache.sinytra_connector;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import settingdust.lazyyyyy.forge.game.mixin.pack_resources_cache.ForgePathPackResourcesMixin;

import java.io.InputStream;

@Mixin(ModNioResourcePack.class)
public class ModNioResourcePackMixin extends ForgePathPackResourcesMixin {
    @WrapMethod(method = "openFile")
    private IoSupplier<InputStream> lazyyyyy$cache(String filename, Operation<IoSupplier<InputStream>> original) {
        var result = lazyyyyy$cache.getResource(filename);
        return result == null ? original.call(filename) : result;
    }
}
