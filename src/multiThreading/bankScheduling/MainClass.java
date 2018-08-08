package multiThreading.bankScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 启动类
 */
public class MainClass {

//	private static Logger logger = Logger.getLogger("cn.itcast.bankqueue");

    public static void main(String[] args) {

        //产生4个普通窗口
        for (int i = 1; i < 5; i++) {
            ServiceWindow window = new ServiceWindow();
            window.setNumber(i);
            window.start();//启动服务窗口
        }

        //产生1个快速窗口
        ServiceWindow expressWindow = new ServiceWindow();
        expressWindow.setType(CustomerType.EXPRESS);
        expressWindow.start();

        //产生1个VIP窗口
        ServiceWindow vipWindow = new ServiceWindow();
        vipWindow.setType(CustomerType.VIP);
        vipWindow.start();

        /**
         * newScheduledThreadPool:参数为核心线程数
         * scheduleAtFixedRate参数说明:
         *      Runnable command:
         *      long initialDelay:
         *      long period:
         *      TimeUnit unit:
         */

        //普通客户拿号
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
                new Runnable() {
                    public void run() {
                        Integer serviceNumber = NumberMachine.getInstance().getCommonManager().generateNewNumber();
                        /**
                         * 采用logger方式，无法看到直观的运行效果，因为logger.log方法内部并不是直接把内容打印出出来，
                         * 而是交给内部的一个线程去处理，所以，打印出来的结果在时间顺序上看起来很混乱。
                         */
                        //logger.info("第" + serviceNumber + "号普通客户正在等待服务！");
                        System.out.println("第" + serviceNumber + "号普通客户正在等待服务！");
                    }
                },
                0,
                Constants.COMMON_CUSTOMER_INTERVAL_TIME,
                TimeUnit.SECONDS);


//        //快速客户拿号
//        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
//                new Runnable() {
//                    public void run() {
//                        Integer serviceNumber = NumberMachine.getInstance().getExpressManager().generateNewNumber();
//                        System.out.println("第" + serviceNumber + "号快速客户正在等待服务！");
//                    }
//                },
//                0,
//                Constants.COMMON_CUSTOMER_INTERVAL_TIME * 2,
//                TimeUnit.SECONDS);


//        //VIP客户拿号
//        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
//                new Runnable() {
//                    public void run() {
//                        Integer serviceNumber = NumberMachine.getInstance().getVipManager().generateNewNumber();
//                        System.out.println("第" + serviceNumber + "号VIP客户正在等待服务！");
//                    }
//                },
//                0,
//                Constants.COMMON_CUSTOMER_INTERVAL_TIME * 6,
//                TimeUnit.SECONDS);
    }

}
