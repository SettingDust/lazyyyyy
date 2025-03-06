package settingdust.lazyyyyy.forge.core.faster_module;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import settingdust.lazyyyyy.forge.core.LazyyyyyHacksInjector;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ResolverClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
        final ClassLoader loader,
        final String className,
        final Class<?> classBeingRedefined,
        final ProtectionDomain protectionDomain,
        final byte[] classfileBuffer
    ) {
        if (!className.equals("java/lang/module/Resolver")) return null;
        LazyyyyyHacksInjector.LOGGER.info("Transforming Resolver#makeGraph");
        var cr = new ClassReader(classfileBuffer);
        var classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("makeGraph")) continue;
            var counter = 0;
            for (int i = 0; i < method.instructions.size(); i++) {
                // Check if the instruction is a method call to g1.values()
                if (method.instructions.get(i) instanceof final MethodInsnNode methodInsn
                    && methodInsn.name.equals("values")
                    && methodInsn.owner.equals("java/util/Map")) {
                    if (counter++ < 2) continue;
                    // Replace the method call with BootstrapHooks.filterAutomaticModules(g1)
                    method.instructions.set(
                        methodInsn, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "settingdust/lazyyyyy/forge/core/BootstrapHooks",
                            "filterAutomaticModules",
                            "(Ljava/util/Map;)Ljava/util/Collection;",
                            false
                        )
                    );
                }
            }
        }

        var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
