package com.fastchar.asm;

import com.sun.xml.internal.ws.org.objectweb.asm.*;

public class FastClassAdapter implements ClassVisitor {
    protected ClassVisitor cv;

    public FastClassAdapter(ClassVisitor cv) {
        this.cv = cv;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.cv.visit(version, access, name, signature, superName, interfaces);
    }

    public void visitSource(String source, String debug) {
        this.cv.visitSource(source, debug);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        this.cv.visitOuterClass(owner, name, desc);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return this.cv.visitAnnotation(desc, visible);
    }

    public void visitAttribute(Attribute attr) {
        this.cv.visitAttribute(attr);
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        this.cv.visitInnerClass(name, outerName, innerName, access);
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return this.cv.visitField(access, name, desc, signature, value);
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return this.cv.visitMethod(access, name, desc, signature, exceptions);
    }

    public void visitEnd() {
        this.cv.visitEnd();
    }
}
