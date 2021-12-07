# Hetan-starter-rocket

阿里云RocketMQ SDK Starter，使用方法与类的方式收发消息。[示例代码仓库](https://gitee.com/zztech2020/Hetan-rocket-example)

# 一、快速启动

### 添加依赖

```xml
<!--在pom.xml中添加依赖-->
<dependency>
    <groupId>com.hetan</groupId>
    <artifactId>hetan-starter-rocket</artifactId>
</dependency>
```

### 配置文件

 ```yaml
rocket:
    access-key: xxx #您在阿里云账号管理控制台中创建的 AccessKey，用于身份认证，使用个人服务器时，可以指定随意字符串，但不得为空
    secret-key: xxx #您在阿里云账号管理控制台中创建的 SecretKey，用于身份认证，使用个人服务器时，可以指定随意字符串，但不得为空
    name-srv-addr: 172.25.226.112:9876 #设置 TCP 协议接入点，从控制台获取
 ```

### 启动RocketMQ

在启动器上添加指定注解`@EnableRocketMQ`

```java

@EnableRocketMQ
@SpringBootApplication
public class HetanRocketExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(HetanRocketExampleApplication.class, args);
    }

}
```

# 二、发送消息

## 1、发送方式

发送方式支持：

- 普通消息
- 顺序消息
- 事务消息

### （1）发送普通消息

```java
/**
 * 发送普通消息示例
 * <p>
 * 发送普通消息有三种发送模型，分别为同步发送，异步发送，直接发送
 * <p>
 * 1.同步发送：发送消息采用同步模式，这种方式只有在消息完全发送完成之后才返回结果，此方式存在需要同步等待发送结果的时间代价。
 * 这种方式具有内部重试机制，即在主动声明本次消息发送失败之前，
 * 内部实现将重试一定次数，默认为2次（DefaultMQProducer＃getRetryTimesWhenSendFailed）。
 * 发送的结果存在同一个消息可能被多次发送给给broker，这里需要应用的开发者自己在消费端处理幂等性问题。
 * <p>
 * 2.异步发送：发送消息采用异步发送模式，消息发送后立刻返回，当消息完全完成发送后，会调用回调函数sendCallback来告知发送者本次发送是成功或者失败。
 * 异步模式通常用于响应时间敏感业务场景，即承受不了同步发送消息时等待返回的耗时代价。
 * 同同步发送一样，异步模式也在内部实现了重试机制，默认次数为2次（DefaultMQProducer#getRetryTimesWhenSendAsyncFailed）。
 * 发送的结果同样存在同一个消息可能被多次发送给给broker，需要应用的开发者自己在消费端处理幂等性问题。
 * <p>
 * 3.直接发送：采用one-way发送模式发送消息的时候，发送端发送完消息后会立即返回，
 * 不会等待来自broker的ack来告知本次消息发送是否完全完成发送。
 * 这种方式吞吐量很大，但是存在消息丢失的风险，所以适用于不重要的消息发送，比如日志收集。
 *
 * @author 赵元昊
 * @date 2021/08/20 13:55
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class SendCommonExampleServer {

    /**
     * 发送同步消息，需要标注{@link CommonMessage#messageSendType()}字段，
     * 该字段默认使用异步方法，需要手动指定为同步发送，同步发送的类型为{@link MessageSendType#SEND}
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND)
    public String sendSyncMessage() {
        return "发送同步消息";
    }

    /**
     * 发送异步消息，需要标注{@link CommonMessage#messageSendType()}字段，
     * 该字段默认使用异步方法，异步发送的类型为{@link MessageSendType#SEND_ASYNC},
     * 可使用{@link CommonMessage#callback()}字段指定消息回调类，消息回调类需要先实例化成bean才能被使用，
     * 默认使用{@link DefaultSendCallback}消息回调
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND_ASYNC, callback = DefaultSendCallback.class)
    public String sendAsyncMessage() {
        return "发送异步消息";
    }

    /**
     * 发送单向消息，需要标注{@link CommonMessage#messageSendType()}字段，
     * 将该字段指定成{@link MessageSendType#SEND_ONE_WAY}
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND_ONE_WAY)
    public String sendOneWayMessage() {
        return "发送单向消息";
    }

}
```

### （2）发送顺序消息

```java
/**
 * 发送顺序消息示例
 * <p>
 * 顺序消息类型分为两种：全局顺序和分区顺序。
 * <p>
 * 全局顺序：对于指定的一个Topic，所有消息按照严格的先入先出（FIFO）的顺序进行发布和消费。
 * <p>
 * 分区顺序：对于指定的一个Topic，所有消息根据Sharding Key进行区块分区。
 * 同一个分区内的消息按照严格的FIFO顺序进行发布和消费。
 * Sharding Key是顺序消息中用来区分不同分区的关键字段，和普通消息的Key是完全不同的概念。
 *
 * @author 赵元昊
 * @date 2021/08/20 14:49
 * @see <a href="http://help.aliyun.com/apsara/enterprise/v_3_12_0_20200630/ons/rocketmq-apsarastack-user-guide/0b10b7.html">阿里云-顺序消息概念</a>
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class SendOrderExampleServer {

    /**
     * 发送顺序消息，顺序消息需要指定注解{@link OrderMessage}，可自定义sharding key
     *
     * @return {@link String}
     */
    @OrderMessage(topic = "exampleTopic", tag = "exampleTag", shardingKey = "shardingKey")
    public String sendOrderMessage() {
        return "发送有序消息";
    }

}
```

### （3）发送事务消息

![Alt](http://static-aliyun-doc.oss-cn-hangzhou.aliyuncs.com/assets/img/zh-CN/9140528951/p39553.png)

```java
/**
 * 发送事务消息示例
 * <p>
 * 事务消息：RocketMQ提供类似X/Open XA的分布事务功能，通过RocketMQ事务消息能达到分布式事务的最终一致。
 * <p>
 * 半事务消息：暂不能投递的消息，发送方已经将消息成功发送到了RocketMQ服务端，但是服务端未收到生产者对该消息的二次确认，此时该消息被标记成“暂不能投递”状态，处于该种状态下的消息即半事务消息。
 * <p>
 * 消息回查：由于网络闪断、生产者应用重启等原因，导致某条事务消息的二次确认丢失，RocketMQ服务端通过扫描发现某条消息长期处于“半消息”时，需要主动向消息生产者询问该消息的最终状态（Commit或是Rollback），该过程即消息回查。
 *
 * @author 赵元昊
 * @date 2021/08/20 15:11
 * @see <a href="https://help.aliyun.com/apsara/enterprise/v_3_12_0_20200630/ons/rocketmq-apsarastack-user-guide/0bb812.html?spm=a2c4g.14484438.10001.59">阿里云-事务消息介绍</a>
 **/
@RocketMessageSend(groupID = "exampleTransactionGroup")
@Component
public class SendTransactionExampleServer {

    /**
     * 发送事务消息,使用注解{@link TransactionMessage},注意，事务消息的{@link RocketMessageSend#groupID()}应与普通消息不同。
     * 可指定{@link TransactionMessage#checker()}服务器回查本地事务器，与{@link TransactionMessage#executer()}本地事务执行器，
     * 本地事务执行器默认指定为提交操作的执行器，需要真实业务调用时重写，回查本地事务默认使用的事提交本地事务操作，
     * 有其他同名的{@link DefaultLocalRollbackTransactionChecker}与{@link DefaultLocalUnknowTransactionChecker}分别默认执行回滚与未知操作，
     * 需要服务自定义
     *
     * @return {@link String}
     */
    @TransactionMessage(topic = "exampleTopic", tag = "exampleTag", checker = DefaultLocalCommitTransactionChecker.class, executer = DefaultLocalTransactionExecuter.class)
    public String sendTransactionMessage() {
        return "发送事务消息";
    }

}
```

## 2、发送延时消息或定时消息

发送延时消息或定时消息需要在发送实例的方法入参中指定定时时间入参，并在该入参上添加`@DeliverTime`注解，该注解有两种功能，分别如下：

- 延时投递：延时投递传入需要延时的毫秒戳，即可实现在指定毫秒戳后投递消息
- 定时投递：定时投递需要手动指定注解上的`timeStampModel`字段为`True`，传入什么时间戳则会按照该时间戳定时投递消息

```java
/**
 * 发送延时消息或定时消息
 * <p>
 * 定时消息：生产者将消息发送到RocketMQ服务端，但并不期望这条消息立马投递，而是推迟到在当前时间点之后的某一个时间投递到消费者进行消费，该消息即定时消息。
 * <p>
 * 延时消息：生产者将消息发送到RocketMQ服务端，但并不期望这条消息立马投递，而是延迟一定时间后才投递到消费者进行消费，该消息即延时消息。
 * <p>
 * 定时消息与延迟消息在代码配置上存在一些差异，但是最终达到的效果相同：消息在发送到RocketMQ服务端后并不会立马投递，而是根据消息中的属性延迟固定时间后才投递给消费者。
 *
 * @author 赵元昊
 * @date 2021/08/20 15:59
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class SendDeliverMessageExampleServer {

    /**
     * 发送延时指定毫秒数的消息，需要在方法入参中指定{@link DeliverTime}注解，
     * 并在注解中指定{@link DeliverTime#timeStampModel()}为false（已默认指定），
     * 强制使用Long类型，否则会转换失败，指定时间为毫秒。
     * 添加注解的消息，将在指定时间后发送（由RocketMQ实现）
     *
     * @param deliverTime 延时时间
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendDeliverTimeMessage(@DeliverTime Long deliverTime) {
        return "发送延时消息";
    }

    /**
     * 发送指定时间的消息，传入的字段应为时间转换为毫秒戳，此功能需要手动开启，开启的注解字段为
     * {@link DeliverTime#timeStampModel()}为{@link Boolean#TRUE}
     *
     * @param timeStamp 时间戳
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendTimeStampMessage(@DeliverTime(timeStampModel = true) Long timeStamp) {
        return "发送定时消息";
    }

}
```

## 3、数据发送类型

数据发送类型大致支持有两种，分别为对象类型与`Byte[]`字节数组类型，对象类型会使用`FastJson`转换为`Byte[]`后再发送给RocketMQ， 字节数组则会跳过转换这一步骤，直接将自定义返回的`Byte[]`
数组发送给RocketMQ，如果需要发送文件数据时可使用`Byte[]`数组反参。

```java
/**
 * 消息发送示例，消息提供者的实例化需要依赖Spring Beans的自动化机制，
 * 需要将发送的类实例化成Spring Bean，推荐使用{@link Component}注解。
 *
 * @author 赵元昊
 * @date 2021/08/20 11:53
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class SendTypeExampleServer {

    private final AtomicInteger sendStringCount = new AtomicInteger();

    private final AtomicInteger sendByteCount = new AtomicInteger();

    private final AtomicInteger sentObjectCount = new AtomicInteger();

    /**
     * 发送String类型消息，String类型也会视为普通对象类型，会经过FastJson转换
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendStringCommonMessage() {
        return String.format("发送%s消息至RocketMQ,第%s次发送,发送的类型%s", "普通", sendStringCount.incrementAndGet(), String.class.getSimpleName());
    }

    /**
     * 发送字节类型消息
     *
     * @return {@link byte[]}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public byte[] sendByteCommonMessage() {
        return String.format("发送%s消息至RocketMQ,第%s次发送,发送的类型%s", "普通字节", sendByteCount.incrementAndGet(), byte[].class.getSimpleName()).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 发送对象类型消息
     *
     * @return {@link ExampleObject}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public ExampleObject sendObjectCommonMessage() {
        return ExampleObject.builder().name("小明").age(18).saying(String.format("Java 是世界上最好的语言，重要的事情要说%s遍", sentObjectCount.incrementAndGet()))
            .linkObject(ExampleObject.builder().name("小红").age(99).saying("汇编才是最好用的语言，虽然掉头发~").build())
            .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleObject {
        private String name;
        private Integer age;
        private String saying;
        private ExampleObject linkObject;
    }

}
```

## 4、发送消息各类回调拓展

发送消息回调拓展大致分为以下几种：

- 发送普通消息时拓展异步回调：拓展异步回调功能需要实现`SendCallback`接口，并需要将实现的类指定成`Spring Bean`，再在`CommonMessage`注解中指定`callback`为自己的拓展类
- 发送事务消息时本地事务回查器：由`Broker`未收到二次确认请求时回调检查本地事务，需要实现`LocalTransactionExecuter`接口，并将实现的类指定成`Spring Bean`
  ，再将类文件指定到事务消息接口中`@TransactionMessage(checker = xxx.class)`
- 发送事务消息时本地事务执行器：当发送事务消息时，半消息发送成功后，`Producer`回调执行器执行本地事务

### （1）普通消息异步回调拓展

#### 1）自定义拓展回调接口

```java
/**
 * 拓展异步回调接口,需要将本类实例化成Bean，
 * 并在{@link CommonMessage#callback()}字段指定本类文件
 *
 * @author 赵元昊
 * @date 2021/08/20 18:59
 **/
@Component
public class ExpandSendCallback implements SendCallback {
    @Override
    public void onSuccess(SendResult sendResult) {
        System.out.println("异步消息发送成功，发送结果为：" + sendResult);
    }

    @Override
    public void onException(OnExceptionContext context) {
        System.out.println("异步消息发送失败，失败信息为：" + context);
    }
}
```

#### 2）发送普通消息时指定回调类

```java
/**
 * 拓展异步发送消息时回调功能
 *
 * @author 赵元昊
 * @date 2021/08/20 19:03
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class ExpandAsyncCommonMessageSend {

    /**
     * 自定义回调接口
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND_ASYNC, callback = ExpandSendCallback.class)
    public String sendAsyncMessage() {
        return "发送异步消息";
    }

}
```

### （2）事务消息本地回查器

#### 1）自定义回查器

```java
/**
 * 拓展本地事务回查器
 *
 * @author 赵元昊
 * @date 2021/08/21 13:16
 **/
@Component
public class ExpandLocalTransactionChecker implements LocalTransactionChecker {

    /**
     * 回查本地事务，由Broker调用Producer执行
     *
     * @param msg 味精
     * @return {@link TransactionStatus}
     */
    @Override
    public TransactionStatus check(Message msg) {
        // 自定义操作内容与事务状态
        return TransactionStatus.RollbackTransaction;
    }
}
```

#### 2）发送事务消息时指定回查器

```java
/**
 * 事务消息Checker回查器示例
 *
 * @author 赵元昊
 * @date 2021/08/21 13:55
 **/
@RocketMessageSend(groupID = "exampleTransactionGroup")
@Component
public class ExpandTransactionCheckerMessageSend {

    /**
     * 发送事务消息,指定回查器{@link ExpandLocalTransactionChecker}
     *
     * @return {@link String}
     */
    @TransactionMessage(topic = "exampleTopic", tag = "exampleTag", checker = ExpandLocalTransactionChecker.class)
    public String sendTransactionCheckerMessage() {
        return "发送事务消息";
    }
}
```

### （3）事务消息本地事务执行器

#### 1）自定义执行器

```java
/**
 * 拓展本地事务回查器
 *
 * @author 赵元昊
 * @date 2021/08/21 14:17
 */
@Component
public class ExpandLocalTransactionExecuter implements LocalTransactionExecuter {

    /**
     * 执行事务
     *
     * @param msg 味精
     * @param arg 由发送时传入的自定义参数，此部分可以通过ThreadLocal进行传递{@link RocketThreadLocalUtil#add(Object...)}
     * @return {@link TransactionStatus}
     */
    @Override
    public TransactionStatus execute(Message msg, Object arg) {
        // 执行事务操作 并返回事务结果
        return TransactionStatus.CommitTransaction;
    }
}
```

#### 2）发送事务消息时指定事务执行器

```java
/**
 * 事务消息Executer事务执行器
 *
 * @author 赵元昊
 * @date 2021/08/21 13:55
 **/
@RocketMessageSend(groupID = "exampleTransactionGroup")
@Component
public class ExpandTransactionExecuterMessageSend {

    /**
     * 发送事务消息,指定事务执行器{@link ExpandLocalTransactionExecuter},
     * 指定事务执行器时可以传入自定义参数，事务执行器接收使用，传入的方法为{@link RocketThreadLocalUtil#add(Object...)}
     *
     * @return {@link String}
     */
    @TransactionMessage(topic = "exampleTopic", tag = "exampleTag", executer = ExpandLocalTransactionExecuter.class)
    public String sendTransactionCheckerMessage() {
        // 指定传入的参数
        RocketThreadLocalUtil.add(new Object(), new Object());
        return "发送事务消息";
    }
}
```

# 三、订阅消息

订阅消息支持两种订阅方式，三种消费模式，均基于注解+接口组合实现。

- 消息消费模式：
    - 普通消费方式
    - 消息批量消费
    - 消息顺序消费
- 消息订阅方式：
    - 集群订阅：同一个Group ID所标识的所有Consumer平均分摊消费消息。 例如某个Topic有9条消息，一个Group ID有3个Consumer实例，那么在集群消费模式下每个实例平均分摊，只消费其中的3条消息。
    - 广播订阅：同一个Group ID所标识的所有Consumer都会各自消费某条消息一次。 例如某个Topic有9条消息，一个Group ID有3个Consumer实例，那么在广播消费模式下每个实例都会各自消费9条消息。

## （1）消费模式：

### 1）普通消费方式

```java
/**
 * 普通消息监听器，使用时需要实例化为Spring Bean，并添加注解{@link RocketMessageListener}与实现消息监听接口。
 * <p>
 * 普通消息需要实现的接口为{@link AliyunCommonConsumerListener},该接口为泛型接口，接口泛型为发送时传入的参数类型，
 * 并且需要在{@link RocketMessageListener#messageType()} 中指定该泛型参数，监听器支持集群消费模式与广播消费模式，
 * 消费模式默认为集群消费模式，如果需要改为广播消费模式需要指定注解中的字段{@link RocketMessageListener#messageModel()},
 * 广播消费模式的字符为BROADCASTING,已经在类中{@link MessageModelState#BROADCASTING}字段中封装完成，
 * 需要将此模式改为集群模式即可。
 *
 * @author 赵元昊
 * @date 2021/08/21 15:50
 **/
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag",
    messageType = String.class, messageModel = MessageModelState.BROADCASTING)
@Component
public class CommonMessageListener implements AliyunCommonConsumerListener<String> {

    /**
     * 消费消息并返回结果
     *
     * @param message 消息
     * @param args    该参数为阿里云SDK保留参数，默认第一位为{@link ConsumeContext}类型参数，如不使用，忽略即可
     * @return {@link Action}
     */
    @Override
    public Action onMessage(String message, Object... args) {
        // 消费消息
        return Action.CommitMessage;
    }
}
```

### 2）消息批量消费

```java
/**
 * 消息批量消费监听器，使用时需要实例化为Spring Bean，并添加注解{@link RocketMessageListener}与实现消息监听接口。
 * <p>
 * 消息批量消费需要实现的接口为{@link AliyunBatchConsumerListener},该接口为泛型接口，接口泛型为发送时传入的参数类型，
 * 并且需要在{@link RocketMessageListener#messageType()} 中指定该泛型参数,
 * {@link RocketMessageListener#consumeMessageBatchMaxSize()}字段为
 * 设置批量消费最大消息数量,当指定Topic的消息数量已经攒够128条,SDK立即执行回调进行消费.默认值：32,取值范围：1~1024.
 * {@link RocketMessageListener#batchConsumeMaxAwaitDurationInSeconds()}字段为
 * 设置批量消费最大等待时长,当等待时间达到10秒,SDK立即执行回调进行消费.默认值：0,取值范围：0~450,单位：秒.
 *
 * @author 赵元昊
 * @date 2021/08/21 17:13
 **/
@RocketMessageListener(groupID = "exampleTransactionGroup", topic = "exampleTopic", tag = "exampleTag",
    consumeMessageBatchMaxSize = 32, batchConsumeMaxAwaitDurationInSeconds = 1,
    messageType = String.class, messageModel = MessageModelState.BROADCASTING)
@Component
public class BatchMessageListener implements AliyunBatchConsumerListener<String> {

    /**
     * 消息批量消费
     *
     * @param message 消息
     * @param args    arg游戏
     * @return {@link Action}
     */
    @Override
    public Action onBatchMessage(List<String> message, Object... args) {
        // 消费消息并回传消费结果
        return Action.CommitMessage;
    }
}
```

### 3）消息顺序消费

```java
/**
 * 有序消息消费监听器，使用时需要实例化为Spring Bean，并添加注解{@link RocketMessageListener}与实现消息监听接口。
 * <p>
 * 有序消息需要实现的接口为{@link AliyunOrderConsumerListener},该接口为泛型接口，接口泛型为发送时传入的参数类型，
 * 并且需要在{@link RocketMessageListener#messageType()} 中指定该泛型参数
 *
 * @author 赵元昊
 * @date 2021/08/21 17:06
 * @see CommonMessageListener
 **/
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag",
    messageType = String.class, messageModel = MessageModelState.BROADCASTING)
@Component
public class OrderMessageListener implements AliyunOrderConsumerListener<String> {

    /**
     * 消费消息
     *
     * @param message 消息
     * @param args    arg游戏
     * @return {@link OrderAction}
     */
    @Override
    public OrderAction onMessage(String message, Object... args) {
        // 消费消息 并返回当前队列的消费结果
        return OrderAction.Success;
    }
}
```

## （2）订阅方式：

订阅方式修改需要修改`RocketMessageListener`注解上的`messageModel`字段 指定为`CLUSTERING`时未集群消费，指定为`BROADCASTING`时为广播消费

### 1)集群消费

```java
@RocketMessageListener(messageModel = MessageModelState.CLUSTERING)
```

### 2)广播消费

```java
@RocketMessageListener(messageModel = MessageModelState.BROADCASTING)
```

## （3）消息内容类型

接收消息内容时会将接收到的原生Message对象中的body(类型为Byte[])根据注解中的messageType字段进行反序列化后传入到监听器中，免去手动获取消息并转换的麻烦，但是对于Byte[]和Message这种原生对象，
做了一定的放行操作。

消息内容类型支持大致三种：

- 普通对象类型：会自动拆箱并进行转换,除去Byte[]、Message类型之外的类型都为普通对象类型，会经过序列化器进行转换
- Byte[]数组类型：会自动拆箱，但是并不会被转换，字节数组类型会调用默认的Json序列化器，但是在默认的序列化器中做了放行操作，保障使用Byte[]数组接收到的消息内容为RocketMQ直接发送过来的数组，不会做任何转换操作
- Message类型：不会被拆箱，也不会被转换，当将接收的泛型参数与注解的message字段都指定为Message.class时，会将接收到的整个Message直接传入监听器中，
  不会经过默认的拆箱操作（即：无论发送的是什么类型的数据，都可以使用Message类型接收）。

### 1）对象类型：

```java
/**
 * 接收普通对象类型，使用{@link String}类型举例，注解与泛型都声明为{@link String}类型
 *
 * @author 赵元昊
 * @date 2021/08/21 18:45
 **/
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag", messageType = String.class)
@Component
public class ObjectTypeMessageListener implements AliyunCommonConsumerListener<String> {
    @Override
    public Action onMessage(String message, Object... args) {
        // 接收到的将会是反序列化回来的string类型的message消息主体
        return Action.CommitMessage;
    }
}
```

### 2）字节数组类型

```java
/**
 * 接收{@link Byte[]}对象类型，注解与泛型都声明为{@link Byte[]}类型
 *
 * @author 赵元昊
 * @date 2021/08/21 18:45
 **/
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag", messageType = byte[].class)
@Component
public class ByteArrayTypeMessageListener implements AliyunCommonConsumerListener<byte[]> {
    @Override
    public Action onMessage(byte[] message, Object... args) {
        // 接收到的将会是RocketMQ原生的字节数组，会被拆箱，但不会被转换
        return Action.CommitMessage;
    }
}
```

### 3）原生Message类型

```java
/**
 * 接收{@link Message}对象类型，注解与泛型都声明为{@link Message}类型
 *
 * @author 赵元昊
 * @date 2021/08/21 18:45
 **/
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag", messageType = Message.class)
@Component
public class MessageTypeMessageListener implements AliyunCommonConsumerListener<Message> {
    @Override
    public Action onMessage(Message message, Object... args) {
        // 接收到的将会是RocketMQ原生的message类型数据，不会拆箱
        return Action.CommitMessage;
    }
}
```

# 四、自定义拓展

自定义拓展主要分为三部分，分别如下：

- 注解中字段值使用环境变量拓展：可以使用环境变量的方式指定注解中以`ex`为开头的字段，可以从外部配置中取值，增加线上配置的功能，减少修改代码的麻烦。
- 自定义序列化器修改默认的序列化方式：可以使用注解`@MessageSerializer`指定使用的序列化器与反序列化器，方便自定义序列化操作。
- 架构组件拓展：整体架构中是针对于支持`AliyunSDK`和官方的`RocketMQ Client`两种模式设计的，故，整体架构中设计有核心模块与具体实现模块，可拓展性较强，
  如果有需求可以很方便增加对注解、接口等的支持操作，甚至可以便捷的重新使用`RocketMQ Client`进行实现，但是受限于篇幅，本次就不在此展示了，仅展示前两种主要使用到的拓展。

## （1）自定义各类注解中外部配置变量

外部配置与默认配置遵从以下原则：

- 外部配置有值会覆盖默认配置的值
- 外部配置值为空字符串会使用默认配置
- 默认配置与外部配置都为空，会抛出异常

### 1）配置外部变量

```yaml
# 注解中的外部配置
Hetan:
    rocket:
        example:
            expand-group: expandGroup
            expand-topic: expandTopic
            expand-tag: expandTag
```

### 2）消息提供者注解配置

```java
/**
 * 使用外部配置，配置发送的注解
 * <p>
 * 在{@link  RocketMessageSend}与各类Message标签中，有保留字段exGroupID、exTopic、exTag，
 * 可以使用${环境变量值}的方式配置，在发送消息时会自动根据环境变量值获取相关的GroupID、Topic、Tag数据，
 * 并且可以与默认的非ex字段开头的配置同时使用，遵从以下的原则：
 * <p>
 * 1）外部配置优先于非外部配置数据
 * <p>
 * 2）外部配置数据假如指定了数据但是获取到的数据为“”空字符串，则会使用默认的非ex开头的数据
 * <p>
 * 3）假如两个数据都为“”，则在调用的时候会报出相关异常信息，禁止使用后续功能
 *
 * @author 赵元昊
 * @date 2021/08/21 19:10
 **/
@RocketMessageSend(groupID = "exampleGroup", exGroupID = "${Hetan.rocket.example.expand-group}")
@Component
public class ConfigSendAnnotation {

    @CommonMessage(topic = "exampleTopic", exTopic = "${Hetan.rocket.example.expand-topic}",
        tag = "exampleTag", exTag = "${Hetan.rocket.example.expand-tag}")
    public String sendCommonMessage() {
        return "发送常用消息";
    }

    @TransactionMessage(topic = "exampleTopic", exTopic = "${Hetan.rocket.example.expand-topic}",
        tag = "exampleTag", exTag = "${Hetan.rocket.example.expand-tag}")
    public String sendTransactionMessage() {
        return "事务消息";
    }

    @OrderMessage(topic = "exampleTopic", exTopic = "${Hetan.rocket.example.expand-topic}",
        tag = "exampleTag", exTag = "${Hetan.rocket.example.expand-tag}")
    public String sendOrderMessage() {
        return "发送有序消息";
    }
}
```

### 3）监听器注解配置

```java
/**
 * 使用外部配置，配置监听器中的配置
 * <p>
 * 如果配置的环境变量可获取到，则会覆写对应的默认配置
 *
 * @author 赵元昊
 * @date 2021/08/21 19:24
 */
@RocketMessageListener(
    groupID = "exampleGroup", exGroupID = "${Hetan.rocket.example.expand-group}",
    topic = "exampleTopic", exTopic = "${Hetan.rocket.example.expand-topic}",
    tag = "exampleTag", exTag = "${Hetan.rocket.example.expand-tag}",
    messageType = String.class)
@Component
public class ConfigListenerAnnotation implements AliyunCommonConsumerListener<String> {

    @Override
    public Action onMessage(String message, Object... args) {
        // 消费消息
        return Action.CommitMessage;
    }
}
```

## （2）自定义序列化器

自定义序列化器需要实现序列化器的接口，并将自定义的序列化器实例化为`Spring Bean`，最后再使用注解在类或方法上指定序列化器的序列化或反序列化类

```java
@MessageSerializer(serializer = DefaultJsonSerializer.class, deSerializer = DefaultJsonSerializer.class)
```

### 1）自定义实现序列化器

```java
/**
 * 自定义拓展序列化器
 *
 * @author 赵元昊
 * @date 2021/08/21 19:37
 **/
@Slf4j
@Component
public class ExpandProxyJsonSerializer implements RocketSerializer {

    private final RocketSerializer inner = new DefaultJsonSerializer();

    @Override
    public <T> byte[] serialize(T object) {
        log.info("发生了序列化操作,将转换:{}", object);
        return inner.serialize(object);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        log.info("发生了反序列化操作,将转换为:{}类", clazz.getSimpleName());
        return inner.deSerialize(bytes, clazz);
    }
}
```

### 2）使用注解指定使用序列化器

#### 在发送消息时可标记在类或方法上

```java
/**
 * 使用指定序列化器发送数据,发送数据是仅会使用注解中的{@link MessageSerializer#serializer()}数据，
 * 并且在发送时，支持在类上或方法上添加序列化注解，类上为全局注解，整个类都生效此序列化器，方法上为该方法会生效此注解，
 * 并且配置的权重要高于类上的配置。
 *
 * @author 赵元昊
 * @date 2021/08/21 19:46
 **/
@MessageSerializer(serializer = ExpandProxyJsonSerializer.class)
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class ExpandSerializerSend {

    /**
     * 会使用此注解中指定的序列化器序列化消息
     *
     * @return {@link String}
     */
    @MessageSerializer(serializer = DefaultJsonSerializer.class)
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendCommonMessage() {
        return "发送常用消息";
    }

    /**
     * 如果仅使用{@link MessageSerializer}注解，但不手动覆写注解中的序列化器，
     * 将会使用注解中已经生明过的{@link DefaultJsonSerializer}序列化器
     *
     * @return {@link String}
     */
    @MessageSerializer
    @TransactionMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendTransactionMessage() {
        return "事务消息";
    }

    /**
     * 将会使用类上注解指定的{@link ExpandProxyJsonSerializer}序列化消息
     *
     * @return {@link String}
     */
    @OrderMessage(topic = "exampleTopic", tag = "exampleTag")
    public String sendOrderMessage() {
        return "发送有序消息";
    }
}
```

#### 在接收消息时仅支持标记在类上，方法上将不会生效

```java
/**
 * 使用注解指定序列化器，在发送消息时仅使用类上标注的注解，
 * 并且仅使用其中的{@link MessageSerializer#deSerializer()}字段，
 * 在方法上标注的注解将不会生效
 *
 * @author 赵元昊
 * @date 2021/08/21 19:46
 **/
@MessageSerializer(deSerializer = ExpandProxyJsonSerializer.class)
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag", messageType = String.class)
@Component
public class ExpandSerializerListener implements AliyunCommonConsumerListener<String> {

    /**
     * 该注解将不会生效
     *
     * @param message 消息
     * @param args    arg游戏
     * @return {@link Action}
     */
    @MessageSerializer(deSerializer = DefaultJsonSerializer.class)
    @Override
    public Action onMessage(String message, Object... args) {
        return Action.CommitMessage;
    }
}
```

# 五、幂等及消息日志记录

幂等及消息日志记录为后续新增功能，使用时主要需要标注注解使用，在收发消息时，标注`@SendHelper`或`@ListenerHelper`注解即可生效消息日志记录等功能。 使用时除使用本注解之外，还需要引入具体注解工具的实现包。

依赖如下：

```xml
<!--RocketMQ Helper-->
<dependency>
    <groupId>com.Hetan</groupId>
    <artifactId>Hetan-starter-rocket-helper</artifactId>
</dependency>
```

（注：如想独立实现功能则可以不引入相关依赖包，自定义实现接口注解并改写默认配置即可。）

## （1）`@SendHelper`注解

该注解需要标记在发送数据的Bean上，按照功能的使用，可标记在发送类上或发送Bean的具体发送方法上，标记在类中时该Bean全部方法生效， 标记在指定方法中则该指定方法生效，并且遵循`就近原则`
，在方法上标记的注解的优先级要大于类中标记的优先级。

### 1）注解标志位

- `SendHelper.successSave()`标志位为发送成功后保存
- `SendHelper.failSave()`标志位代表为发送失败后保存
- `SendHelper.failNotify()`标志位代表为发送失败后通知企业微信
- `SendHelper.processor()`为可配置的执行器，可多配置，如自定义陪之后会按照配置的顺讯依次执行配置的执行器。 同时，默认配置为空，
  配置为空时会走全局的默认配置，全局默认配置位置为com.Hetan.rocket.base.config.RocketProperties.defaultSendHelper, 该标志位在引用Rocket
  Helper时会自动配置为Rocket Helper的默认实现，如无必要则无需指定。

```java
import com.hetan.rocket.base.config.RocketProperties;

/**
 * 发送助手注解示例
 * <p>
 * {@link SendHelper}注解中的{@link SendHelper#successSave()}标志位为发送成功后保存、{@link SendHelper#failSave()}标志位代表为发送失败后保存、
 * {@link SendHelper#failNotify()}标志位代表为发送失败后通知企业微信，{@link SendHelper#processor()}为可配置的执行器，可多配置，如自定义陪之后会按照配置的顺讯依次执行配置的执行器。
 * 同时，默认配置为空，配置为空时会走全局的默认配置，全局默认配置位置为{@link RocketProperties#defaultSendHelper},
 * 该标志位在引用Rocket Helper时会自动配置为Rocket Helper的默认实现，如无必要则无需指定。
 *
 * @author 赵元昊
 * @date 2021/10/15 15:31
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
@SendHelper(successSave = true, failSave = true, failNotify = true, processor = {DefaultSendHelperProcessor.class})
public class SendHelperExampleServer {

  /**
   * 发送同步消息
   * {@link SendHelper}方法上注解的优先级大于类上的
   * @return {@link String}
   */
  @SendHelper(successSave = false, failSave = true, failNotify = false, processor = {DefaultSendHelperProcessor.class})
  @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND)
  public String sendSyncMessage() {
    return "发送同步消息";
  }

  /**
   * 发送异步消息，会使用发送类上标记的注解配置
   *
   * @return {@link String}
   */
  @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND_ASYNC, callback = DefaultSendCallback.class)
  public String sendAsyncMessage() {
    return "发送异步消息";
  }
}
```

### 2）其他

#### 使用默认的实现包`Hetan-starter-rocket-helper`时

- 使用默认的实现包时需要在数据库新增[`base_rocket_send_log`](../Hetan-starter-rocket-helper/doc/db/base_rocket_send_log.sql)表。
- 使用该方法时，发送时不出异常则视为发送成功，因此，对异步方法并不友好，使用时谨慎使用。

## 2）使用`@ListenerHelper`注解

该注解需要标记位实现XxxxListener接口的监听bean类上，目前适用于顺序消费与普通消息消费的情况，并不适用于批量消息消费的情况，批量消费时该功能请自定义实现。

### 1）注解标志位

- `ListenerHelper.successSave()`代表消费成功后保存
- `ListenerHelper.failSave()`代表消费失败后保存
- `ListenerHelper.failNotify()`代表消费失败后通知
- `ListenerHelper.isIdempotent()`代表是否需要幂等校验
- `ListenerHelper.autoCommit()`代表是否消费成功和消费失败后都提交成功消费信息（适用于消费成功、消费失败都标记位都打开的情况）
- `ListenerHelper.businessType()`代表为商业类型的标记位，默认为“”，在幂等校验时使用该标记位内容，会将该标记位与message_key一同做幂等校验， 在广播消费的情况下方便区分不同的不同业务的消费情况。
- `ListenerHelper.processor()`具体的执行器标志位，可自定义实现并配置，也可忽略并走默认的配置

```java
/**
 * 该注解适用于顺序消费与普通消息消费的情况，并不适用于批量消息消费的情况，批量消费时该功能请自定义实现，
 * {@link ListenerHelper#successSave()}代表消费成功后保存，{@link ListenerHelper#failSave()}代表消费失败后保存，
 * {@link ListenerHelper#failNotify()}代表消费失败后通知，{@link ListenerHelper#isIdempotent()}代表是否需要幂等校验，
 * {@link ListenerHelper#autoCommit()}代表是否消费成功和消费失败后都提交成功消费信息（适用于消费成功、消费失败都标记位都打开的情况）
 * {@link ListenerHelper#businessType()}代表为商业类型的标记位，默认为“”，在幂等校验时使用该标记位内容，会将该标记位与message_key一同做幂等校验，
 * 在广播消费的情况下方便区分不同的不同业务的消费情况。{@link ListenerHelper#processor()}具体的执行器标志位，可自定义实现并配置，也可忽略并走默认的配置
 *
 * @author 赵元昊
 * @date 2021/10/15
 */
@ListenerHelper(successSave = true, failSave = true, failNotify = true, isIdempotent = true, autoCommit = true, businessType = "business_type", processor = {DefaultListenerHelperProcessor.class})
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag",
    messageType = String.class, messageModel = MessageModelState.BROADCASTING)
@Component
public class ListenerHelperMessageListener implements AliyunCommonConsumerListener<String> {

    /**
     * 消费消息并返回结果
     *
     * @param message 消息
     * @param args    该参数为阿里云SDK保留参数，默认第一位为{@link ConsumeContext}类型参数，如不使用，忽略即可
     * @return {@link Action}
     */
    @Override
    public Action onMessage(String message, Object... args) {
        // 消费消息
        return Action.CommitMessage;
    }
}
```

### 2）其他

#### 使用默认的实现包`Hetan-starter-rocket-helper`时

- 在仅使用幂等功能的情况时，
  需要在数据库新增[`base_rocket_idempotent_log`幂等表](../Hetan-starter-rocket-helper/doc/db/base_rocket_idempotent_log.sql)表。
- 在仅使用消费消息记录功能的情况时，
  需要在数据库新增[`base_rocket_listener_log`](../Hetan-starter-rocket-helper/doc/db/base_rocket_listener_log.sql)表。
- 在两种功能都需要使用时，以上两种都需要添加。

## （3）Message_Key

在收发消息时可以接收与发送MessageKey信息，本组件支持发送时自动生成MessageKey与自定义传入MessageKey。使用如下：

### 1）发送Message_Key

发送Message_key有两种方式，分别为手动传入与自动生成。

- 自动生成：`@AutoMessageKey`注解支持标记在类上与方法上，标记在类上全局生效，标记在方法上则为该方法生效，会自动生成UUID数据充当MessageKey。
- 手动传入：使用`RocketThreadLocalUtil.setMessageKey(String)`自定义传入MessageKey，并无需维护RocketThreadLocalUtil.clearMessageKey()
  ，组件会自动维护。
- 混合模式：使用自动生成注解加手动传入混合传入MessageKey，在业务特殊情况时可使用此方式，自定义传入的Key会复写自动生成的Key。

```java
/**
 * {@link AutoMessageKey}注解支持标记在类上与方法上，标记在类上全局生效，标记在方法上则为该方法生效，会自动生成UUID数据充当MessageKey
 *
 * @author 赵元昊
 * @date 2021/10/15 15:31
 **/
@RocketMessageSend(groupID = "exampleGroup")
@Component
public class MessageKeyExampleServer {

    /**
     * 发送同步消息，自动生成MessageKey
     *
     * @return {@link String}
     */
    @AutoMessageKey
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND)
    public String sendSyncMessage() {
        return "发送同步消息";
    }

    /**
     * 发送异步消息,自定义传入MessageKey，无需维护{@link RocketThreadLocalUtil#clearMessageKey()}，组件会自动维护。
     *
     * @return {@link String}
     */
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND_ASYNC, callback = DefaultSendCallback.class)
    public String sendAsyncMessage() {
        RocketThreadLocalUtil.setMessageKey(UUID.randomUUID().toString());
        return "发送异步消息";
    }

    /**
     * 发送同步消息，自动生成MessageKey，在整秒时覆盖自动生成的MessageKey
     *
     * @return {@link String}
     */
    @AutoMessageKey
    @CommonMessage(topic = "exampleTopic", tag = "exampleTag", messageSendType = MessageSendType.SEND)
    public String sendSyncMessageTwo() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis % 1000 == 0) {
            RocketThreadLocalUtil.setMessageKey(currentTimeMillis + "-" + UUID.randomUUID().toString());
        }
        return "发送同步消息";
    }
}
```

### 2）接收Message_Key

接受方式有监听原生Message信息与通过上下文获取Key等两种方式：

- 接收MessageKey可指定监听消息类型为原生的Message类型：当监听的为原生类型时，会监听到Message所有的信息，包含MessageKey。

- 上下文获取Key：可使用手动接受MessageKey的方式，手动接收则使用`RocketThreadLocalUtil.getMessageKey()`方式获取。

```java
/**
 * 消息键消息侦听器
 *
 * @author 赵元昊
 * @date 2021/10/15
 */
@RocketMessageListener(groupID = "exampleGroup", topic = "exampleTopic", tag = "exampleTag",
    messageType = Message.class, messageModel = MessageModelState.BROADCASTING)
@Component
public class MessageKeyMessageListener implements AliyunCommonConsumerListener<Message> {

    /**
     * 消费消息
     *
     * @param message 消息
     * @param args    arg游戏
     * @return {@link Action}
     */
    @Override
    public Action onMessage(Message message, Object... args) {
        // 原生获取MessageKey
        String messageKey = message.getKey();
        // 通过上下文方式获取MessageKey
        String threadLocalMessageKey = RocketThreadLocalUtil.getMessageKey();

        // 消费消息
        return Action.CommitMessage;
    }
}

```

# 六、参考实现

本次封装RocketMQ主要参考了Github的两个开源实现，兼并两个人实现的思想及功能

[原作者Github](https://github.com/ThierrySquirrel/rocketmq-spring-boot-starter) 保留功能的基础上重新搭建整个Starter架构

[RcoketMQ官方Starter Github](https://github.com/apache/rocketmq-spring)