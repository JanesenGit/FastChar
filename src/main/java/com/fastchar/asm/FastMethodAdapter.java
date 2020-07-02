package com.fastchar.asm;

import jdk.internal.org.objectweb.asm.*;


public class FastMethodAdapter extends MethodVisitor {
    protected MethodVisitor mv;

    public FastMethodAdapter(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
        this.mv = mv;
    }

    public FastMethodAdapter(int api) {
        super(api);
    }

    public FastMethodAdapter(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return this.mv.visitAnnotationDefault();
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return this.mv.visitAnnotation(desc, visible);
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return this.mv.visitParameterAnnotation(parameter, desc, visible);
    }

    public void visitAttribute(Attribute attr) {
        this.mv.visitAttribute(attr);
    }

    public void visitCode() {
        this.mv.visitCode();
    }

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        this.mv.visitFrame(type, nLocal, local, nStack, stack);
    }

    public void visitInsn(int opcode) {
        this.mv.visitInsn(opcode);
    }

    public void visitIntInsn(int opcode, int operand) {
        this.mv.visitIntInsn(opcode, operand);
    }

    public void visitVarInsn(int opcode, int var) {
        this.mv.visitVarInsn(opcode, var);
    }

    public void visitTypeInsn(int opcode, String type) {
        this.mv.visitTypeInsn(opcode, type);
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        this.mv.visitFieldInsn(opcode, owner, name, desc);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        this.mv.visitMethodInsn(opcode, owner, name, desc);
    }

    public void visitJumpInsn(int opcode, Label label) {
        this.mv.visitJumpInsn(opcode, label);
    }

    public void visitLabel(Label label) {
        this.mv.visitLabel(label);
    }

    public void visitLdcInsn(Object cst) {
        this.mv.visitLdcInsn(cst);
    }

    public void visitIincInsn(int var, int increment) {
        this.mv.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        this.mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        this.mv.visitMultiANewArrayInsn(desc, dims);
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.mv.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        this.mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void visitLineNumber(int line, Label start) {
        this.mv.visitLineNumber(line, start);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        this.mv.visitMaxs(maxStack, maxLocals);
    }

    public void visitEnd() {
        this.mv.visitEnd();
    }
}
