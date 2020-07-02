package com.fastchar.core;

import com.fastchar.annotation.AFastEntity;
import com.fastchar.asm.FastMethodRead;
import com.fastchar.database.info.FastTableInfo;
import com.fastchar.exception.FastEntityException;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;
import net.sf.cglib.reflect.FastClass;

import java.util.ArrayList;
import java.util.List;

/**
 * FastEntity实体类集合
 * @author 沈建（Janesen）
 */
public final class FastEntities {
    private final FastMethodRead methodConverter = new FastMethodRead();
    private final List<EntityInfo> entityInfos = new ArrayList<>();

     FastEntities() {
    }

    public FastEntities addEntity(Class<? extends FastEntity<?>> targetClass) throws Exception {
        if (!FastClassUtils.checkNewInstance(targetClass)) {
            return this;
        }
        if (targetClass.isAnnotationPresent(AFastEntity.class)) {
            AFastEntity aFastEntity = targetClass.getAnnotation(AFastEntity.class);
            if (!aFastEntity.value()) {
                return this;
            }
        }

        EntityInfo entityInfo = new EntityInfo();
        List<FastMethodRead.MethodLine> lineNumber = methodConverter.getMethodLineNumber(targetClass, "getTableName");
        FastEntity<?> fastEntity = FastClassUtils.newInstance(targetClass);
        if (fastEntity == null) {
            return this;
        }
        String tableName = fastEntity.getTableName();

        if (FastStringUtils.isEmpty(tableName)) {
            StackTraceElement stackTraceElement = new StackTraceElement(targetClass.getName(),
                    "getTableName", targetClass.getSimpleName() + ".java",
                    lineNumber.get(0).getLastLine());
            throw new FastEntityException(FastChar.getLocal().getInfo("Entity_Error1") +
                    "\n\tat " + stackTraceElement);
        }
        entityInfo.setMethodLine(lineNumber.get(0))
                .setTableName(tableName)
                .setDatabaseName(fastEntity.getDatabase())
                .setTargetClass(targetClass);
        entityInfos.add(entityInfo);
        return this;
    }


    private void checkTableExists() throws Exception {
        for (EntityInfo entityInfo : entityInfos) {
            FastTableInfo<?> tableInfo = FastChar.getDatabases().get(entityInfo.getDatabaseName()).getTableInfo(entityInfo.getTableName());
            if (tableInfo == null) {
                Class<? extends FastEntity<?>> aClass = entityInfo.getTargetClass();
                StackTraceElement stackTraceElement = new StackTraceElement(aClass.getName(),
                        "getTableName", aClass.getSimpleName() + ".java",
                        entityInfo.getMethodLine().getLastLine());
                throw new FastEntityException(FastChar.getLocal().getInfo("Entity_Error2", "'" + entityInfo.getTableName() + "'") +
                        "\n\tat " + stackTraceElement);
            }
        }
    }


    public EntityInfo getEntityInfo(Class<? extends FastEntity<?>> targetClass) {
        for (EntityInfo entityInfo : entityInfos) {
            if (entityInfo.getTargetClass() == targetClass) {
                return entityInfo;
            }
        }
        return null;
    }

    public List<EntityInfo> getEntityInfo(String tableName) {
        List<EntityInfo> entityInfoList = new ArrayList<>();
        for (EntityInfo entityInfo : entityInfos) {
            if (entityInfo.getTableName().equalsIgnoreCase(tableName)) {
                entityInfoList.add(entityInfo);
            }
        }
        return entityInfoList;
    }

    public void flush() {
        List<EntityInfo> waitRemove = new ArrayList<>();
        for (EntityInfo entityInfo : entityInfos) {
            if (FastClassUtils.isRelease(entityInfo.getTargetClass())) {
                waitRemove.add(entityInfo);
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastEntities.class,
                            FastChar.getLocal().getInfo("Entity_Error5",entityInfo.getTargetClass()));
                }
            }
        }
        entityInfos.removeAll(waitRemove);
    }


    public static class EntityInfo {
        private String databaseName;
        private String tableName;
        private FastMethodRead.MethodLine methodLine;
        private Class<? extends FastEntity<?>> targetClass;

        public String getTableName() {
            return tableName;
        }

        public EntityInfo setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public FastMethodRead.MethodLine getMethodLine() {
            return methodLine;
        }

        public EntityInfo setMethodLine(FastMethodRead.MethodLine methodLine) {
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

        public String getDatabaseName() {
            return databaseName;
        }

        public EntityInfo setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }
    }
}
