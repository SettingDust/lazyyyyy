package settingdust.lazyyyyy

import org.apache.logging.log4j.LogManager
import settingdust.lazyyyyy.util.MinecraftAdapter

object Lazyyyyy {
    const val ID = "lazyyyyy"

    val LOGGER = LogManager.getLogger()

    fun id(path: String) = MinecraftAdapter.id(ID, path)
}