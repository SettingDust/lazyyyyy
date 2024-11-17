package settingdust.lazyyyyy.forge

import net.minecraftforge.forgespi.language.IModInfo
import java.io.InputStream
import kotlin.io.path.inputStream

fun getResource(modInfo: IModInfo, name: String): InputStream? = try {
    modInfo.owningFile.file.findResource(name)?.inputStream()
} catch (_: Throwable) {
    null
}