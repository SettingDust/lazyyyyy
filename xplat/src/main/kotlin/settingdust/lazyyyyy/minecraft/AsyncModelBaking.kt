package settingdust.lazyyyyy.minecraft

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.util.RandomSource
import org.joml.Vector3f
import settingdust.lazyyyyy.Lazyyyyy
import kotlin.time.measureTimedValue

@Suppress("UsePropertyAccessSyntax", "HasPlatformType")
class AsyncModelPart(val provider: () -> ModelPart) : ModelPart(emptyList(), emptyMap()) {
    private val loading = Lazyyyyy.scope.async(start = CoroutineStart.LAZY) { provider() }
    val wrapped by lazy {
        val value = measureTimedValue { runBlocking { loading.await() } }
        Lazyyyyy.logger.debug("ModelPart {} loaded in {}ms", value.value, value.duration)
        value.value
    }

    override fun storePose() = wrapped.storePose()

    override fun getInitialPose() = wrapped.getInitialPose()

    override fun setInitialPose(partPose: PartPose) = wrapped.setInitialPose(partPose)

    override fun resetPose() = wrapped.resetPose()

    override fun loadPose(partPose: PartPose) = wrapped.loadPose(partPose)

    override fun copyFrom(modelPart: ModelPart) = wrapped.copyFrom(modelPart)

    override fun hasChild(string: String) = wrapped.hasChild(string)

    override fun getChild(string: String) = wrapped.getChild(string)

    override fun setPos(f: Float, g: Float, h: Float) = wrapped.setPos(f, g, h)

    override fun setRotation(f: Float, g: Float, h: Float) = wrapped.setRotation(f, g, h)

    override fun render(poseStack: PoseStack, vertexConsumer: VertexConsumer, i: Int, j: Int) =
        wrapped.render(poseStack, vertexConsumer, i, j)

    override fun render(
        poseStack: PoseStack,
        vertexConsumer: VertexConsumer,
        i: Int,
        j: Int,
        f: Float,
        g: Float,
        h: Float,
        k: Float
    ) = wrapped.render(poseStack, vertexConsumer, i, j, f, g, h, k)

    override fun visit(poseStack: PoseStack, visitor: Visitor) = wrapped.visit(poseStack, visitor)

    override fun translateAndRotate(poseStack: PoseStack) = wrapped.translateAndRotate(poseStack)

    override fun getRandomCube(randomSource: RandomSource) = wrapped.getRandomCube(randomSource)

    override fun isEmpty() = wrapped.isEmpty()

    override fun offsetPos(vector3f: Vector3f) = wrapped.offsetPos(vector3f)

    override fun offsetRotation(vector3f: Vector3f) = wrapped.offsetRotation(vector3f)

    override fun offsetScale(vector3f: Vector3f) = wrapped.offsetScale(vector3f)

    override fun getAllParts() = wrapped.getAllParts()
}