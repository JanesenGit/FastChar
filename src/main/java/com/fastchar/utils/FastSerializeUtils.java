package com.fastchar.utils;

import java.io.*;

public class FastSerializeUtils {

    public static byte[] serialize(Object object) {
        try {
            if (object == null) {
                return null;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            objectOutputStream.close();
            return bytes;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object deserialize(byte[] bytes) {
        try {
            if (bytes == null) {
                return null;
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (Exception ignored) {
        }
        return null;
    }

}
