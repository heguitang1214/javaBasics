package utils.redis;

import redis.clients.jedis.Jedis;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/16]
 */
public class Test {

    public static void main(String[] args) {

        test1();

    }


    private static void test1() {
        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("192.168.105.135");
        System.out.println("Connection to server sucessfully");
        //查看服务是否运行
        System.out.println("Server is running: " + jedis.ping());
    }

    private static void test2(){
        // 连接 Redis 服务
        Jedis jedis = new Jedis("10.80.248.22"); // 默认端口
        //Jedis jedis = new Jedis("10.80.248.22",6379); // 指定端口
        // jedis.auth("pass") // 指定密码
        System.out.println("Connection to server sucessfully");
        // 设置 redis 字符串数据
//        jedis.set("redis", "Redis 1");
        // 获取存储的数据并输出
        System.out.println("Stored string in redis:: " + jedis.get("redis"));
        System.out.println("redis : " + jedis.get("redis"));
    }


}
