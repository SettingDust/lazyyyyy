package settingdust.lazyyyyy.yacl

import dev.isxander.yacl3.gui.image.ImageRenderer
import dev.isxander.yacl3.gui.image.ImageRendererFactory
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.server.packs.resources.Resource
import settingdust.lazyyyyy.Lazyyyyy
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class AsyncImageRenderer(val original: Lazy<ImageRendererFactory.ImageSupplier>) : ImageRenderer {
    private val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) {
        original.value.completeImage()
    }

    init {
        Lazyyyyy.scope.launch { Lazyyyyy.clientLaunched.collectLatest { loading.start() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun render(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        renderWidth: Int,
        tickDelta: Float
    ) = if (loading.isCompleted) {
        loading.getCompleted().render(graphics, x, y, renderWidth, tickDelta)
    } else {
        0
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun close() {
        try {
            if (loading.isCompleted) loading.getCompleted().close()
            loading.cancel()
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

