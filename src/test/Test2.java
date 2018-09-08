package test;

/**
 * Created by 11256 on 2018/9/6.
 * 字符串的测试
 */
public class Test2 {

    public static void main(String[] args) {

        String str1 = new StringBuilder("hello").append("Word").toString();
        String str2 = new StringBuilder("Ja").append("va").toString();

        System.out.println(str1.intern() == str1);
        System.out.println(str2.intern() == str2);

        String str3 = "helloWord";//直接指向方法区(常量池)
        StringBuilder str4 = new StringBuilder("helloWord");//指向堆内存空间
        String str5 = new String("helloWord");//指向堆内存空间


//        System.out.println(str1.intern() == str1);
//        System.out.println(str2.intern() == str2);

        System.out.println(str3 == str4.toString());
        System.out.println(str3.equals(str4));
        System.out.println(str3.equals(str4.toString()));
        System.out.println(str3.equals(str5));


        System.out.println("比较结果为:" + "123" == "123");
        Object o1 = new Object();
        System.out.println("比较结果为:" + o1 == o1);//false
//        T t = new T();
//        System.out.println("比较结果为:" + t == t);
    }

    static class T {}

}
