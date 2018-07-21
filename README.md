# rabbit-friend
privide some comon patterns to use RabbitMq, rpc,p2p,delay queues,retry queues etc
 
##### Rabbitmq-friend 基于Rabbitmq client封装了可回复，可重试，可延迟等多种类型消息，客户端可以使用三种类型消息，可轻松实现rpc，重试队列，延迟任务等业务场景
 
## 1.Hello world实现

### 1.1 几个基础概念
   ##### 1.交换器，队列，路由键，生产者，消费者
   
    生产者发送消息到指定交换器，根据Rabbitmq根据消息路由键路由到指定队列，消费者绑定到指定的队列，消费该队列消息
    
    
    BaseExchange ：交换器，在系统初始化时需要声明交换器。
    BaseQueue:  队列，在系统初始化时，声明队列，并且按照指定路由键将队列绑定到交换器
    RoutingKey: 发送消息时需要指定路由键，同时绑定队列到交换器也需要路由键
    ProducerCompositor:生产者
    

  ##### 2.消息类型
    
    Message:普通消息类型，所有的消息必须继承自Message
    NeedReplyMessage 发送该消息类型可通过注册回调接口收到该消息的回复
    RetriableMessage 该类型消息提供重试操作，可通过重试完成某些容错逻辑
    DefferedMessage 该类型消息可用于延迟队列实现，延迟某一消息处理
    
    可重试，可延迟消息类型，只需要实现以上两个接口即可，可回复消息需要继承NeedReplyMessage


   ##### 3.Example
###### 3.1 定义TestCase基类
     
  
```
public class BaseTest {
    protected RabbitContext context;
    
    protected RabbitConfiguration configuration;

    protected Logger logger = LogManager.getLogger(this.getClass());

    protected BaseExchange exchange;

    protected BaseQueue queue;

    protected RoutingKey routingKey;

    protected Gson gson = GsonUtil.getGson();

    protected CountDownLatch latch = new CountDownLatch(1);

    // 停止线程
    protected void stop() {
        latch.countDown();
    }

    //等待
    protected void waitStop() {
        try {
            latch.await();
        } catch (Exception e) {

        }
    }

    /***
    * 测试Case通用代码在所有的Case之前执行
    *
    **/
    @Before
    public void setup() {
        /****
        * 配置Rabbitmq 基本信息，用户名密码，server ip,channel pool size
        *  默认回复队列名称
        ***/
        configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        configuration.setUsername("muppet");
        configuration.setPassword("muppet");
        configuration.setIps(new String[]{"127.0.0.1"});
        configuration.setChannelPoolSize(20);
        configuration.setDefaultReplyToQueue("DefaultReplyQueue");

        /***
         * 通过RabbitContext 注册Producer,Consumer
         */
        context = configuration.getRabbitContext();
        context.start();

        /***
        * 定义交换器
        */
        exchange = new BaseExchange("BaseExchange", ExchangeType.topic);
        context.declareExchange(exchange);

        /****
        * 定义队列
        **/
        queue = context.declareQueueIfAbsent("TestQueue");
        
        /***
        * 定义路由键，并且使用队列名作为路由键绑定到交换器
        **/
        routingKey = new RoutingKey("TestQueue");
        context.bind(exchange, queue, routingKey);
    }


}

```
 ###### 3.2 测试NeedReplyMessage消息
 
