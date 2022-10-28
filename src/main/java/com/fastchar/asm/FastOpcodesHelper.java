package com.fastchar.asm;

import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author 沈建（Janesen）
 * @date 2021/5/18 15:45
 */
public class FastOpcodesHelper {

    public static int getLastASM() {
        try {
            List<String> asmList = new ArrayList<>(16);
            Field[] fields = Opcodes.class.getFields();
            for (Field field : fields) {
                if (field.getName().startsWith("ASM")) {
                    asmList.add(field.getName());
                }
            }
            Collections.sort(asmList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o2.compareTo(o1);
                }
            });
            Field field = Opcodes.class.getField(asmList.get(0));
            return field.getInt(Opcodes.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Opcodes.ASM5;
    }


}
