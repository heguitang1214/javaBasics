package multiThreading.base.threadCommunication.sync;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/13]
 */
public class SyncTest {

    public static void main(String[] args) {

        SyncResource resource = new SyncResource();

        new Thread(new SyncInput(resource)).start();
        new Thread(new SyncOutput(resource)).start();

    }

}
