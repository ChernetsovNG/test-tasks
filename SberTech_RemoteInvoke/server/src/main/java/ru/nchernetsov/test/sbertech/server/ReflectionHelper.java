package ru.nchernetsov.test.sbertech.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Вспомогательный класс для работы с Reflection
 */
@SuppressWarnings("SameParameterValue")
public class ReflectionHelper {

    private ReflectionHelper() {
    }

    static <T> T instantiate(Class<T> type, Object... args) throws
        IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (args.length == 0) {
            return type.newInstance();
        } else {
            return type.getConstructor(toClasses(args)).newInstance(args);
        }
    }

    public static Object callMethod(Object object, String name, Object... args)
        throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Method method = null;
        boolean isAccessible = true;
        try {
            method = object.getClass().getDeclaredMethod(name, toClasses(args));
            isAccessible = method.isAccessible();
            method.setAccessible(true);
            return method.invoke(object, args);
        } finally {
            if (method != null && !isAccessible) {
                method.setAccessible(false);
            }
        }
    }

    private static Class<?>[] toClasses(Object[] args) {
        return Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
    }

}
