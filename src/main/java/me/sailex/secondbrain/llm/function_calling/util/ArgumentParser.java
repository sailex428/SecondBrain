package me.sailex.secondbrain.llm.function_calling.util;

import java.util.List;
import java.util.Map;

public class ArgumentParser {

    private ArgumentParser() {}

    public static <T> List<T> getList(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value instanceof List) {
            return (List<T>) value;
        }
        throw new IllegalArgumentException("Expected a List for key " + key + " but got " + (value != null ? value.getClass().getSimpleName() : "null"));
    }

    public static int getInt(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value instanceof Integer integer) {
            return integer;
        } else if (value instanceof String intString) {
            try {
                return Integer.parseInt(intString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert " + value + " to int for key " + key);
            }
        }
        throw new IllegalArgumentException("Expected int value for key " + key + " but got " + value);
    }

    public static boolean getBoolean(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        } else if (value instanceof String boolString) {
            if ("true".equalsIgnoreCase(boolString)) {
                return true;
            } else if ("false".equalsIgnoreCase(boolString)) {
                return false;
            }
            throw new IllegalArgumentException("Cannot convert string " + value + " to boolean for key " + key);
        }
        return false;
    }

    public static String getString(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value instanceof String string) {
            return string;
        } else if (value != null) {
            return value.toString();
        }
        throw new IllegalArgumentException("Expected string value for key " + key + " but got null");
    }
}
