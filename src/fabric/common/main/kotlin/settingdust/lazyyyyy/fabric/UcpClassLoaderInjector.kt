package settingdust.lazyyyyy.fabric

import net.lenni0451.reflect.JVMConstants
import net.lenni0451.reflect.stream.RStream
import settingdust.lazyyyyy.Lazyyyyy
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URL
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.outputStream

object UcpClassLoaderInjector {
    fun inject(
        path: Path,
        classLoader: ClassLoader
    ) {
        val cachePath = Path.of(".cache", Lazyyyyy.ID)
        try {
            Files.createDirectories(cachePath)
        } catch (_: IOException) {
        }
        val tempFile: Path
        try {
            tempFile = Files.createTempFile(cachePath, "_nested", ".tmp")
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to create a temporary file for nested jar in $cachePath: $e"
            )
        }
        try {
            val hash =
                extractEmbeddedJarFile(path, tempFile)
            val finalPath = cachePath.resolve("$hash/${path.name}")
            // If the file already exists, reuse it, since it might already be opened.
            if (!Files.isRegularFile(finalPath)) {
                moveExtractedFileIntoPlace(tempFile, finalPath)
            }
            try {
                appendToClassLoader(finalPath.toUri().toURL(), classLoader)
            } catch (e: Throwable) {
                throw RuntimeException(e)
            }
        } finally {
            try {
                Files.deleteIfExists(tempFile)
            } catch (e: IOException) {
                Lazyyyyy.LOGGER.error("Failed to remove temporary file {}: {}", tempFile, e)
            }
        }
    }

    @Throws(Throwable::class)
    fun appendToClassLoader(url: URL?, classLoader: ClassLoader) {
        val ucpField = RStream.of(classLoader.javaClass).withSuper().fields().by(JVMConstants.FIELD_URLClassLoader_ucp)
        val ucp: Any = ucpField.get(classLoader)
        RStream.of(ucp).methods().by(JVMConstants.METHOD_URLClassPath_addURL).invokeInstance<Unit>(ucp, url)
    }

    private fun extractEmbeddedJarFile(
        path: Path,
        destination: Path
    ): String? {
        try {
            path.inputStream().use { inStream ->
                destination.outputStream().use { outStream ->
                    val digest: MessageDigest
                    try {
                        digest = MessageDigest.getInstance("SHA-256")
                    } catch (e: NoSuchAlgorithmException) {
                        throw RuntimeException("Missing default JCA algorithm SHA-256.", e)
                    }

                    val digestOut = DigestOutputStream(outStream, digest)
                    inStream.transferTo(digestOut)
                    return HexFormat.of().formatHex(digest.digest())
                }
            }
        } catch (e: IOException) {
            Lazyyyyy.LOGGER.error(
                "Failed to copy nested jar file {} to {}",
                path,
                destination,
                e
            )
            throw IllegalStateException("Failed to extract file " + path.fileName, e)
        }
    }

    /**
     * Atomically moves the extracted embedded jar file to its final location.
     * If an atomic move is not supported, the file will be moved normally.
     */
    private fun moveExtractedFileIntoPlace(source: Path, destination: Path) {
        try {
            Files.createDirectories(destination.getParent())
        } catch (e: IOException) {
            throw UncheckedIOException(
                "Failed to create parent directory for extracted nested jar file $source at $destination", e
            )
        }

        try {
            try {
                Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (ex: AtomicMoveNotSupportedException) {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: IOException) {
            throw UncheckedIOException(
                "Failed to move temporary nested file $source to its final location $destination", e
            )
        }
    }
}