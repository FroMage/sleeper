package fr.epardaud.stephane;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassSleeper implements ClassFileTransformer {

    public final static int MAX_SLEEP_MS = 10;
    
    public byte[] transform(ClassLoader loader, final String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if((!className.startsWith("io/quarkus/resteasy/reactive/")
                && !className.startsWith("org/jboss/resteasy/reactive/"))
                || className.contains("/deployment/")
                || className.endsWith("BuildItem")
                || className.endsWith("Recorder")) {
            return classfileBuffer;
        }
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, final String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if(name.equals("<init>")
                        || name.equals("<clinit>")
                        || name.startsWith("access$")
                        || name.startsWith("lambda$")){
                    return methodVisitor;
                }
                return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
                    @Override
                    public void visitCode() {
                        super.visitCode();
                        System.err.println("Visiting method "+className+"."+name);
                        visitMethodInsn(Opcodes.INVOKESTATIC, Math.class.getName().replace('.', '/'), "random", "()D", false);
                        visitLdcInsn(new Double(MAX_SLEEP_MS));
                        visitInsn(Opcodes.DMUL);
                        visitMethodInsn(Opcodes.INVOKESTATIC, Math.class.getName().replace('.', '/'), "round", "(D)J", false);
                        visitMethodInsn(Opcodes.INVOKESTATIC, Thread.class.getName().replace('.', '/'), "sleep", "(J)V", false);
                    }
                };
            }
        }, 0);
        
        return writer.toByteArray();
    }

}
