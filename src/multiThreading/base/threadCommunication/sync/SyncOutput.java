package multiThreading.base.threadCommunication.sync;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 * 线程输出
 */
public class SyncOutput implements Runnable{

    private SyncResource resource;

    SyncOutput(SyncResource resource){
        this.resource = resource;
    }

    @Override
    public void run() {
        while (true){
            resource.out();
        }
    }
}
