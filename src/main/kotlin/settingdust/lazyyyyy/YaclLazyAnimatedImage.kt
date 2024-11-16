package settingdust.lazyyyyy

import dev.isxander.yacl3.gui.image.ImageRenderer
import dev.isxander.yacl3.gui.image.ImageRendererFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.server.packs.resources.Resource
import java.io.InputStream

class AsyncImageRenderer(val original: Lazy<ImageRendererFactory.ImageSupplier>) : ImageRenderer {
    val image = lazy { original.value.completeImage() }
    private val loadingJob = Lazyyyyy.scope.launch { image.value }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun render(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        renderWidth: Int,
        tickDelta: Float
    ) = if (image.isInitialized()) {
        image.value.render(graphics, x, y, renderWidth, tickDelta)
    } else {
        0
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun close() {
        runBlocking {
            if (loadingJob.isActive)
                loadingJob.cancelAndJoin()
        }
        image.value.close()
    }
}

fun asyncPrepareWEBP(
    resource: Resource,
    original: (InputStream) -> ImageRendererFactory.ImageSupplier
): ImageRendererFactory.ImageSupplier = object : ImageRendererFactory.ImageSupplier {
    override fun completeImage() = AsyncImageRenderer(lazy { original(resource.open()) })
}

fun syncCompleteWEBP(
    original: () -> ImageRenderer
): ImageRenderer = runBlocking(Lazyyyyy.mainThreadContext) { original.invoke() }