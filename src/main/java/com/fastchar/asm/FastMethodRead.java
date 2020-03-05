package com.fastchar.asm;


import com.sun.xml.internal.ws.org.objectweb.asm.ClassReader;
import com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter;
import com.sun.xml.internal.ws.org.objectweb.asm.Label;
import com.sun.xml.internal.ws.org.objectweb.asm.MethodVisitor;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FastMethodRead {

    public List<FastParameter> getParameter(final Method method) throws Exception {
        return getParameter(method, null);
    }
    /**
     * 获得方法参数
     *
     * @param method
     * @return
     */
    public List<FastParameter> getParameter(final Method method,final List<MethodLine> numbers) throws Exception {
        final List<FastParameter> parameters = new ArrayList<FastParameter>();
        String className = method.getDeclaringClass().getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        String classPath = className.replace('.', '/') + ".class";
        InputStream resourceAsStream = method.getDeclaringClass().getClassLoader().getResourceAsStream(classPath);
        if (resourceAsStream == null) {
            return parameters;
        }
        if (numbers != null) {
            numbers.clear();
        }
        ClassReader reader = new ClassReader(resourceAsStream);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(new FastClassAdapter(classWriter) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                             final String signature, final String[] exceptions) {
                MethodVisitor superMethodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals(method.getName())) {
                    parameters.clear();
                    final MethodLine methodLine = new MethodLine();
                    if (numbers != null) {
                        numbers.add(methodLine);
                    }
                    return new FastMethodAdapter(superMethodVisitor) {
                        int localIndex = 0;

                        public void visitLocalVariable(String name1, String desc1, String signature1, Label start, Label end, int index) {
                            super.visitLocalVariable(name1, desc1, signature1, start, end, index);
                            if (name1.equals("this")) {
                                return;
                            }
                            if (localIndex >= parameterTypes.length) {//非方法参数
                                return;
                            }
                            FastParameter parameter = new FastParameter();
                            parameter.setName(name1);
                            parameter.setIndex(localIndex);
                            parameter.setType(parameterTypes[parameter.getIndex()]);
                            parameter.setParameterizedType(genericParameterTypes[parameter.getIndex()]);
                            if (parameter.getIndex() < parameterAnnotations.length) {
                                parameter.setAnnotations(parameterAnnotations[parameter.getIndex()]);
                            }
                            parameters.add(parameter);
                            localIndex++;
                        }

                        @Override
                        public void visitLineNumber(int line, Label start) {
                            super.visitLineNumber(line, start);
                            methodLine.setFirstLine(Math.min(methodLine.getFirstLine(), line));
                            methodLine.setLastLine(Math.max(methodLine.getLastLine(), line));
                        }
                    };
                }
                return superMethodVisitor;
            }
        }, 0);
        if (numbers != null && numbers.size() == 0) {
            numbers.add(new MethodLine().setFirstLine(1).setLastLine(1));
        }
        return parameters;
    }


    public List<MethodLine> getMethodLineNumber(Class<?> targetClass, final String methodName) throws Exception {
        final List<MethodLine> numbers = new ArrayList<MethodLine>();
        String className = targetClass.getName();
        String classPath = className.replace('.', '/') + ".class";
        InputStream resourceAsStream = targetClass.getClassLoader().getResourceAsStream(classPath);
        if (resourceAsStream == null) {
            numbers.add(new MethodLine().setFirstLine(1).setLastLine(1));
            return numbers;
        }
        ClassReader reader = new ClassReader(resourceAsStream);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(new FastClassAdapter(classWriter) {

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc,
                                             final String signature, final String[] exceptions) {
                MethodVisitor superMethodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals(methodName)) {
                    final MethodLine methodLine = new MethodLine();
                    numbers.add(methodLine);
                    return new FastMethodAdapter(superMethodVisitor) {
                        @Override
                        public void visitLineNumber(int line, Label start) {
                            super.visitLineNumber(line, start);
                            methodLine.setFirstLine(Math.min(methodLine.getFirstLine(), line));
                            methodLine.setLastLine(Math.max(methodLine.getLastLine(), line));
                        }
                    };
                }
                return superMethodVisitor;
            }
        }, 0);
        if (numbers.size() == 0) {
            numbers.add(new MethodLine().setFirstLine(1).setLastLine(1));
        }
        return numbers;
    }

    public static class MethodLine{
        private int firstLine = Integer.MAX_VALUE;
        private int lastLine;

        public int getFirstLine() {
            return firstLine;
        }

        public MethodLine setFirstLine(int firstLine) {
            this.firstLine = firstLine;
            return this;
        }

        public int getLastLine() {
            return lastLine;
        }

        public MethodLine setLastLine(int lastLine) {
            this.lastLine = lastLine;
            return this;
        }
    }
}
