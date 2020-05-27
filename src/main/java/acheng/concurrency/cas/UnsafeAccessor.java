package acheng.concurrency.cas;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe类不可直接获取，当非jvm加载时抛SecurityException
 * 通过反射获取
 */
public class UnsafeAccessor {

    private static Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static Unsafe getUnsafe() {
        return unsafe;
    }
}
