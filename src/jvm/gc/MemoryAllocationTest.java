package jvm.gc;

/**
 * Created by 11256 on 2018/9/9.
 * 内存分配测试
 */
public class MemoryAllocationTest {

    private static final int _MB = 1024 * 1024;

    public static void main(String[] args) {
//        test1();
        test2();
    }

    /**
     * 大对象直接进入老年代
     * 3145728 = 1024 * 1024 *3
     * 新生代分配:
     * -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:+UseParNewGC -XX:SurvivorRatio=8 -XX:PretenureSizeThreshold=5145728
     * 老年代分配:
     * -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:+UseParNewGC -XX:SurvivorRatio=8 -XX:PretenureSizeThreshold=3145728
     */
    private static void test1(){
        byte[] allocation = new byte[4 * _MB];//1M
    }

    /**
     * 长期存活的对象进入老年代
     * -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:+UseParNewGC -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1 -XX:+PrintTenuringDistribution
     *
     * 添加参数: -XX:TargetSurvivorRatio=90
     */
    private static void test2(){
        byte[] allocation1, allocation2, allocation3;
        allocation1 = new byte[_MB / 4];//0.25M
        allocation2 = new byte[4 * _MB];
        allocation3 = new byte[4 * _MB];
        allocation3 = null;
        allocation3 = new byte[4 * _MB];
    }

}
