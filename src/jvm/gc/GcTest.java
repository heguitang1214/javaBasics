package jvm.gc;

/**
 * Created by 11256 on 2018/9/9.
 *  测试JVM的各种垃圾回收器
 */
public class GcTest {

    public static void main(String[] args) {
        defaultTest();
//        test1();
//        test2();
    }


    /**
     * 1.8默认的垃圾回收器
     *  Parallel Scavenge + Parallel Old
     */
    private static void defaultTest(){
        System.out.println("默认的垃圾回收....");
//        Heap
//        PSYoungGen      total 75776K, used 6538K [0x000000076bb80000, 0x0000000771000000, 0x00000007c0000000)
//        eden space 65024K, 10% used [0x000000076bb80000,0x000000076c1e2ae8,0x000000076fb00000)
//        from space 10752K, 0% used [0x0000000770580000,0x0000000770580000,0x0000000771000000)
//        to   space 10752K, 0% used [0x000000076fb00000,0x000000076fb00000,0x0000000770580000)
//        ParOldGen       total 173568K, used 0K [0x00000006c3200000, 0x00000006cdb80000, 0x000000076bb80000)
//        object space 1 73568K, 0% used [0x00000006c3200000,0x00000006c3200000,0x00000006cdb80000)
//        Metaspace       used 3433K, capacity 4496K, committed 4864K, reserved 1056768K
//        class space    used 374K, capacity 388K, committed 512K, reserved 1048576K
    }


    /**
     * -XX:+PrintGCDetails -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
     * 不支持
     * -XX:+UseParNewGC -XX:+UseParallelOldGC
     * -XX:+UseParNewGC -XX:+UseSerialGC
     */
    private static void test1(){
        System.out.println("新生代[UseParNewGC],老年代[UseConcMarkSweepGC]组合....");
//        Heap
//        par new generation   total 78016K, used 6942K [0x00000006c3200000, 0x00000006c86a0000, 0x00000006f7200000)
//        eden space 69376K,  10% used [0x00000006c3200000, 0x00000006c38c7818, 0x00000006c75c0000)
//        from space 8640K,   0% used [0x00000006c75c0000, 0x00000006c75c0000, 0x00000006c7e30000)
//        to   space 8640K,   0% used [0x00000006c7e30000, 0x00000006c7e30000, 0x00000006c86a0000)
//        concurrent mark-sweep generation total 173440K, used 0K [0x00000006f7200000, 0x0000000701b60000, 0x00000007c0000000)
//        Metaspace       used 3429K, capacity 4496K, committed 4864K, reserved 1056768K
//        class space    used 373K, capacity 388K, committed 512K, reserved 1048576K
    }

    /**
     *
     */
    private static void test2(){
        System.out.println("新生代[],老年代[]组合....");
    }



}
