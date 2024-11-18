package settingdust.lazyyyyy

import dev.isxander.yacl3.gui.image.ImageRenderer
import dev.isxander.yacl3.gui.image.ImageRendererFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.server.packs.resources.Resource
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class AsyncImageRenderer(val original: Lazy<ImageRendererFactory.ImageSupplier>) : ImageRenderer {
    val image = lazy { Lazyyyyy.scope.async { original.value.completeImage() } }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun render(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        renderWidth: Int,
        tickDelta: Float
    ) = if (image.isInitialized() && image.value.isCompleted) {
        image.value.getCompleted().render(graphics, x, y, renderWidth, tickDelta)
    } else {
        image.value
        0
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun close() {
        try {
            image.value.getCompleted().close()
        } catch (_: IllegalStateException) {
        }
    }
}

fun asyncPrepareImageFromTexture(
    resource: Resource,
    original: (InputStream) -> ImageRendererFactory.ImageSupplier
): ImageRendererFactory.ImageSupplier = object : ImageRendererFactory.ImageSupplier {
    override fun completeImage() = AsyncImageRenderer(lazy { original(resource.open()) })
}

fun asyncPrepareImageFromPath(
    path: Path,
    original: (InputStream) -> ImageRendererFactory.ImageSupplier
): ImageRendererFactory.ImageSupplier = object : ImageRendererFactory.ImageSupplier {
    override fun completeImage() = AsyncImageRenderer(lazy { original(path.inputStream()) })
}

fun syncCompleteImage(
    original: () -> ImageRenderer
): ImageRenderer =
    if (Minecraft.getInstance().isSameThread) original() else runBlocking(Lazyyyyy.mainThreadContext) { original() }

