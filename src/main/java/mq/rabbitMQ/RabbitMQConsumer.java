package mq.rabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * 消费者 处理RabbitMQ中的消息
 */
public class RabbitMQConsumer {
    private static final String QUEUE_NAME = "demo.queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory(); //连接工厂
        // 配置连接参数信息
        factory.setUsername("rabbitstudy");
        factory.setPassword("123456");
        factory.setHost("192.168.110.130");
        factory.setPort(5672);
        Connection connection = factory.newConnection(); //创建连接
        final Channel channel = connection.createChannel(); //创建信道在信道上传递消息
        //告诉RabbitMQ我可以接收消息了
//        @Override
        channel.basicConsume(QUEUE_NAME, new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                    throws IOException {
                System.out.println("消费者接收到: " + new String(body));
                //告诉服务器,我收到消息了
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
        TimeUnit.SECONDS.sleep(1);//关闭
        channel.close();
        connection.close();
    }

}
