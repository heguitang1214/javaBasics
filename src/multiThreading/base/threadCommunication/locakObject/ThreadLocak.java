package multiThreading.base.threadCommunication.locakObject;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 *  线程任务锁
 */
public class ThreadLocak {

    public static void main(String[] args) {
        new ThreadLocak().init();
    }

    private void init(){
        final Outputer outputer = new Outputer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("1234567890");
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("qwertyuiop");
                }
            }
        }).start();


    }
}

class Outputer{

    private Lock lock = new ReentrantLock();

    //线程安全方法
    void output(String name){
        lock.lock();//当前线程上锁
        try {
            for (int i = 0; i < name.length(); i++) {
                System.out.print(name.charAt(i));
            }
            System.out.println();
        }finally {
            lock.unlock();//当前线程释放锁
        }
    }

}

