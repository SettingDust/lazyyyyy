package settingdust.lazyyyyy.faster_mixin.cache;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class MixinCacheUtil {
    public static byte[] getClassBytes(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void cleanClassNode(ClassNode node) {
        node.interfaces.clear();
        node.visibleAnnotations = null;
        node.invisibleAnnotations = null;
        node.visibleTypeAnnotations = null;
        node.invisibleTypeAnnotations = null;
        node.attrs = null;
        node.innerClasses.clear();
        node.nestHostClass = null;
        node.nestMembers = null;
        node.permittedSubclasses = null;
        node.recordComponents = null;
        node.fields.clear();
        node.methods.clear();
    }
}
