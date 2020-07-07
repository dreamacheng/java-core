package acheng.core.classLoader;

public class TestClassLoaderPath {

    public static void main(String[] args) {
        // BootStrap ClassLoader加载的文件
        System.out.println(System.getProperty("sun.boot.class.path"));

        // ExtClassLoader加载的文件
        System.out.println(System.getProperty("java.ext.dirs"));

        // AppClassLoader加载的文件
        System.out.println(System.getProperty("java.class.path"));
    }
}
