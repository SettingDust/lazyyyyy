package settingdust.lazyyyyy.faster_mixin.cache.generator;

import java.io.IOException;
import java.nio.file.Path;

public interface GeneratorCache {
    void load(Path baseDir) throws IOException;
    void save(Path baseDir) throws IOException;
    default boolean isDirty() { return false; }
}
