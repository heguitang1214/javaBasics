package jvm.jdkTools;

/**
 * Created by 11256 on 2018/9/11.
 * JDK工具:jps + jstat
 */
public class JpsAndJstatTest {
    private static final int _MB = 1024 * 1024;

    public static void main(String[] args) throws InterruptedException {
        jps_jstat();
    }

    /**
     * 分析GC回收情况
     * jps
     * jstat -<option> [-t] [-h<lines>] <vmid> [<interval> [<count>]]
     */
    private static void jps_jstat() throws InterruptedException {
        Thread.sleep(1000 * 20);
        byte[] allocation1, allocation2, allocation3;
        allocation1 = new byte[_MB / 4];
        allocation2 = new byte[4 * _MB];
        allocation3 = new byte[4 * _MB];
        allocation3 = null;
        allocation3 = new byte[4 * _MB];
        Thread.sleep(1000 * 30);
    }

}