```
public class TestNeedReplyMessage extends BaseTest {

    @Test
    public void testProducer() {
        /**
         * 定义生产者
         */
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        /**
         * 可回复消息
         */
        BaseNeedReplyMessage base = new BaseNeedReplyMessage();
        base.setRoutingkey("TestQueue");
        base.a = new A();
        //发送消息，并声明回调
        producerCompositor.send(base, new AsyncMessageReplyCallback(null) {
            @Override
            public void run(MessageReply r) {
                logger.debug(gson.toJson(r));
                stop();
            }
        });
        waitStop();
    }

    @Test
    public void testConsumer() {
        /**
         * 注册消费者，需要指定消费的队列
         */
        context.registerConsumer(new ConsumerCompositor(context) {
            @Override
            public String getQueueName() {
                return queue.getName();
            }

            @Override
            public void handle(Message message) {
                BaseNeedReplyMessage baseNeedReplyMessage = message.cast();
                logger.debug(gson.toJson(baseNeedReplyMessage));
                //回复该消息，回复消息类型为MessageReply类型
                baseNeedReplyMessage.reply(new BaseMessageReply());
                stop();
            }
        });
        waitStop();

    }

    class A {
        public String res = "RESULT";
    }

    /**
     * 可回复消息
     */
    class BaseNeedReplyMessage extends NeedReplyMessage {
        public A a;
    }

    /**
     * 消息回复
     */
    class BaseMessageReply extends MessageReply {
        public String reply = "reply";
    }

}

```
###### 3.3 测试RetriableMessage

```
public class TestRetryMessage extends BaseTest {


    @Test
    public void testProducer() {
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        BaseRetryMessage message = new BaseRetryMessage();
        message.setRoutingkey(routingKey.getRoutingKey());
        producerCompositor.send(message);
    }


    @Test
    public void testConsumer() {
        context.registerConsumer(new ConsumerCompositor(context) {
            @Override
            public String getQueueName() {
                return queue.getName();
            }

            @Override
            public void handle(Message message) {
                BaseRetryMessage baseRetryMessage = message.cast();
                //获取当前的重试次数
                logger.debug("current retry times[{}]", baseRetryMessage.getCurrentRetryTimes());

                //如果当前重试次数小于最大重试次数则重试，也可以无限重试
                if (baseRetryMessage.getCurrentRetryTimes() < baseRetryMessage.getMaxRetryTimes()) {
                    logger.debug(" retry message[{}]", gson.toJson(baseRetryMessage));
                    baseRetryMessage.retry();
                } else {
                    logger.debug("succeed to handle this message");
                    stop();
                }
            }
        });
        waitStop();
    }

}

//重试消息类型
class BaseRetryMessage extends Message implements RetriableMessage {

    private String name = "Base retry message";

    @Override
    public Integer getMaxRetryTimes() {
        return 4;
    }

    @Override
    public Integer getRetryInterval() {
        return 5000;
    }
}

```

###### 3.4 可延迟类型消息

```
public class TestDefferedMessage extends BaseTest {

    @Test
    public void testProducer() {
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        for (int i = 0; i < 100; i++) {
            BaseDefferedMessage message = new BaseDefferedMessage();
            message.setRoutingkey(routingKey.getRoutingKey());
            producerCompositor.send(message);
        }
    }

    @Test
    public void testConsumer() {
        context.registerConsuimerCompositor(new ConsumerCompositor(context) {
            @Override
            public String getQueueName() {
                return queue.getName();
            }

            @Override
            public void handle(Message message) {
                logger.debug("time interval:{}", (System.currentTimeMillis() - message.getBasicProperties().getTimestamp().getTime()) / 1000);
                //stop();
            }
        }, 20);
        waitStop();
    }

}

class BaseDefferedMessage extends Message implements DefferedMessage {

    private String name = "延迟消息";

    @Override
    public Integer getDefferedTime() {
        return 10000;
    }
}

```
###### 3.5 三种类型消息可随意组合

```
class ARetrableMessage extends NeedReplyMessage implements RetriableMessage, DefferedMessage {


    public ARetrableMessage(String name) {
        this.name = name;
    }

    //@Override
    public TimeoutMessage setTimeout(Long timeout) {
        return this;
    }

    private String name = "组合消息类型";

    @Override
    public Long getTimeout() {
        return 100000L;
    }

    @Override
    public Integer getMaxRetryTimes() {
        return 3;
    }

    @Override
    public Integer getRetryInterval() {
        return 5000;
    }

    @Override
    public Integer getDefferedTime() {
        return 10000;
    }
}

```
#### Note
1. 消息的属性赋值时不能被赋值为匿名内部类型，目前还不能对这种类型反序列化
