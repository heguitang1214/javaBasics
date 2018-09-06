package jvm.classLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author he_guitang
 * @version [1.0 , 2018/7/24]
 *          自定义的类加载器
 */
public class MyClassLoader extends ClassLoader {

    private String name;// 类加载器的名字
    private String path = "d:\\";// 加载类的路径
    private final String fileType = ".class";// class文件的扩展名

    public MyClassLoader(String name) {
        super();// 让系统类加载器成为该类加载器的父加载器
        this.name = name;
    }

    public MyClassLoader(ClassLoader parent, String name) {
        super(parent);// 显示指定该类加载器的父加载器
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }


    /**
     * 当loadClass()方法中父加载器加载失败后，则会调用自己的findClass()方法来完成类加载，
     * 这样就可以保证自定义的类加载器也符合双亲委托模式。
     * 需要注意的是ClassLoader类中并没有实现findClass()方法的具体代码逻辑，
     * 取而代之的是抛出ClassNotFoundException异常(即:双亲委派后,找不到加载该类的加载器)
     */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.loadClassData(name);
        /*
            defineClass将字节数组转换成Class对象
            defineClass()方法是用来将byte字节流解析成JVM能够识别的Class对象(ClassLoader中已实现该方法逻辑)，
            通过这个方法不仅能够通过class文件实例化class对象，也可以通过其他方式实例化class对象:
            如通过网络接收一个类的字节码，然后转换为byte字节流创建对应的Class对象，
            defineClass()方法通常与findClass()方法一起使用，
            一般情况下，在自定义类加载器时，会直接覆盖ClassLoader的findClass()方法并编写加载规则，
            获取到要加载类的字节码后将其转换成流，然后调用defineClass()方法生成类的Class对象
         */
        return this.defineClass(name, data, 0, data.length);
    }

    //将字节码文件转换成byte[]数组
    private byte[] loadClassData(String name) {
        InputStream is = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            // 将.装换成\
            this.name = this.name.replace(".", "\\");
            String pathLoader = path + name + fileType;
            System.out.println(pathLoader);
            is = new FileInputStream(new File(path + name + fileType));
            baos = new ByteArrayOutputStream();
            int ch = 0;
            while (-1 != (ch = is.read())) {
                baos.write(ch);
            }
            data = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (baos != null) baos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return data;
    }


    /**
     * java自己实现的是双亲委托机制
     * 重写loadClass,修改其中的逻辑,就会破坏java的双亲委派机制
     */
//    @Override
//    public Class<?> loadClass(String name) throws ClassNotFoundException {
//        Class<?> clazz = null;
//        ClassLoader system = getSystemClassLoader();
//        try {
//            clazz = system.loadClass(name);
//        } catch (Exception e) {
//            // ignore
//        }
//        if (clazz != null)
//            return clazz;
//        clazz = findClass(name);
//        return clazz;
//    }


    /**
     * 该方法在这抛出了异常,如果Simple在loader1加载器的路径下,而Dog在loader2的加载路径下
     * 会出现java.io.FileNotFoundException的异常,找不到Dog.class
     */
    public static void main(String[] args) throws Exception {
        //默认的父加载器是系统加载器
        MyClassLoader loader1 = new MyClassLoader("loader1");
//        loader1.setPath("d:\\myapp\\serverlib\\");
        loader1.setPath("D:\\workSoftware\\Java\\gitWorkspace\\javaBasics\\out\\production\\javaBasics\\jvm\\");
        //D:/myjava/javaBasics/out/production/javaBasics/
//        MyClassLoader loader2 = new MyClassLoader(loader1, "loader2");
//        loader2.setPath("d:\\myapp\\clientlib\\");
//        loader2.setPath("D:\\myjava\\javaBasics\\out\\production\\javaBasics\\jvm\\");
            /*
            MyClassLoader loader3 = new MyClassLoader(null,"loader3");
			loader3.setPath("d:\\myapp\\otherlib\\");
			test(loader2);
			System.out.println("------------");
			test(loader3);*/

			/*强转(失败)
			Class clazz = loader1.loadClass("Simple");
			Object object = clazz.newInstance();//创建一个Simple类的对象
			Simple simple = (Simple)object;*/

//			反射实现
			/*Class clazz = loader1.loadClass("Simple");
			Object object = clazz.newInstance();//创建一个Simple类的对象
			Field field = clazz.getField("vl");
			int v1 = field.getInt(object);
			System.out.println("v1: = "+v1);*/
        System.getProperty("java.classpath");

        String path = MyClassLoader.class.getResource("/").toString();
        System.out.println(path);

        Class clazz = loader1.loadClass("Simple");
        System.out.println(clazz.hashCode());
        Object object = clazz.newInstance();
//        loader1 = null;
//        clazz = null;
//        object = null;
        loader1 = new MyClassLoader("loader1");
//        loader1.setPath("d:\\myapp\\serverlib\\");
        clazz = loader1.loadClass("Simple");
        System.out.println(clazz.hashCode());
    }

//    public static void test(ClassLoader loader) throws Exception {
//        Class clazz = loader.loadClass("Simple");
//        Object object = clazz.newInstance();
//
//    }


}
