package settingdust.lazyyyyy.minecraft.pack_resources_cache

import net.lenni0451.reflect.Methods
import java.nio.file.FileSystem
import java.nio.file.Path

val ZipFileSystemClass: Class<*> = Class.forName("jdk.nio.zipfs.ZipFileSystem")

private val getZipFileMethod = Methods.getDeclaredMethod(ZipFileSystemClass, "getZipFile")

fun FileSystem.getZipFile() = Methods.invoke(this, getZipFileMethod) as Path