package multiThreading.base.threadCommunication.sync;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 * 线程输入
 */
public class SyncInput implements Runnable{

    private SyncResource resource;

    SyncInput(SyncResource resource){
        this.resource = resource;
    }

    @Override
    public void run() {
        int x = 0;
        while(true){
            if (x == 0){
                resource.set("张三", "男");
            }else {
                resource.set("李四", "女");
            }
            x = (x + 1) % 2;
        }
    }
}
