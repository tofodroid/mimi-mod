package io.github.tofodroid.mods.mimi.util;

public class MathUtils {
    public static Byte addClamped(Byte value, Integer add, Integer min, Integer max) {
        return addClamped(value.intValue(), add, min, max).byteValue();
    }

    public static Integer addClamped(Integer value, Integer add, Integer min, Integer max) {
        Integer result = value + add;

        if(result < min) {
            result = min;
        } else if(result > max) {
            result = max;
        }

        return result;
    }
}
