package jvm.gc;

/**
 * Created by 11256 on 2018/9/6.
 * 强引用
 *      -XX:+PrintGC 参看GC回收情况
 *      -XX:+PrintGCDetails 参看GC回收详情
 */
public class StrongReference {

    private static final int MB = 1024 * 1024;
    public Object instance = null;

    private byte[] size = new byte[2 * MB];

    public static void main(String[] args) {
        //强引用
        StrongReference s1 = new StrongReference();
        StrongReference s2 = new StrongReference();
        //循环引用(互相引用)
        s1.instance = s2;
        s2.instance = s1;

        s1 = null;
        s2 = null;

        //按理说,引用计算器不为0,应该不会回收
        System.gc();

    }


}
