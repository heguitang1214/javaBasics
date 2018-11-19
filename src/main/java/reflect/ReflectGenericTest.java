package reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 反射泛型
 */
public class ReflectGenericTest {

    public static void main(String[] args) {
//        new B();
//        new C();
        new B("字符串");
    }


    static class A<T> {
        //A的构造方法
        public A() {
            //Java中,this是实例中的对象,也就是this指向new的对象;继承时，对于方法覆盖时，new的谁，this就指向谁，由多态性决定.
            //如果是成员变量，this，在哪个类就指向哪个类的成员变量，成员变量没有多态性
            Class clazz = this.getClass();//得到子类类型,this指向的是new B();
            System.out.println("这个this代表的是:" + clazz + ",super代表的是" + super.getClass());
            Type type = clazz.getGenericSuperclass();//获取传递给父类参数化类型
            //就会一个A<String>
            ParameterizedType pType = (ParameterizedType) type;
            //就是一个Class数组
            Type[] types = pType.getActualTypeArguments();
            //就是String
            Class classes = (Class) types[0];
            System.out.println(classes.getName());
        }

        public A(String name) {
            System.out.println(this.getClass());
            this.privateMethod();
        }

        private void privateMethod() {
            System.out.println("这是父类私有的方法:" + this.getClass());
        }

        public void publicMethod() {
            System.out.println("这是父类公有的方法:" + this.getClass());
        }

    }

    static class B extends A<String> {
        public B() {
            System.out.println("这个super()中的super代表的是:" + super.getClass());
        }
        public B(String name) {
            super(name);
            System.out.println("这个super(name)中的super代表的是:" + super.getClass());
        }
    }

    static class C extends A<Integer> {}

}
