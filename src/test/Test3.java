package test;

/**
 * Created by 11256 on 2018/9/10.
 * 测试3
 */
public class Test3 {
    public static void main(String[] args) throws Exception {
        A a = new A();
        a.mathodA();
        Class clazz = A.class;
        A aa = (A) clazz.newInstance();
        aa.mathodA();

        Class clazz1 = a.getClass();
        A aa1 = (A) clazz1.newInstance();
        aa1.mathodA();
    }

    static class A{
        void mathodA(){
            System.out.println("我是A类中的方法...");
        }
    }

}
