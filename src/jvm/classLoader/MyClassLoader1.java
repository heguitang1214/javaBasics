package jvm.classLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 11256 on 2018/9/5.
 * 简易的自定义类加载器
 */
public class MyClassLoader1 {

    //自定义的类加载器
    ClassLoader loader = new ClassLoader() {
        @Override
        // 重载loadClass类，达到打断双亲委派的目的
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                // 获取class文件的名字
                String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                // class文件输入流
                InputStream is = getClass().getResourceAsStream(fileName);
                if (is == null) {
                    // 如果流为空，交给父类加载
                    return super.loadClass(name);
                }
                // 定义byte数组
                byte[] b = new byte[is.available()];
                // 把数据读到byte数组中
                is.read(b);
                // 将byte数组转换成一个对象Class<?>
                return defineClass(name, b, 0, b.length);
            } catch (IOException e) {
                throw new ClassNotFoundException();
            }
        }
    };


    


}
