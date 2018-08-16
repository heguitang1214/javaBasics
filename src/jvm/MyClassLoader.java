package jvm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author he_guitang
 * @version [1.0 , 2018/7/24]
 *
 * 1.
 *
 *
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

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.loadClassData(name);
        // defineClass将字节数组转换成Class对象
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
                is.close();
                baos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return data;
    }


    /**
     * java自己实现的是双亲委托机制
     * 重写loadClass,修改其中的逻辑,就会破坏java的双亲委派机制
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        ClassLoader system = getSystemClassLoader();
        try {
            clazz = system.loadClass(name);
        } catch (Exception e) {
            // ignore
        }
        if (clazz != null)
            return clazz;
        clazz = findClass(name);
        return clazz;
    }


    /**
     * 该方法在这抛出了异常,如果Simple在loader1加载器的路径下,而Dog在loader2的加载路径下
     * 会出现java.io.FileNotFoundException的异常,找不到Dog.class
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //默认的父加载器是系统加载器
        MyClassLoader loader1 = new MyClassLoader("loader1");
        loader1.setPath("d:\\myapp\\serverlib\\");
        MyClassLoader loader2 = new MyClassLoader(loader1,"loader2");
        loader2.setPath("d:\\myapp\\clientlib\\");
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

			/*反射实现
			Class clazz = loader1.loadClass("Simple");
			Object object = clazz.newInstance();//创建一个Simple类的对象
			Field field = clazz.getField("vl");
			int v1 = field.getInt(object);
			System.out.println("v1: = "+v1);*/

        Class clazz = loader1.loadClass("Simple");
        System.out.println(clazz.hashCode());
        Object object = clazz.newInstance();
        loader1 = null;
        clazz = null;
        object = null;
        loader1 = new MyClassLoader("loader1");
        loader1.setPath("d:\\myapp\\serverlib\\");
        clazz = loader1.loadClass("Simple");
        System.out.println(clazz.hashCode());
    }

    public static void test(ClassLoader loader) throws Exception{
        Class clazz = loader.loadClass("Simple");
        Object object = clazz.newInstance();

    }

    



}
