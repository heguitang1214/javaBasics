package multiThreading.singleThread;

public class WaitAndNotifyDemo {
    public static void main(String[] args) throws InterruptedException {
        wait1();
//        wait2();
    }

    private static void wait2() throws InterruptedException {
        MyThread myThread = new MyThread();
        myThread.start();
        // 主线程睡眠3s
        Thread.sleep(3000);
        synchronized (myThread) {
            try {
                System.out.println("before wait");
                // 阻塞主线程，因为主线程使用的锁是myThread，
                // 所以同步代码块中，可以使用myThread来阻塞主线程
                myThread.wait();
                System.out.println("after wait");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 线程
     */
    private static void wait1(){
        MyThread myThread = new MyThread();
        synchronized (myThread) {
            try {
                myThread.start();
                // 主线程睡眠3s
                Thread.sleep(2000);
                System.out.println("before wait");
                // 阻塞主线程，因为是同一把锁，才能阻塞主线程，
                //如果不是同一把锁，主线程就直接走完了，myThread再调用wait()
                //就是失败，wait()会释放锁
                myThread.wait();
                System.out.println("after wait");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class MyThread extends Thread {
        public void run() {
            synchronized (this) {
                System.out.println("before notify");
                notify();
                System.out.println("after notify");
            }
        }
    }
}