package settingdust.lazyyyyy.mixin.moremcmeta.avoid_duplicate_sprites;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import settingdust.lazyyyyy.moremcmeta.SetBackingLinkedBlockingQueue;

import java.util.concurrent.LinkedBlockingQueue;

@IfModLoaded("moremcmeta")
@Mixin(value = TextureAtlas.class, priority = 1001)
public class TextureAtlasMixin {
    @Dynamic(mixin = io.github.moremcmeta.moremcmeta.impl.client.mixin.TextureAtlasMixin.class)
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/concurrent/LinkedBlockingQueue"))
    private LinkedBlockingQueue<ResourceLocation> lazyyyyy$avoidDuplicateSprites() {
        return new SetBackingLinkedBlockingQueue<>();
    }
}
