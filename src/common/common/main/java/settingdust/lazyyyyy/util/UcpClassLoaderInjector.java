package settingdust.lazyyyyy.util;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import net.lenni0451.reflect.JVMConstants;
import net.lenni0451.reflect.stream.RStream;
import settingdust.lazyyyyy.Lazyyyyy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Predicate;

public final class UcpClassLoaderInjector {
    private UcpClassLoaderInjector() {
    }

    public static void inject(Path path, ClassLoader classLoader) {
        Path cachePath = Path.of(".cache", Lazyyyyy.ID);
        try {
            Files.createDirectories(cachePath);
        } catch (IOException ignored) {
        }
        Path tempFile;
        try {
            tempFile = Files.createTempFile(cachePath, "_nested", ".tmp");
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to create a temporary file for nested jar in " + cachePath + ": " + e
            );
        }
        try {
            String hash = extractEmbeddedJarFile(path, tempFile);
            Path finalPath = cachePath.resolve(hash + "/" + path.getFileName().toString());
            if (!Files.isRegularFile(finalPath)) {
                moveExtractedFileIntoPlace(tempFile, finalPath);
            }
            try {
                appendToClassLoader(finalPath.toUri().toURL(), classLoader);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                Lazyyyyy.LOGGER.error("Failed to remove temporary file {}: {}", tempFile, e);
            }
        }
    }

    public static void appendToClassLoader(URL url, ClassLoader classLoader) throws Throwable {
        var ucpField = RStream.of(classLoader.getClass())
                .withSuper()
                .fields()
                .by(JVMConstants.FIELD_URLClassLoader_ucp);
        Object ucp = ucpField.get(classLoader);
        RStream.of(ucp).methods().by(JVMConstants.METHOD_URLClassPath_addURL).invokeInstance(ucp, url);
    }

    public static void removeFromClassLoader(ClassLoader classLoader, Predicate<URL> predicate) {
        var ucpField = RStream.of(classLoader.getClass())
                .withSuper()
                .fields()
                .by(JVMConstants.FIELD_URLClassLoader_ucp);
        Object ucp = ucpField.get(classLoader);
        removeUrlFromUcp(ucp, predicate);
    }

    public static void replaceInClassLoader(Predicate<URL> predicate, URL newUrl, ClassLoader classLoader) throws Throwable {
        removeFromClassLoader(classLoader, predicate);
        appendToClassLoader(newUrl, classLoader);
    }

    private static void removeUrlFromUcp(Object ucp, Predicate<URL> predicate) {
        RStream.of(ucp)
                .withSuper()
                .fields()
                .by("path")
                .<List<URL>>get().removeIf(predicate);
    }

    private static String extractEmbeddedJarFile(Path path, Path destination) {
        try (var inStream = Files.newInputStream(path);
             var outStream = new HashingOutputStream(Hashing.murmur3_128(), Files.newOutputStream(destination))) {
            inStream.transferTo(outStream);
            return outStream.hash().toString();
        } catch (IOException e) {
            Lazyyyyy.LOGGER.error(
                    "Failed to copy nested jar file {} to {}",
                    path,
                    destination,
                    e
            );
            throw new IllegalStateException("Failed to extract file " + path.getFileName(), e);
        }
    }

    private static void moveExtractedFileIntoPlace(Path source, Path destination) {
        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to create parent directory for extracted nested jar file " + source + " at " + destination,
                    e
            );
        }

        try {
            try {
                Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to move temporary nested file " + source + " to its final location " + destination, e
            );
        }
    }
}
