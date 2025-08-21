package anon.ecco.experiment.utils.property;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertyUtils {

    public static Properties loadProperties(String path){
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(path));
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int loadInteger(Properties properties, String propertyName){
        return Integer.parseInt(properties.getProperty(propertyName));
    }

    public static boolean loadBoolean(Properties properties, String propertyName){
        return Boolean.parseBoolean(properties.getProperty(propertyName));
    }

    public static Path loadPath(Properties properties, String propertyName){
        return Paths.get(properties.getProperty(propertyName));
    }

    public static List<String> loadStringList(Properties properties, String propertyName){
        String stringArray = properties.getProperty(propertyName);
        List<String> list =  Arrays.stream(stringArray.split(",")).toList();
        return new LinkedList<>(list);
    }

    public static List<Integer> loadIntegerList(Properties properties, String propertyName){
        String intArrayString = properties.getProperty(propertyName);
        List<Integer> list = Arrays.stream(intArrayString.split(","))
                .map(Integer::valueOf)
                .toList();
        return new LinkedList<>(list);
    }

    public static <T> List<T> loadInstances(Properties properties, String propertyName, Class<T> expectedType) {
        List<T> instances = new LinkedList<>();
        try {
            String classNames = properties.getProperty(propertyName);
            if (classNames == null) {
                throw new RuntimeException(propertyName + " not found in properties file.");
            }
            String[] classArray = classNames.split(",");
            for (String className : classArray) {
                Class<?> clazz = Class.forName(className.trim());
                if (expectedType.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    T instance = (T) clazz.getDeclaredConstructor().newInstance();
                    instances.add(instance);
                } else {
                    throw new RuntimeException(className + " is no subtype of " + expectedType.getName());
                }
            }
        } catch (InvocationTargetException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return instances;
    }
}
