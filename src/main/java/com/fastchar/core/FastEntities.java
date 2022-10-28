package com.fastchar.core;

import com.fastchar.annotation.AFastEntity;
import com.fastchar.exception.FastEntityException;
import com.fastchar.interfaces.IFastMethodRead;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * FastEntity实体类集合
 * @author 沈建（Janesen）
 */
public final class FastEntities {
    private final List<EntityInfo> entityInfos = new ArrayList<>(16);

    FastEntities() {
    }

    public FastEntities addEntity(Class<? extends FastEntity<?>> targetClass) throws Exception {
        if (!FastClassUtils.checkNewInstance(targetClass)) {
            return this;
        }
        if (targetClass.isAnnotationPresent(AFastEntity.class)) {
            AFastEntity aFastEntity = targetClass.getAnnotation(AFastEntity.class);
            if (!aFastEntity.enable()) {
                return this;
            }
        }

        EntityInfo entityInfo = new EntityInfo();
        IFastMethodRead methodConverter = FastChar.getOverrides().newInstance(IFastMethodRead.class);
        List<IFastMethodRead.MethodLine> lineNumber = methodConverter.getMethodLineNumber(targetClass, "getTableName");
        FastEntity<?> fastEntity = FastClassUtils.newInstance(targetClass);
        if (fastEntity == null) {
            return this;
        }

        for (EntityInfo info : entityInfos) {
            if (info.getTableName().equalsIgnoreCase(fastEntity.getTableName())
                    && FastClassUtils.isSameClass(targetClass, info.getTargetClass())) {
                return this;
            }
        }

        String tableName = fastEntity.getTableName();

        if (FastStringUtils.isEmpty(tableName)) {
            StackTraceElement stackTraceElement = new StackTraceElement(targetClass.getName(),
                    "getTableName", targetClass.getSimpleName() + ".java",
                    lineNumber.get(0).getLastLine());
            throw new FastEntityException(FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR1) +
                    "\n\tat " + stackTraceElement);
        }

        entityInfo.setMethodLine(lineNumber.get(0))
                .setTableName(tableName)
                .setTargetClass(targetClass);
        entityInfos.add(entityInfo);
        return this;
    }


    /**
     * 根据FastEntity类获取EntityInfo
     * @param targetClass 目标类
     * @return EntityInfo
     */
    public EntityInfo getEntityInfo(Class<? extends FastEntity<?>> targetClass) {
        for (EntityInfo entityInfo : entityInfos) {
            if (FastClassUtils.isSameClass(entityInfo.getTargetClass(), targetClass)) {
                return entityInfo;
            }
        }
        return null;
    }

    /**
     * 根据表格获取EntityInfo集合，因为一个表格可能被多个FastEntity类绑定
     * @param tableName 表格名称
     * @return EntityInfo集合
     */
    public List<EntityInfo> getEntityInfo(String tableName) {
        List<EntityInfo> entityInfoList = new ArrayList<>(5);
        for (EntityInfo entityInfo : entityInfos) {
            if (entityInfo.getTableName().equalsIgnoreCase(tableName)) {
                entityInfoList.add(entityInfo);
            }
        }
        return entityInfoList;
    }

    /**
     * 根据表格获取第一个EntityInfo，因为一个表格可能被多个FastEntity类绑定
     * @param tableName 表格名称
     * @return 第一个EntityInfo
     */
    public EntityInfo getFirstEntityInfo(String tableName) {
        for (EntityInfo entityInfo : entityInfos) {
            if (entityInfo.getTableName().equalsIgnoreCase(tableName)) {
                return entityInfo;
            }
        }
        return null;
    }

    public List<EntityInfo> getEntityInfos() {
        return entityInfos;
    }


    public void flush() {
        List<EntityInfo> waitRemove = new ArrayList<>(16);
        for (EntityInfo entityInfo : entityInfos) {
            if (FastClassUtils.isRelease(entityInfo.getTargetClass())) {
                waitRemove.add(entityInfo);
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastEntities.class,
                            FastChar.getLocal().getInfo(FastCharLocal.ENTITY_ERROR5, entityInfo.getTargetClass()));
                }
            }
        }
        entityInfos.removeAll(waitRemove);
    }


    public static class EntityInfo {
        private String tableName;
        private IFastMethodRead.MethodLine methodLine;
        private Class<? extends FastEntity<?>> targetClass;

        public String getTableName() {
            return tableName;
        }

        public EntityInfo setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public IFastMethodRead.MethodLine getMethodLine() {
            return methodLine;
        }

        public EntityInfo setMethodLine(IFastMethodRead.MethodLine methodLine) {
            this.methodLine = methodLine;
            return this;
        }

        public Class<? extends FastEntity<?>> getTargetClass() {
            return targetClass;
        }

        public EntityInfo setTargetClass(Class<? extends FastEntity<?>> targetClass) {
            this.targetClass = targetClass;
            return this;
        }
    }
}
