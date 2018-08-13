package multiThreading.base.threadCommunication.locakObject;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 *
 */
public class Resource {

    private String name;
    private int count = 1;
    private boolean flag = false;

    //创建锁对象
    Lock lock = new ReentrantLock();
    //通过已有的锁获取两组监视器,一组件事生产者,一组监视消费者
    Condition producer_con = lock.newCondition();
    Condition consumer_con = lock.newCondition();


    public void set(String name){
        lock.lock();
        try{
            while (flag){
                try {
                    //冻结生产线程(监视生产者线程)
                    producer_con.await();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            this.name = name + count;
            count ++ ;
            System.out.println(Thread.currentThread().getName() + ">>生产数据:" + this.name);
            flag = true;
            //唤醒消费线程中的任意一个
            consumer_con.signal();
        }finally {
            lock.unlock();
        }

    }


}
