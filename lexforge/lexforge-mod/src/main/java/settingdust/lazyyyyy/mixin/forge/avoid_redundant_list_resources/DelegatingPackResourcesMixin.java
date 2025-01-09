package settingdust.lazyyyyy.mixin.forge.avoid_redundant_list_resources;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.resource.DelegatingPackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(DelegatingPackResources.class)
public class DelegatingPackResourcesMixin {
    @Shadow
    @Final
    private Map<String, List<PackResources>> namespacesAssets;

    @Shadow
    @Final
    private Map<String, List<PackResources>> namespacesData;

    @Redirect(
        method = "listResources",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraftforge/resource/DelegatingPackResources;delegates:Ljava/util/List;"
        )
    )
    private List<PackResources> lazyyyyyy$avoidRedundantListResources(
        DelegatingPackResources instance,
        PackType type,
        String resourceNamespace
    ) {
        var namespaces = type == PackType.CLIENT_RESOURCES
                         ? namespacesAssets
                         : namespacesData;
        var list = namespaces.get(resourceNamespace);
        return list == null ? Collections.emptyList() : list;
    }
}
