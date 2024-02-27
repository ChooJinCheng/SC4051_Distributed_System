package utilities;

import message.Metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomSerializationUtil {
    public static byte[] marshal(Object obj) throws IllegalAccessException {
        List<byte[]> fieldBytes = new ArrayList<>();
        for (Field field : getAllFields(obj.getClass())) {
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                Object value = field.get(obj);
                byte[] valueBytes = marshalFieldValue(value);
                if(valueBytes != null)
                    fieldBytes.add(valueBytes);
            }
        }
        return concatenateByteArrays(fieldBytes);
    }

    public static void unmarshal(Object obj, byte[] data) throws IllegalAccessException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        for (Field field : getAllFields(obj.getClass())) {
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                Object value = unmarshalFieldValue(field.getType(), buffer);
                field.set(obj, value);
            }
        }
    }

    private static byte[] marshalFieldValue(Object value) throws IllegalAccessException {
        // Implement custom logic for marshalling different types
        if (value instanceof Integer) {
            return ByteBuffer.allocate(Integer.BYTES).putInt((Integer) value).array();
        } else if (value instanceof String) {
            byte[] stringBytes = ((String) value).getBytes();
            byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(stringBytes.length).array();
            List<byte[]> contentBytes = Arrays.asList(lengthBytes, stringBytes);
            return concatenateByteArrays(contentBytes);
        }else if (value instanceof Metadata){
            return marshal(value);
        }
        // Add more cases as needed for other data types
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
        }else if(fieldType == Metadata.class){
            /*Since object is last in the byteArray simply get remaining or calculate via
            byte[] metadataBytes = new byte[data.length - (4 * Integer.BYTES) - (commandType.length() + filePath.length() + errorMessage.length())];*/
            int length = buffer.remaining();
            byte[] metadataBytes = new byte[length];
            buffer.get(metadataBytes);

            Metadata newMetadata = new Metadata();
            unmarshal(newMetadata, metadataBytes);
            return newMetadata;
        }
        // Add more cases as needed for other data types
        throw new IllegalArgumentException("Unsupported data type: " + fieldType.getName());
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
