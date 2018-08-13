package designPatterns.singleCase;

/**
 * 双重校验锁模式(懒汉式)
 */
public class DoubleCheckLock {

    private DoubleCheckLock(){}

    private static DoubleCheckLock single = null;

    /**
     * 获取返回的单例对象
     *      -双重校验锁模式
     */
    public static DoubleCheckLock getInstance(){
        /**
         * 以后创建对象,不需要再进行锁的判断,提升效率
         */
        if (single == null){//解决效率问题
            synchronized (DoubleCheckLock.class){
                //防止多个线程同时第一判空通过
                if (single == null){//解决安全问题
                    single = new DoubleCheckLock();
                }
            }
        }
        return single;
    }

}
