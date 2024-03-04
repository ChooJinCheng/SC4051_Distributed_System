package utilities;

import message.BaseMessage;
import models.Metadata;
import models.MonitorClient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * When introducing new field type, need to implement it in both Marshalling and Unmarshalling
 *
 * */
public class CustomSerializationUtil {
    public static byte[] marshal(Object obj) throws IllegalAccessException {
        List<byte[]> fieldBytes = new ArrayList<>();
        for (Field field : getAllFields(obj.getClass())) {
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                Object value = field.get(obj);
                byte[] valueBytes = marshalFieldValue(value);
                if (valueBytes != null)
                    fieldBytes.add(valueBytes);
            }
        }
        return concatenateByteArrays(fieldBytes);
    }

    public static void unmarshal(Object obj, ByteBuffer buffer) throws IllegalAccessException {
        unmarshalObject(obj, buffer);
    }

    public static String unmarshalStringAttribute(ByteBuffer buffer){
        int length = buffer.getInt();
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);

        return new String(stringBytes);
    }

    private static byte[] marshalFieldValue(Object value) throws IllegalAccessException {
        // Implement custom logic for marshalling different types
        if (value instanceof Integer) {
            return ByteBuffer.allocate(Integer.BYTES).putInt((Integer) value).array();
        }
        else if (value instanceof Long) {
            return ByteBuffer.allocate(Long.BYTES).putLong((Long) value).array();
        }
        else if (value instanceof String) {
            byte[] stringBytes = ((String) value).getBytes();
            byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(stringBytes.length).array();
            List<byte[]> contentBytes = Arrays.asList(lengthBytes, stringBytes);
            return concatenateByteArrays(contentBytes);
        } else if (value instanceof BaseMessage || value instanceof Metadata || value instanceof MonitorClient) {
            return marshal(value);
        }
        throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
    }

    private static Object unmarshalFieldValue(Class<?> fieldType, ByteBuffer buffer) throws IllegalAccessException {
        // Implement custom logic for unmarshalling different types
        if (fieldType == int.class || fieldType == Integer.class) {
            return buffer.getInt();
        } else if (fieldType == String.class) {
            int length = buffer.getInt();
            byte[] stringBytes = new byte[length];
            buffer.get(stringBytes);
            return new String(stringBytes);
        }
        else if (fieldType == long.class || fieldType == Long.class) {
            return buffer.getLong();
        }
        else if (fieldType == Metadata.class) {
            Metadata newMetadata = new Metadata();
            unmarshalObject(newMetadata, buffer);
            return newMetadata;
        } else if (fieldType == MonitorClient.class) {
            MonitorClient newMonitorClient = new MonitorClient();
            unmarshalObject(newMonitorClient, buffer);
            return newMonitorClient;
        }
        throw new IllegalArgumentException("Unsupported data type: " + fieldType.getName());
    }

    private static void unmarshalObject(Object obj, ByteBuffer buffer) throws IllegalAccessException {
        for (Field field : getAllFields(obj.getClass())) {
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                Object value = unmarshalFieldValue(field.getType(), buffer);
                field.set(obj, value);
            }
        }
    }

    private static byte[] concatenateByteArrays(List<byte[]> byteArrays) {
        int totalLength = byteArrays.stream().mapToInt(bytes -> bytes.length).sum();
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        for (byte[] bytes : byteArrays) {
            buffer.put(bytes);
        }
        return buffer.array();
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }
}
