package multiThreading.base.threadCommunication.sync;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 */
public class SyncResource {

    private String name;
    private String sex;
    private boolean flag = false;

    //资源写入
    public synchronized void set(String name, String sex){
        //flag为true,表示有资源,冻结输入线程
        if (flag){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.name = name;
            this.sex = sex;
            //资源写入完毕,改变状态flag为true
            flag = true;
            //唤醒所终的输出线程
            this.notify();
        }
    }

    //资源写出
    public synchronized void out(){
        //有资源标记为true,读取资源,没有资源为false,输出线程沉睡
        if (!flag){//没有资源
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() +
                    "获取数据:name=" + name + ",sex=" + sex);
            flag = false;
            this.notify();
        }
    }

}
