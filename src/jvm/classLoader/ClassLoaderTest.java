package jvm.classLoader;

/**
 * Created by 11256 on 2018/9/5.
 * 测试  类的加载器
 */
public class ClassLoaderTest {

    public static void main(String[] args) {
        ClassLoader c = ClassLoaderTest.class.getClassLoader();
        while (c != null){
            System.out.println(c);
            c = c.getParent();
        }
        System.out.println(System.getProperty("java.class.path"));
        System.out.println(System.getProperty("java.ext.dirs"));
    }

}
