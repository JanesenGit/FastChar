package com.fastchar.interfaces;

import com.fastchar.asm.FastMethodRead;
import com.fastchar.asm.FastParameter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法反编译读取接口
 * @author 沈建（Janesen）
 * @date 2020/7/13 09:55
 */
public interface IFastMethodRead {

    /**
     * 获取方法所有的参数列表
     * @param method 方法对象
     * @return
     * @throws Exception
     */
    List<FastParameter> getParameter( Method method) throws Exception;

    /**
     * 获取方法所有的参数列表
     * @param method 方法对象
     * @param numbers 获取
     * @return
     * @throws Exception
     */
    List<FastParameter> getParameter(Method method, List<MethodLine> numbers) throws Exception;

    List<MethodLine> getMethodLineNumber(Class<?> targetClass, final String methodName) throws Exception;


    class MethodLine{
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
