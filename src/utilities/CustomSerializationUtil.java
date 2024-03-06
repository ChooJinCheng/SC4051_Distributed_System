package utilities;

import message.BaseMessage;
import models.MonitorClient;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * This serialization utility implements the marshalling of a java object to bytes array
 * and unmarshalling of bytes array back to the java object
 * When introducing new field type, need to implement it in both Marshalling and Unmarshalling
 * */
public class CustomSerializationUtil {
    /*
     * This method takes in a java object where all the fields of the given object including the superclass will be iterated
     * and be marshalled into bytes which will then be concatenated into a long series of bytes
     * */
    public static byte[] marshal(Object obj) throws IllegalAccessException {
        List<byte[]> fieldBytes = new ArrayList<>();
        //Obtain all the fields that this object has
        for (Field field : getAllFields(obj.getClass())) {
            //Ensure the field is allowed to be serialized
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                //Obtain value of the field for the object
                Object value = field.get(obj);
                //Pass to marshal field value for type sorting
                byte[] valueBytes = marshalFieldValue(value);
                if (valueBytes != null)
                    //Append marshalled object to the list
                    fieldBytes.add(valueBytes);
            }
        }
        return concatenateByteArrays(fieldBytes);
    }

    /*
     * This method takes in ByteBuffer containing the received bytes and the object that the bytes will unmarshall to
     * */
    public static void unmarshal(Object obj, ByteBuffer buffer) throws IllegalAccessException {
        unmarshalObject(obj, buffer);
    }

    /*
     * This method takes in ByteBuffer containing the received bytes and unmarshall it as a String
     * */
    public static String unmarshalStringAttribute(ByteBuffer buffer){
        int length = buffer.getInt();
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);

        return new String(stringBytes);
    }

    /*
     * This method takes in the Field object value and marshall primitive data types
     * Allocating a fixed length of bytes for Integer, Long etc. Since String length is dynamic
     * A String is processed with their string length is allocated integer fixed bytes followed by the value of the String bytes.
     * This is processed in this way so that we can recognize the end of the String bytes and the start of the next data type by obtaining
     * the length of the String first.
     * */
    private static byte[] marshalFieldValue(Object value) throws IllegalAccessException {
        //Bytes allocation for primitive data types
        if (value instanceof Integer) {
            return ByteBuffer.allocate(Integer.BYTES).putInt((Integer) value).array();
        }
        else if (value instanceof Long) {
            return ByteBuffer.allocate(Long.BYTES).putLong((Long) value).array();
        }
        else if (value instanceof String) {
            //Length of the String is obtained first and converted to bytes
            byte[] stringBytes = ((String) value).getBytes();
            byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(stringBytes.length).array();
            //The length of the String is placed in the list first followed by the String value
            List<byte[]> contentBytes = Arrays.asList(lengthBytes, stringBytes);
            //The 2 values are then concatenated into a longer byte[] and returned
            return concatenateByteArrays(contentBytes);
        } else if (value instanceof BaseMessage || value instanceof MonitorClient) {
            //For all object field, a recursive call to the method marshal ensure return of the bytes for the object once it has reached the object's primitive data
            return marshal(value);
        }
        throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
    }
    /*
     * This method takes in the Object that the bytes will convert into where all the fields of the given object including the superclass will be iterated
     * and ByteBuffer containing the received bytes.
     * */
    private static void unmarshalObject(Object obj, ByteBuffer buffer) throws IllegalAccessException {
        for (Field field : getAllFields(obj.getClass())) {
            if (!Modifier.isTransient(field.getModifiers())) {
                field.setAccessible(true);
                //The field type of the object and the received bytes will be passed into unmarshallFieldValue to be parsed into the object data
                Object value = unmarshalFieldValue(field.getType(), buffer);
                field.set(obj, value);
            }
        }
    }
    /*
     * This method takes in the Object fieldType and the ByteBuffer containing the received bytes and
     * unmarshall the bytes into the field type indicated by the object. Since the object is marshalled in a sequence,
     * the same is applicable to unmarshalling which ensures that the unmarshalled bytes must be for the object field type
     * */
    private static Object unmarshalFieldValue(Class<?> fieldType, ByteBuffer buffer) throws IllegalAccessException {
        if (fieldType == int.class || fieldType == Integer.class) {
            //Reads in the next 4 bytes for int data
            return buffer.getInt();
        } else if (fieldType == String.class) {
            //Reads in the length of the String first as mentioned earlier in the marshalling section
            int length = buffer.getInt();
            byte[] stringBytes = new byte[length];
            //Obtain the remaining of the String bytes with the String length
            buffer.get(stringBytes);
            return new String(stringBytes);
        }
        else if (fieldType == long.class || fieldType == Long.class) {
            //Reads in the next 8 bytes for long data
            return buffer.getLong();
        }
        else if (fieldType == MonitorClient.class) {
            MonitorClient newMonitorClient = new MonitorClient();
            //For all object field, a recursive call to the method unmarshal ensure return of the object once it has reached the object's primitive fields
            unmarshalObject(newMonitorClient, buffer);
            return newMonitorClient;
        }
        throw new IllegalArgumentException("Unsupported data type: " + fieldType.getName());
    }
    /*
     * This method takes in a list of bytes array and allocate all the byte[] in the list into a buffer
     * */
    private static byte[] concatenateByteArrays(List<byte[]> byteArrays) {
        int totalLength = byteArrays.stream().mapToInt(bytes -> bytes.length).sum();
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        for (byte[] bytes : byteArrays) {
            buffer.put(bytes);
        }
        return buffer.array();
    }
    /*
     * This method takes in object class type and obtain all the fields in the class
     * */
    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null) {
            //Add all the declared fields of the class into the field list
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            //traverse to the superclass of the given class and repeat
            type = type.getSuperclass();
        }
        return fields;
    }
}
